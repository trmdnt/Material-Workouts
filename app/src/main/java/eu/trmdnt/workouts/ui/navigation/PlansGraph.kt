package eu.trmdnt.workouts.ui.navigation

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import eu.trmdnt.workouts.ui.plans.exercisesTemplates.EditExerciseTemplate
import eu.trmdnt.workouts.ui.plans.exercisesTemplates.ViewExerciseTemplates
import eu.trmdnt.workouts.ui.plans.workoutTemplates.EditPlan
import eu.trmdnt.workouts.ui.plans.workoutTemplates.ViewPlans

fun NavGraphBuilder.plansGraph(
    navController: NavHostController
) {
    val navEventHandler = NavEventHandler(navController)
    navigation<Screen.Plans>(startDestination = Screen.Plans.ViewPlans) {
        composable<Screen.Plans.ViewPlans> {
            ViewPlans(
                navEventHandler
            )
        }
        composable<Screen.Plans.EditPlan> { entry ->
            val editPlan = entry.toRoute<Screen.Plans.EditPlan>()
            EditPlan(workoutTemplateId = editPlan.planId, navEventHandler = navEventHandler)
        }

        composable<Screen.Plans.ViewExercises> {
            ViewExerciseTemplates(navEventHandler)
        }

        composable<Screen.Plans.EditExercise> { entry ->
            val editExercise = entry.toRoute<Screen.Plans.EditExercise>()
            EditExerciseTemplate(
                exerciseTemplateId = editExercise.exerciseTemplateId,
                navEventHandler = navEventHandler
            )
        }
    }
}

internal fun NavBackStackEntry.lifecycleIsResumed() =
    this.lifecycle.currentState == Lifecycle.State.RESUMED