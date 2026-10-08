package eu.trmdnt.workouts.database

import android.content.Context
import android.util.Log
import androidx.room3.*
import androidx.room3.migration.AutoMigrationSpec
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.sqlite.execSQL
import eu.trmdnt.workouts.database.entities.*
import java.io.File

const val DATABASE_NAME = "gym_database"
private const val TAG = "AppDatabase"
const val DATABASE_VERSION = 7

@Database(
    entities = [Exercise::class, ExerciseTemplate::class, ExerciseSet::class, Workout::class, WorkoutTemplate::class, WorkoutExerciseTemplateCrossRef::class],
    version = DATABASE_VERSION,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 5, to = 6, spec = MIGRATION_SPEC_5_6::class),
        AutoMigration(from = 6, to = 7),
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): Dao
    abstract fun statisticsDao(): StatisticsDao
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL("UPDATE `Set` SET `date` = `date` / 1000")
        connection.execSQL("UPDATE `Workout` SET `dateStarted` = `dateStarted` / 1000")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE `Set` ADD COLUMN set_type TEXT NOT NULL DEFAULT 'Default'")
        connection.execSQL(
            """
    UPDATE `Set`
    SET `set_type` = CASE
        WHEN `ignore_in_stat` = 1 THEN 'WarmUp'
        ELSE 'Default'
    END
""".trimIndent()
        )
    }
}

@DeleteColumn("Set", "ignore_in_stat")
internal class MIGRATION_SPEC_5_6 : AutoMigrationSpec


fun getDatabase(context: Context): AppDatabase {
    applyPendingRestore(context)

    val builder = Room.databaseBuilder(
        context.applicationContext, AppDatabase::class.java, DATABASE_NAME
    ).addMigrations(MIGRATION_3_4).addMigrations(MIGRATION_4_5)
        .setDriver(AndroidSQLiteDriver())

    return builder.build()
}

private fun applyPendingRestore(context: Context) {
    Log.d(TAG, "applyPendingRestore: check for pending restore")
    val dbPath = context.getDatabasePath(DATABASE_NAME).path
    val staged = File("$dbPath.restore")

    if (staged.exists()) {
        Log.d(TAG, "applyPendingRestore: pending restore")
        listOf("", "-wal", "-shm", ".lck", "-journal").forEach { File(dbPath + it).delete() }

        if (staged.renameTo(File(dbPath))) {
            Log.d(TAG, "applyPendingRestore: restore applied")
        } else {
            Log.d(TAG, "applyPendingRestore: restore not applied")
        }
    } else {
        Log.d(TAG, "applyPendingRestore: no pending restore")
    }
}