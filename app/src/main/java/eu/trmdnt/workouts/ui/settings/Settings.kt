package eu.trmdnt.workouts.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.trmdnt.workouts.ui.components.SelectTimespanDialog

@Composable
fun Settings() {
    val viewModel: SettingsViewModel = hiltViewModel()

    val startTimerOnSet by viewModel.startTimerOnSet.collectAsState(true)
    val timerDefaultValue by viewModel.timerDefaultValue.collectAsState(90)
    val alwaysShowTimerUi by viewModel.alwaysShowTimerUi.collectAsState(true)

    Column() {
        SettingsItemContainer {
            Text(text = "start timer after adding set", modifier = Modifier.weight(1f))
            Switch(
                checked = startTimerOnSet,
                onCheckedChange = { viewModel.onStartTimerChanged(it) }
            )
        }
        HorizontalDivider()
        SettingsItemContainer {
            Text(text = "timer default value", modifier = Modifier.weight(1f))
            val openAlertDialog = remember { mutableStateOf(false) }
            when {
                openAlertDialog.value -> {
                    SelectTimespanDialog(
                        initialValue = timerDefaultValue,
                        onConfirmValue = {
                            viewModel.onTimerDefaultValueChanged(it)
                            openAlertDialog.value = false
                        },
                        onDismiss = {
                            openAlertDialog.value = false
                        }
                    )
                }
            }
            Button(onClick = {
                openAlertDialog.value = true
            }) {
                Text("Change")
            }
        }
        HorizontalDivider()
        SettingsItemContainer {
            Text(text = "always show timer ui", modifier = Modifier.weight(1f))
            Switch(
                checked = alwaysShowTimerUi,
                onCheckedChange = { viewModel.onAlwaysShowTimerChanged(it) }
            )
        }
    }
}

@Composable
fun SettingsItemContainer(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}