package com.example.gymutil.ui.navigation

import androidx.lifecycle.Lifecycle
import androidx.navigation.*
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import com.example.gymutil.ui.plans.exercisesTemplates.CreateExerciseDialog
import com.example.gymutil.ui.plans.exercisesTemplates.EditExerciseTemplate
import com.example.gymutil.ui.plans.exercisesTemplates.ViewExerciseTemplates
import com.example.gymutil.ui.plans.workoutTemplates.CreatePlan
import com.example.gymutil.ui.plans.workoutTemplates.EditPlan
import com.example.gymutil.ui.plans.workoutTemplates.ViewPlans

fun NavGraphBuilder.plansGraph(
    navController: NavHostController
) {
    navigation<Screens.Plans>(startDestination = Screens.Plans.ViewPlans) {
        composable<Screens.Plans.ViewPlans> {
            ViewPlans(goToWorkoutTemplate = {
                if (navController.currentBackStackEntry?.lifecycleIsResumed() == true) {
                    navController.navigate(Screens.Plans.EditPlan(it))
                }
            }, navToCreateWorkoutTemplate = {
                if (navController.currentBackStackEntry?.lifecycleIsResumed() == true) {
                    navController.navigate(Screens.Plans.CreatePlan)
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

        dialog<Screens.Plans.CreatePlan> {
            CreatePlan(onCreateWorkoutTemplate = {
                if (navController.currentBackStackEntry?.lifecycleIsResumed() == true) {
                    navController.navigate(Screens.Plans.EditPlan(it))
                }
            })
        }

        composable<Screens.Plans.ViewExercises> {
            ViewExerciseTemplates(goToExerciseTemplate = {
                if (navController.currentBackStackEntry?.lifecycleIsResumed() == true) {
                    navController.navigate(Screens.Plans.EditExercise(it))
                }
            }, navToCreateExerciseTemplate = {
                if (navController.currentBackStackEntry?.lifecycleIsResumed() == true) {
                    navController.navigate(Screens.Plans.CreateExercise)
                }
            }, onBackPressed = {
                if (navController.currentBackStackEntry?.lifecycleIsResumed() == true) {
                    navController.popBackStack()
                }
            })
        }

        dialog<Screens.Plans.CreateExercise> {
            CreateExerciseDialog(onCreateExercise = {
                if (navController.currentBackStackEntry?.lifecycleIsResumed() == true) {
                    navController.navigate(Screens.Plans.EditExercise(it))
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