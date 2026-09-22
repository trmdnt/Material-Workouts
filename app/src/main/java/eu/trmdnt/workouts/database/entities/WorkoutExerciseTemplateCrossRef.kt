package eu.trmdnt.workouts.database.entities

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey

@Entity(
    tableName = "workout_exercise_template_cross_ref",
    primaryKeys = ["exercise_template_id", "workout_template_id"],
    foreignKeys = [
        ForeignKey(
            entity = ExerciseTemplate::class,
            parentColumns = arrayOf("exercise_template_id"),
            childColumns = arrayOf("exercise_template_id"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = WorkoutTemplate::class,
            parentColumns = arrayOf("workout_template_id"),
            childColumns = arrayOf("workout_template_id"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)

data class WorkoutExerciseTemplateCrossRef(
    @ColumnInfo(name = "workout_template_id", index = true)
    val workoutTemplateId: Long,

    @ColumnInfo(name = "exercise_template_id", index = true)
    val exerciseTemplateId: Long
)
