package eu.trmdnt.workouts.database.backup

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.room3.immediateTransaction
import androidx.room3.useWriterConnection
import eu.trmdnt.workouts.database.AppDatabase
import eu.trmdnt.workouts.database.DATABASE_NAME
import eu.trmdnt.workouts.database.DATABASE_VERSION
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class BackupManager(val db: AppDatabase, val appContext: Context) {

    companion object {
        val TAG = BackupManager::class.qualifiedName
    }

    suspend fun backup(uri: Uri) {
        val snapshot = File(appContext.cacheDir, "backup_snapshot.db")
        // vacuum into would be simpler but requires api30
        try {
            withContext(Dispatchers.IO) {
                // open write connection to prevent writes
                db.useWriterConnection { conn ->
                    // make sure wal is empty
                    conn.usePrepared("PRAGMA wal_checkpoint(TRUNCATE)") { stmt ->
                        stmt.step()
                        check(stmt.getLong(0) == 0L) { "Checkpoint blocked by an active reader" }
                    }

                    // copy to fast storage
                    // immediate transaction should not be required as there is a single process
                    conn.immediateTransaction {
                        appContext.getDatabasePath(DATABASE_NAME).copyTo(snapshot, overwrite = true)
                    }
                }

                val filename = "$DATABASE_NAME${System.currentTimeMillis()}.db"

                val tree = DocumentFile.fromTreeUri(appContext, uri) ?: error("bad tree")

                val out = tree.createFile("application/octet-stream", filename)
                    ?: error("Could not create file")

                try {
                    (appContext.contentResolver.openOutputStream(out.uri, "wt")
                        ?: error("could not open output stream")).use { os ->
                        snapshot.inputStream().use { it.copyTo(os) }
                    }
                } catch (t: Throwable) {
                    out.delete()
                    throw t
                }

            }
        } finally {
            snapshot.delete()
        }
    }

    enum class RestoreResult {
        SUCCESS,
        BAD_FILE,
        OTHER
    }

    suspend fun restore(uri: Uri): RestoreResult {
        val dbpath = appContext.getDatabasePath(DATABASE_NAME).path
        val tmpPath = "$dbpath.restore.tmp"
        val tmpFile = File(tmpPath)

        val stagedPath = "$dbpath.restore"
        val stagedFile = File(stagedPath)

        fun cleanupTmp() = listOf("", "-wal", "-shm", ".lck", "-journal").forEach { File(tmpPath + it).delete() }

        try {
            cleanupTmp()
            tmpFile.outputStream().use { os ->
                appContext.contentResolver.openInputStream(uri).use { inputStream ->
                    (inputStream ?: error("could not open inputStream")).copyTo(os)
                }
            }

            val dbIsOk = SQLiteDatabase.openDatabase(tmpFile.path, null, SQLiteDatabase.OPEN_READONLY).use { db ->
                db.rawQuery("PRAGMA integrity_check", null)
                    .use { it.moveToFirst() && it.getString(0) == "ok" }
                        && db.version <= DATABASE_VERSION
            }

            return if (dbIsOk) {
                if (tmpFile.renameTo(stagedFile)) {
                    RestoreResult.SUCCESS
                } else {
                    RestoreResult.OTHER
                }

            } else {
                RestoreResult.BAD_FILE
            }
        } catch (e: SQLiteException) {
            Log.e(TAG, "restore: Failed to restore db", e)
            return RestoreResult.BAD_FILE
        } catch (t: Throwable) {
            Log.e(TAG, "restore: Failed to restore db", t)
            return RestoreResult.OTHER
        } finally {
            cleanupTmp()
        }
    }
}