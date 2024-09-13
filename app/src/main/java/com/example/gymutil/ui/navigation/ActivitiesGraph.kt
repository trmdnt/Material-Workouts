package com.example.gymutil.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.example.gymutil.ui.activities.ListWorkouts
import com.example.gymutil.ui.activities.ViewWorkout

fun NavGraphBuilder.activitiesGraph(
    navController: NavHostController
) {
    navigation<Screens.Activities>(startDestination = Screens.Activities.ListWorkouts) {
        composable<Screens.Activities.ListWorkouts> {
            ListWorkouts(navigateToWorkout = { workoutId, edit ->
                if (navController.currentBackStackEntry?.lifecycleIsResumed() == true) {
                    navController.navigate(Screens.Activities.ViewWorkout(workoutId, edit))
                }
            })
        }
        composable<Screens.Activities.ViewWorkout> { entry ->
            val viewWorkout = entry.toRoute<Screens.Activities.ViewWorkout>()
            ViewWorkout(
                viewWorkout.workoutId, viewWorkout.edit,
                onBackPressed = {
                    if (navController.currentBackStackEntry?.lifecycleIsResumed() == true) {
                        navController.popBackStack()
                    }
                }
            )
        }
    }
}