package eu.trmdnt.workouts.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import eu.trmdnt.workouts.database.entities.Exercise
import eu.trmdnt.workouts.database.entities.ExerciseSet
import eu.trmdnt.workouts.database.entities.ExerciseTemplate
import eu.trmdnt.workouts.database.entities.Workout
import eu.trmdnt.workouts.database.entities.WorkoutExerciseTemplateCrossRef
import eu.trmdnt.workouts.database.entities.WorkoutTemplate

@Database(
    entities = [
        Exercise::class,
        ExerciseTemplate::class,
        ExerciseSet::class,
        Workout::class,
        WorkoutTemplate::class,
        WorkoutExerciseTemplateCrossRef::class],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): Dao
}