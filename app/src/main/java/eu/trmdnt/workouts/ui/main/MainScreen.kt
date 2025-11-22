package eu.trmdnt.workouts.ui.main

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Timer10
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import eu.trmdnt.workouts.ui.components.SelectTimespanDialog
import eu.trmdnt.workouts.ui.components.TimerContainer
import eu.trmdnt.workouts.ui.navigation.*

@OptIn(ExperimentalLayoutApi::class)
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
        Column {
            if (showTimer) {
                TimerContainer {
                    Text(
                        text = timerText.toString(), modifier = Modifier.Companion
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
                    Column(modifier = Modifier.Companion.weight(1f)) {}
                    TextButton(onClick = { showTimerPickerDialog.value = true }) {
                        Text("Select timer")
                        Icon(Icons.Outlined.Timer, contentDescription = "add timer")
                    }
                    when {
                        showTimerPickerDialog.value -> {
                            SelectTimespanDialog(initialValue = timerDefaultValue, onConfirmValue = {
                                addTimer(null, it)
                            }, onDismiss = {
                                showTimerPickerDialog.value = false
                            })
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
                modifier = Modifier.Companion
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
}