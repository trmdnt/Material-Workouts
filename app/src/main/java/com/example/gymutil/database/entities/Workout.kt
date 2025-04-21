package com.example.gymutil.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = WorkoutTemplate::class,
            parentColumns = arrayOf("workout_template_id"),
            childColumns = arrayOf("workout_template_id"),
            onDelete = ForeignKey.SET_NULL
        ),
    ]
)
data class Workout(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo
    val name: String,

    @ColumnInfo
    val dateStarted: Long,

    @ColumnInfo(name = "workout_template_id")
    val workoutTemplateId: Long?,

    )
