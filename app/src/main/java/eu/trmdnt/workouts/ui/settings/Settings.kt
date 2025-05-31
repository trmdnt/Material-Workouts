package eu.trmdnt.workouts.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.trmdnt.workouts.settings.Theme
import eu.trmdnt.workouts.ui.components.SelectTimespanDialog

@Composable
fun Settings() {
    val viewModel: SettingsViewModel = hiltViewModel()

    Column {
        for (i in 1..viewModel.preferences.size - 1) {
            val pref = viewModel.preferences[i]
            when (pref) {
                is SettingsViewModel.SwitchPreferenceEntry -> {
                    val value = pref.value.collectAsStateWithLifecycle(true).value
                    SwitchPrefItem("${pref.key}", value) {
                        viewModel.onBooleanPreferenceChange(pref.key, it)
                    }
                }

                is SettingsViewModel.RadioPreferenceEntry -> {
                    val value = pref.value.collectAsStateWithLifecycle(Theme.System).value
                    RadioPrefItem("${pref.key}", value) {
                        viewModel.onEnumPreferenceChange(pref.key, it)
                    }
                }

                is SettingsViewModel.TimeSpanPreferenceEntry -> {
                    val value = pref.value.collectAsStateWithLifecycle(90).value

                    val openAlertDialog = remember { mutableStateOf(false) }
                    when {
                        openAlertDialog.value -> {
                            SelectTimespanDialog(initialValue = value, onConfirmValue = {
                                viewModel.onIntPreferenceChange(pref.key, it)
                                openAlertDialog.value = false
                            }, onDismiss = {
                                openAlertDialog.value = false
                            })
                        }
                    }
                    ButtonPrefItem("${pref.key}", "Select") {
                        openAlertDialog.value = true
                    }

                }
            }
            if (i != viewModel.preferences.lastIndex) {
                HorizontalDivider()
            }
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
fun <T : Enum<T>> RadioPrefItem(label: String, value: Enum<T>, onValueChange: (Enum<T>) -> Unit) {
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