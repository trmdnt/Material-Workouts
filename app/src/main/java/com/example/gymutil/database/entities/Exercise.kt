package com.example.gymutil.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("workout_id"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseTemplate::class,
            parentColumns = arrayOf("exercise_template_id"),
            childColumns = arrayOf("exercise_template_id"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Exercise(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "exercise_id")
    val exerciseId: Long = 0,

    @ColumnInfo(name = "workout_id")
    val workoutId: Long,

    @ColumnInfo(name = "exercise_template_id")
    val exerciseTemplateId: Long

)
