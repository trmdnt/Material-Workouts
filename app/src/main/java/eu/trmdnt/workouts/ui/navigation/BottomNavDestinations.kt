package eu.trmdnt.workouts.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavDestinations(
    val title: String,
    val icon: ImageVector,
    val route: Screen
) {
    data object ActivitiesDest : BottomNavDestinations(
        title = "Activities", icon = Icons.Filled.FitnessCenter, route = Screen.Activities
    )

    data object PlansDest :
        BottomNavDestinations(title = "Plans", icon = Icons.Filled.Summarize, route = Screen.Plans)

    data object StatisticsDest : BottomNavDestinations(
        title = "Statistics", icon = Icons.AutoMirrored.Filled.ShowChart, route = Screen.Statistics
    )

    data object SettingsDest :
        BottomNavDestinations(
            title = "Settings",
            icon = Icons.Filled.Settings,
            route = Screen.Settings
        )
}