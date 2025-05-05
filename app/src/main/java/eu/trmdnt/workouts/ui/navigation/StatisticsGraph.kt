package eu.trmdnt.workouts.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import eu.trmdnt.workouts.ui.statistics.ListStatistics

fun NavGraphBuilder.statisticsGraph(
    navController: NavHostController
) {
    navigation<Screens.Statistics>(startDestination = Screens.Statistics.ListStatistics) {
        composable<Screens.Statistics.ListStatistics> {
            ListStatistics()
        }
    }
}