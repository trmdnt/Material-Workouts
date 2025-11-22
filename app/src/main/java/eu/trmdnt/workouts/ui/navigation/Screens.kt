package eu.trmdnt.workouts.ui.navigation

import kotlinx.serialization.Serializable

const val uri = "workouts://app"

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
        object ViewExercises

        @Serializable
        data class EditExercise(
            val exerciseTemplateId: Long,
        )
    }

    @Serializable
    object Statistics {
        @Serializable
        object ListStatistics

        @Serializable
        data class ViewWeightPerRep(
            val exerciseTemplateId: Long,
        )
    }


    @Serializable
    object Settings {
        @Serializable
        object MainSettings
    }
}