package eu.trmdnt.workouts.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import eu.trmdnt.workouts.ui.statistics.ListStatistics
import eu.trmdnt.workouts.ui.statistics.ViewWeightPerRep

fun NavGraphBuilder.statisticsGraph(
    navController: NavHostController
) {
    val navEventHandler = NavEventHandler(navController)
    navigation<Screen.Statistics>(startDestination = Screen.Statistics.ListStatistics) {
        composable<Screen.Statistics.ListStatistics> {
            ListStatistics(navEventHandler)
        }

        composable<Screen.Statistics.ViewWeightPerRep> { entry ->
            val exerciseId = entry.toRoute<Screen.Statistics.ViewWeightPerRep>().exerciseTemplateId
            ViewWeightPerRep(exerciseId, navEventHandler = navEventHandler)
        }
    }
}