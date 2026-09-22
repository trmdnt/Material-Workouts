package eu.trmdnt.workouts.ui.navigation

import androidx.navigation.NavController

sealed class NavEvent {
    object Back : NavEvent()
    data class Destination(val dest: Screen) : NavEvent()
}

class NavEventHandler(private val navController: NavController) {
    fun handle(event: NavEvent) = when (event) {
        is NavEvent.Back -> {
            if (navController.currentBackStackEntry?.lifecycleIsResumed() == true) {
                navController.popBackStack()
            } else {
            }
        }

        is NavEvent.Destination -> {
            if (navController.currentBackStackEntry?.lifecycleIsResumed() == true) {
                navController.navigate(event.dest)
            } else {
            }
        }
    }

    fun goBack() = handle(NavEvent.Back)
}