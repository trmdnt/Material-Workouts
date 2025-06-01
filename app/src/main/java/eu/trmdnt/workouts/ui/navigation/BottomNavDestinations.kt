package eu.trmdnt.workouts.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavDestinations(
    val title: String, val icon: ImageVector,
    //TODO Any is stupid
    val route: Any
) {
    data object ActivitiesDest : BottomNavDestinations(
        title = "Activities", icon = Icons.Filled.FitnessCenter, route = Screens.Activities
    )

    data object PlansDest : BottomNavDestinations(title = "Plans", icon = Icons.Filled.Summarize, route = Screens.Plans)

    data object StatisticsDest : BottomNavDestinations(
        title = "Statistics", icon = Icons.AutoMirrored.Filled.ShowChart, route = Screens.Statistics
    )

    data object SettingsDest :
        BottomNavDestinations(title = "Settings", icon = Icons.Filled.Settings, route = Screens.Settings)
}