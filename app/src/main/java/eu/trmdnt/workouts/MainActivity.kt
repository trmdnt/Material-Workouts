package eu.trmdnt.workouts


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Timer10
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import dagger.hilt.android.AndroidEntryPoint
import eu.trmdnt.workouts.ui.components.SelectTimespanDialog
import eu.trmdnt.workouts.ui.navigation.*
import eu.trmdnt.workouts.ui.theme.AppTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: MainActivityViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            AppTheme {
                //TODO fix deprecation
                val backgroundColor = MaterialTheme.colorScheme.surfaceContainer.toArgb()
                window.navigationBarColor = backgroundColor
                MainScreen(
                    showTimer = uiState.showTimer,
                    timerText = uiState.timerText,
                    onTimerCancelPressed = {
                        viewModel.onTimerCancelPressed()
                    },
                    onTimerAddTimePressed = {
                        viewModel.onTimerAddTimePressed()
                    },
                    addTimer = { workoutId, time ->
                        viewModel.addTimer(workoutId, time)
                    },
                    showTimerPickerButton = uiState.showTimerPickerButton,
                    timerDefaultValue = uiState.timerDefaultValue,
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

@Composable
fun MainScreen(
    showTimer: Boolean,
    showTimerPickerButton: Boolean,
    timerText: String?,
    onTimerCancelPressed: () -> Unit,
    onTimerAddTimePressed: () -> Unit,
    addTimer: (workoutId: Long?, time: Int?) -> Unit,
    timerDefaultValue: Int,
) {
    val enterTransition = fadeIn()
    val exitTransition = fadeOut()

    val navController = rememberNavController()
    Scaffold(bottomBar = {
        Column() {
            if (showTimer) {
                TimerContainer {
                    Text(
                        text = timerText.toString(), modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                    )
                    TextButton(onClick = {
                        onTimerAddTimePressed()
                    }) {
                        Icon(imageVector = Icons.Outlined.Timer10, contentDescription = "add 10 seconds to timer")
                    }
                    TextButton(onClick = {
                        onTimerCancelPressed()
                    }) {
                        Icon(imageVector = Icons.Outlined.Close, contentDescription = "stop timer")
                    }
                }
            } else if (showTimerPickerButton) {
                TimerContainer {
                    val showTimerPickerDialog = remember { mutableStateOf(false) }
                    Column(modifier = Modifier.weight(1f)) {}
                    TextButton(onClick = { showTimerPickerDialog.value = true }) {
                        Text("Select timer")
                        Icon(Icons.Outlined.Timer, contentDescription = "add timer")
                    }
                    when {
                        showTimerPickerDialog.value -> {
                            SelectTimespanDialog(
                                initialValue = timerDefaultValue,
                                onConfirmValue = {
                                    addTimer(null, it)
                                },
                                onDismiss = {
                                    showTimerPickerDialog.value = false
                                }
                            )
                        }
                    }
                }
            }
            TabView(navController)
        }

    }) { contentPadding ->
        Column {
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
                activitiesGraph(navController, {
                    addTimer(it, null)
                })
                plansGraph(navController)
                statisticsGraph(navController)
                settingsGraph(navController)
            }


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

@Composable
fun TimerContainer(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
    HorizontalDivider()
}