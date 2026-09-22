package eu.trmdnt.workouts.database

import android.content.Context
import androidx.room3.*
import androidx.room3.migration.AutoMigrationSpec
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.sqlite.execSQL
import eu.trmdnt.workouts.database.entities.*

@Database(
    entities = [Exercise::class, ExerciseTemplate::class, ExerciseSet::class, Workout::class, WorkoutTemplate::class, WorkoutExerciseTemplateCrossRef::class],
    version = 7,
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
    val builder = Room.databaseBuilder(
        context.applicationContext, AppDatabase::class.java, "gym_database"
    ).addMigrations(MIGRATION_3_4).addMigrations(MIGRATION_4_5)
        .setDriver(AndroidSQLiteDriver())

    //TODO fix this
    // from https://stackoverflow.com/a/23844693
//    val isDebuggable = 0 != context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
//    if (isDebuggable) {
//        builder.setQueryCallback({ sqlquery, bindargs ->
//            Log.d("DB_QUERY", "$sqlquery SQL Args: $bindargs")
//        }, Executors.newSingleThreadExecutor())
//    }

    return builder.build()
}