package com.example.gymutil.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.gymutil.ui.statistics.ListStatistics

fun NavGraphBuilder.statisticsGraph(
    navController: NavHostController
) {
    navigation<Screens.Statistics>(startDestination = Screens.Statistics.ListStatistics) {
        composable<Screens.Statistics.ListStatistics> {
            ListStatistics()
        }
    }
}