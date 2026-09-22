package eu.trmdnt.workouts.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import androidx.navigation.toRoute
import eu.trmdnt.workouts.ui.activities.ListWorkoutsScreen
import eu.trmdnt.workouts.ui.activities.ViewWorkout

fun NavGraphBuilder.activitiesGraph(
    navController: NavHostController,
) {
    val navEventHandler = NavEventHandler(navController)
    navigation<Screen.Activities>(startDestination = Screen.Activities.ListWorkouts) {
        composable<Screen.Activities.ListWorkouts> {
            ListWorkoutsScreen(navEventHandler = navEventHandler)
        }
        composable<Screen.Activities.ViewWorkout>(
            deepLinks = listOf(
                navDeepLink<Screen.Activities.ViewWorkout>(basePath = "$uri/activities/workout")
            )
        ) { entry ->
            val viewWorkout = entry.toRoute<Screen.Activities.ViewWorkout>()
            ViewWorkout(
                viewWorkout.workoutId, viewWorkout.edit,
                navEventHandler
            )
        }
    }
}