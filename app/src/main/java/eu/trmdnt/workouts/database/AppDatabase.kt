package eu.trmdnt.workouts.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import eu.trmdnt.workouts.database.entities.*

@Database(
    entities = [
        Exercise::class,
        ExerciseTemplate::class,
        ExerciseSet::class,
        Workout::class,
        WorkoutTemplate::class,
        WorkoutExerciseTemplateCrossRef::class],
    version = 3,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): Dao
}