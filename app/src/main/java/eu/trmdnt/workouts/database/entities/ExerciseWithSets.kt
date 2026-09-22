package eu.trmdnt.workouts.database.entities

import androidx.room3.Embedded
import androidx.room3.Relation

data class ExerciseWithSets(
    @Embedded val exercise: Exercise,
    @Relation(
        parentColumns = ["exercise_id"],
        entityColumns = ["exercise_id"]
    )
    val exerciseSets: List<ExerciseSet>,

    @Relation(
        parentColumns = ["exercise_template_id"],
        entityColumns = ["exercise_template_id"]
    )
    val exerciseTemplate: ExerciseTemplate
)