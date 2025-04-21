package com.example.gymutil.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.gymutil.database.entities.*

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