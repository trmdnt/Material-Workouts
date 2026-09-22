package eu.trmdnt.workouts.database.entities

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.PrimaryKey

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

    @ColumnInfo(name = "workout_template_id", index = true)
    val workoutTemplateId: Long?,

    )
