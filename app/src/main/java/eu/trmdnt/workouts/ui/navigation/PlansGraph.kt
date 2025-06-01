package eu.trmdnt.workouts.ui.navigation

import androidx.lifecycle.Lifecycle
import androidx.navigation.*
import androidx.navigation.compose.composable
import eu.trmdnt.workouts.ui.plans.exercisesTemplates.EditExerciseTemplate
import eu.trmdnt.workouts.ui.plans.exercisesTemplates.ViewExerciseTemplates
import eu.trmdnt.workouts.ui.plans.workoutTemplates.EditPlan
import eu.trmdnt.workouts.ui.plans.workoutTemplates.ViewPlans

fun NavGraphBuilder.plansGraph(
    navController: NavHostController
) {
    navigation<Screens.Plans>(startDestination = Screens.Plans.ViewPlans) {
        composable<Screens.Plans.ViewPlans> {
            ViewPlans(goToWorkoutTemplate = {
                if (navController.currentBackStackEntry?.lifecycleIsResumed() == true) {
                    navController.navigate(Screens.Plans.EditPlan(it))
                }
            })
        }
        composable<Screens.Plans.EditPlan> { entry ->
            val editPlan = entry.toRoute<Screens.Plans.EditPlan>()
            EditPlan(workoutTemplateId = editPlan.planId, onBackPressed = {
                if (navController.currentBackStackEntry?.lifecycleIsResumed() == true) {
                    navController.popBackStack()
                }
            }, navToViewExercises = {
                if (navController.currentBackStackEntry?.lifecycleIsResumed() == true) {
                    navController.navigate(Screens.Plans.ViewExercises)
                }
            })
        }

        composable<Screens.Plans.ViewExercises> {
            ViewExerciseTemplates(goToExerciseTemplate = {
                if (navController.currentBackStackEntry?.lifecycleIsResumed() == true) {
                    navController.navigate(Screens.Plans.EditExercise(it))
                }
            }, onBackPressed = {
                if (navController.currentBackStackEntry?.lifecycleIsResumed() == true) {
                    navController.popBackStack()
                }
            })
        }

        composable<Screens.Plans.EditExercise> { entry ->
            val editExercise = entry.toRoute<Screens.Plans.EditExercise>()
            EditExerciseTemplate(exerciseTemplateId = editExercise.exerciseTemplateId, onBackPressed = {
                if (navController.currentBackStackEntry?.lifecycleIsResumed() == true) {
                    navController.popBackStack()
                }
            })
        }
    }
}

internal fun NavBackStackEntry.lifecycleIsResumed() = this.lifecycle.currentState == Lifecycle.State.RESUMED