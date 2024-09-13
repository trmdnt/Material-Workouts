package com.example.gymutil.database.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class WorkoutTemplateWithExercises(
    @Embedded val workoutTemplate: WorkoutTemplate,
    @Relation(
        parentColumn = "workout_template_id",
        entityColumn = "exercise_template_id",
        associateBy = Junction(WorkoutExerciseTemplateCrossRef::class)
    )
    val exerciseTemplates: List<ExerciseTemplate>

)
