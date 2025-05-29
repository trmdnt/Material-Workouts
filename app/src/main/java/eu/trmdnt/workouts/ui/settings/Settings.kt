package eu.trmdnt.workouts.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
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
    val theme by viewModel.useTheme.collectAsState(Theme.entries[0])

    Column {
        SwitchPrefItem("start timer after adding set", startTimerOnSet) {
            viewModel.onStartTimerChanged(it)
        }
        HorizontalDivider()
        val openAlertDialog = remember { mutableStateOf(false) }
        when {
            openAlertDialog.value -> {
                SelectTimespanDialog(initialValue = timerDefaultValue, onConfirmValue = {
                    viewModel.onTimerDefaultValueChanged(it)
                    openAlertDialog.value = false
                }, onDismiss = {
                    openAlertDialog.value = false
                })
            }
        }
        ButtonPrefItem("timer default value", "Change") {
            openAlertDialog.value = true
        }

        HorizontalDivider()
        SwitchPrefItem("always show timer ui", alwaysShowTimerUi, {
            viewModel.onAlwaysShowTimerChanged(it)
        })
        HorizontalDivider()
        RadioPrefItem("theme", theme) {
            viewModel.onThemeChanged(it)
        }
    }
}

@Composable
fun SettingsItemContainer(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        content()
    }
}

@Composable
fun SwitchPrefItem(label: String, value: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically, modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(text = label, modifier = Modifier.weight(1f))
        Switch(checked = value, onCheckedChange = { onCheckedChange(it) })
    }

}

@Composable
fun ButtonPrefItem(label: String, buttonLabel: String, onButtonPressed: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically, modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(text = label, modifier = Modifier.weight(1f))
        Button(onClick = onButtonPressed) {
            Text(buttonLabel)
        }
    }
}

@Composable
fun <T : Enum<T>> RadioPrefItem(label: String, value: T, onValueChange: (T) -> Unit) {

    Column(modifier = Modifier.padding(8.dp)) {
        Text(text = "$label:")
        Column(modifier = Modifier.padding(start = 16.dp)) {
            value.javaClass.enumConstants!!.forEach { enumValue ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = enumValue.name.lowercase(), modifier = Modifier.weight(1f))
                    RadioButton(selected = enumValue == value, onClick = { onValueChange(enumValue) })
                }
            }
        }
    }

}