package eu.trmdnt.workouts.ui.navigation

import kotlinx.serialization.Serializable

const val uri = "workouts://app"

@Serializable
sealed class Screen {
    @Serializable
    object Activities : Screen() {
        @Serializable
        object ListWorkouts : Screen()

        @Serializable
        data class ViewWorkout(
            val workoutId: Long,
            val edit: Boolean = false
        ) : Screen()

    }

    @Serializable
    object Plans : Screen() {
        @Serializable
        object ViewPlans : Screen()

        @Serializable
        data class EditPlan(
            val planId: Long
        ) : Screen()

        @Serializable
        object ViewExercises : Screen()

        @Serializable
        data class EditExercise(
            val exerciseTemplateId: Long,
        ) : Screen()
    }

    @Serializable
    object Statistics : Screen() {
        @Serializable
        object ListStatistics : Screen()

        @Serializable
        data class ViewWeightPerRep(
            val exerciseTemplateId: Long,
        ) : Screen()
    }


    @Serializable
    object Settings : Screen() {
        @Serializable
        object MainSettings : Screen()
    }
}