package com.example.gymutil.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.gymutil.database.entities.*
import com.example.gymutil.database.entities.ExerciseSet

@Database(
    entities = [
        Exercise::class,
        ExerciseTemplate::class,
        ExerciseSet::class,
        Workout::class,
        WorkoutTemplate::class,
        WorkoutExerciseTemplateCrossRef::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): Dao
}