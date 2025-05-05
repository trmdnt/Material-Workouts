package eu.trmdnt.workouts.ui.navigation

import androidx.navigation.*
import androidx.navigation.compose.composable
import eu.trmdnt.workouts.ui.activities.ListWorkouts
import eu.trmdnt.workouts.ui.activities.ViewWorkout

fun NavGraphBuilder.activitiesGraph(
    navController: NavHostController,
    startTimer: (workoutId: Long?) -> Unit
) {
    navigation<Screens.Activities>(startDestination = Screens.Activities.ListWorkouts) {
        composable<Screens.Activities.ListWorkouts> {
            ListWorkouts(navigateToWorkout = { workoutId, edit ->
                if (navController.currentBackStackEntry?.lifecycleIsResumed() == true) {
                    navController.navigate(Screens.Activities.ViewWorkout(workoutId, edit))
                }
            })
        }
        composable<Screens.Activities.ViewWorkout>(
            deepLinks = listOf(
                navDeepLink<Screens.Activities.ViewWorkout>(basePath = "$uri/activities/workout")
            )
        ) { entry ->
            val viewWorkout = entry.toRoute<Screens.Activities.ViewWorkout>()
            ViewWorkout(
                viewWorkout.workoutId, viewWorkout.edit,
                onBackPressed = {
                    if (navController.currentBackStackEntry?.lifecycleIsResumed() == true) {
                        navController.popBackStack()
                    }
                },
                startTimer = startTimer
            )
        }
    }
}