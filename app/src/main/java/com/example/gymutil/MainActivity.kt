package com.example.gymutil


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.example.gymutil.ui.navigation.*
import com.example.gymutil.ui.theme.GymUtilTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        //WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            GymUtilTheme {
                //TODO fix deprecation
                val backgroundColor = MaterialTheme.colorScheme.surfaceContainer.toArgb()
                window.navigationBarColor = backgroundColor
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val enterTransition = fadeIn()
    val exitTransition = fadeOut()

    val navController = rememberNavController()
    Scaffold(bottomBar = { TabView(navController) }) { contentPadding ->
        NavHost(
            navController = navController,
            startDestination = Screens.Activities,
            enterTransition = { enterTransition },
            exitTransition = { exitTransition },
            popEnterTransition = { enterTransition },
            popExitTransition = { exitTransition },
            modifier = Modifier
                .padding(contentPadding)
                .consumeWindowInsets(contentPadding)
        ) {
            activitiesGraph(navController)
            plansGraph(navController)
            statisticsGraph(navController)
            settingsGraph(navController)
        }
    }
}

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
                            popUpTo(Screens.Activities.ListWorkouts)
                        })
                }

            }, icon = {
                Icon(
                    imageVector = destination.icon, contentDescription = destination.title
                )
            }, label = {
//                    val animationTimeMillis = 300
//                    AnimatedVisibility(
//                        visible = isSelected,
//                        enter = expandVertically(),
//                        exit = shrinkVertically()
//                    ) {
                Text(destination.title, fontWeight = FontWeight.Bold)
//                    }
            })
        }
    }
}