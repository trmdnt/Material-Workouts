package eu.trmdnt.workouts.database.entities

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.PrimaryKey

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

    @ColumnInfo(name = "workout_id", index = true)
    val workoutId: Long,

    @ColumnInfo(name = "exercise_template_id", index = true)
    val exerciseTemplateId: Long

)
