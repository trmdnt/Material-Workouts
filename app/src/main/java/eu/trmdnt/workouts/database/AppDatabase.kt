package eu.trmdnt.workouts.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import eu.trmdnt.workouts.database.entities.*

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
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("UPDATE `Set` SET `date` = `date` / 1000")
        db.execSQL("UPDATE `Workout` SET `dateStarted` = `dateStarted` / 1000")
    }
}

fun getDatabase(context: Context): AppDatabase {
    return Room.databaseBuilder(
        context.applicationContext, AppDatabase::class.java, "gym_database"
    ).addMigrations(MIGRATION_3_4).build()
}