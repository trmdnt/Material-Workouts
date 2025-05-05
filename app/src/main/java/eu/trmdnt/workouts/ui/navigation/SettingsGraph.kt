package eu.trmdnt.workouts.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import eu.trmdnt.workouts.ui.settings.Settings

fun NavGraphBuilder.settingsGraph(
    navController: NavHostController
) {
    navigation<Screens.Settings>(startDestination = Screens.Settings.MainSettings) {
        composable<Screens.Settings.MainSettings> {
            Settings()
        }
    }
}