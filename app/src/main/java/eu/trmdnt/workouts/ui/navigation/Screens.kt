package eu.trmdnt.workouts.ui.navigation

import kotlinx.serialization.Serializable

val uri = "workouts://app"

@Serializable
sealed class Screens {
    @Serializable
    object Activities {
        @Serializable
        object ListWorkouts

        @Serializable
        data class ViewWorkout(
            val workoutId: Long,
            val edit: Boolean = false
        )

        @Serializable
        object StartWorkout
    }

    @Serializable
    object Plans {
        @Serializable
        object ViewPlans

        @Serializable
        data class EditPlan(
            val planId: Long
        )

        @Serializable
        object CreatePlan

        @Serializable
        object ViewExercises

        @Serializable
        data class EditExercise(
            val exerciseTemplateId: Long,
        )

        @Serializable
        object CreateExercise
    }

    @Serializable
    object Statistics {
        @Serializable
        object ListStatistics
    }


    @Serializable
    object Settings {
        @Serializable
        object MainSettings
    }
}