package eu.trmdnt.workouts.database.entities

import androidx.room.Embedded
import androidx.room.Relation

data class ExerciseWithSets(
    @Embedded val exercise: Exercise,
    @Relation(
        parentColumn = "exercise_id",
        entityColumn = "exercise_id"
    )
    val exerciseSets: List<ExerciseSet>,

    @Relation(
        parentColumn = "exercise_template_id",
        entityColumn = "exercise_template_id"
    )
    val exerciseTemplate: ExerciseTemplate
)