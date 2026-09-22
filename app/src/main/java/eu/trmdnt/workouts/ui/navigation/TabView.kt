package eu.trmdnt.workouts.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navOptions

@Composable
fun TabView(navController: NavController) {
    val bottomNavDestinations = remember {
        listOf(
            BottomNavDestinations.ActivitiesDest,
            BottomNavDestinations.PlansDest,
            BottomNavDestinations.StatisticsDest,
            BottomNavDestinations.SettingsDest
        )
    }
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination

    NavigationBar {
        // looping over each tab to generate the views and navigation for each item
        bottomNavDestinations.forEach { destination ->
            val isSelected: Boolean = currentDestination?.hierarchy?.any {
                it.hasRoute(destination.route::class)
            } == true

            NavigationBarItem(selected = isSelected, onClick = {
                if (!isSelected) {
                    navController.navigate(
                        route = destination.route, navOptions = navOptions {
                            this.popUpTo(Screen.Activities.ListWorkouts)
                        })
                }
            }, icon = {
                Icon(
                    imageVector = destination.icon, contentDescription = destination.title
                )
            }, label = {
                AnimatedVisibility(
                    visible = isSelected, enter = expandVertically(), exit = shrinkVertically()
                ) {
                    Text(destination.title, fontWeight = FontWeight.Bold)
                }
            })
        }
    }
}