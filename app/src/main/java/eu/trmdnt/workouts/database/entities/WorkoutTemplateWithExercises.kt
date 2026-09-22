package eu.trmdnt.workouts.database.entities

import androidx.room3.Embedded
import androidx.room3.Junction
import androidx.room3.Relation

data class WorkoutTemplateWithExercises(
    @Embedded val workoutTemplate: WorkoutTemplate,
    @Relation(
        parentColumns = ["workout_template_id"],
        entityColumns = ["exercise_template_id"],
        associateBy = Junction(WorkoutExerciseTemplateCrossRef::class)
    )
    val exerciseTemplates: List<ExerciseTemplate>

)
