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
    navigation<Screens.Statistics>(startDestination = Screens.Statistics.ListStatistics) {
        composable<Screens.Statistics.ListStatistics> {
            ListStatistics({
                if (navController.currentBackStackEntry?.lifecycleIsResumed() == true) {
                    navController.navigate(Screens.Statistics.ViewWeightPerRep(it))
                }
            })
        }

        composable<Screens.Statistics.ViewWeightPerRep> { entry ->
            val exerciseId = entry.toRoute<Screens.Statistics.ViewWeightPerRep>().exerciseTemplateId
            ViewWeightPerRep(exerciseId, onBackPressed = { navController.popBackStack() })
        }
    }
}