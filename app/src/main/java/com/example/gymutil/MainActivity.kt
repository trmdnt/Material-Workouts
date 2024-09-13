package com.example.gymutil


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.example.gymutil.ui.theme.GymUtilTheme

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val homeTab = TabBarItem(title = "Home", selectedIcon = Icons.Filled.Home, unselectedIcon = Icons.Outlined.Home)
            val alertsTab = TabBarItem(title = "Alerts", selectedIcon = Icons.Filled.Notifications, unselectedIcon = Icons.Outlined.Notifications)
            val settingsTab = TabBarItem(title = "Settings", selectedIcon = Icons.Filled.Settings, unselectedIcon = Icons.Outlined.Settings)
            val moreTab = TabBarItem(title = "More", selectedIcon = Icons.AutoMirrored.Filled.List, unselectedIcon = Icons.AutoMirrored.Outlined.List)

            // creating a list of all the tabs
            val tabBarItems = listOf(homeTab, alertsTab, settingsTab, moreTab)

            val enterTransition = fadeIn(animationSpec = tween(durationMillis = 300))
            val exitTransition = fadeOut(animationSpec = tween(durationMillis = 300))

            // creating our navController
            val navController = rememberNavController()

            GymUtilTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(Modifier.safeDrawingPadding()) {
                        Scaffold(bottomBar = { TabView(tabBarItems, navController) }) {
                            NavHost(
                                navController = navController,
                                startDestination = homeTab.title,
                                enterTransition = { enterTransition},
                                exitTransition = { exitTransition},
                                popEnterTransition = { enterTransition },
                                popExitTransition = { exitTransition },
                            ) {
                                composable(homeTab.title) {
                                    Text(homeTab.title)
                                }
                                composable(alertsTab.title) {
                                    Text(alertsTab.title)
                                }
                                composable(settingsTab.title) {
                                    Text(settingsTab.title)
                                }
                                composable(moreTab.title) {
                                    MoreView()
                                }
                            }
                        }
                    }

                }
            }
        }
    }
}

data class TabBarItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun TabView(tabBarItems: List<TabBarItem>, navController: NavController) {
    val mainViewModel = viewModel<MainViewModel>()
    val selectedTabIndex by mainViewModel.selectedTabIndex.observeAsState(0)

    NavigationBar {
        // looping over each tab to generate the views and navigation for each item
        tabBarItems.forEachIndexed { index, tabBarItem ->
            NavigationBarItem(
                selected = selectedTabIndex == index,
                onClick = {
                    if (selectedTabIndex != index) {
                        mainViewModel.selectTab(index)
                        navController.navigate(tabBarItem.title, navOptions {})
                    }

                },
                icon = {
                    TabBarIconView(
                        isSelected = selectedTabIndex == index,
                        selectedIcon = tabBarItem.selectedIcon,
                        unselectedIcon = tabBarItem.unselectedIcon,
                        title = tabBarItem.title,
                    )
                },
                label = {
                    val animationTimeMillis = 300
                    AnimatedVisibility(
                        visible = selectedTabIndex == index,
                        enter = expandVertically (animationSpec = tween(durationMillis = animationTimeMillis)),
                        exit = shrinkVertically (animationSpec = tween(durationMillis = animationTimeMillis))
                    ) {
                        Text(tabBarItem.title, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
fun TabBarIconView(
    isSelected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    title: String,
) {
    Crossfade(targetState = isSelected, animationSpec = tween(durationMillis = 300)) { selected ->
        Icon(
            imageVector = if (selected) selectedIcon else unselectedIcon,
            contentDescription = title
        )
    }

}

@Composable
fun MoreView() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text("Thing 1")
        Text("Thing 2")
        Text("Thing 3")
        Text("Thing 4")
        Text("Thing 5")
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GymUtilTheme {
        MoreView()
    }
}
