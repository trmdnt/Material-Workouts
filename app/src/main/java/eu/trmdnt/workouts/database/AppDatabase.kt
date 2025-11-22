package eu.trmdnt.workouts.database

import android.content.Context
import android.util.Log
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import eu.trmdnt.workouts.database.entities.*
import java.util.concurrent.Executors

@Database(
    entities = [
        Exercise::class,
        ExerciseTemplate::class,
        ExerciseSet::class,
        Workout::class,
        WorkoutTemplate::class,
        WorkoutExerciseTemplateCrossRef::class],
    version = 4,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): Dao
    abstract fun statisticsDao(): StatisticsDao
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("UPDATE `Set` SET `date` = `date` / 1000")
        db.execSQL("UPDATE `Workout` SET `dateStarted` = `dateStarted` / 1000")
    }
}

fun getDatabase(context: Context): AppDatabase {
    val builder = Room.databaseBuilder(
        context.applicationContext, AppDatabase::class.java, "gym_database"
    ).addMigrations(MIGRATION_3_4)

    builder.setQueryCallback({ sqlquery, bindargs ->
        Log.d("DB_QUERY", "$sqlquery SQL Args: $bindargs")
    }, Executors.newSingleThreadExecutor())

    return builder.build()
}