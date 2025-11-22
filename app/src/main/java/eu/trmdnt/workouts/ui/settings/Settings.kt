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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.trmdnt.workouts.settings.Theme
import eu.trmdnt.workouts.ui.components.SelectTimespanDialog

@Composable
fun Settings() {
    val viewModel: SettingsViewModel = hiltViewModel()

    Column {
        for (i in 0..<viewModel.preferences.size) {
            when (val pref = viewModel.preferences[i]) {
                is SettingsViewModel.SwitchPreferenceEntry -> {
                    val value = pref.value.collectAsStateWithLifecycle(true).value
                    SwitchPrefItem(label = pref.label, value = value, enabled = pref.enabled) {
                        viewModel.onBooleanPreferenceChange(pref.key, it)
                    }
                }

                is SettingsViewModel.ThemePreferenceEntry -> {
                    val value = pref.value.collectAsStateWithLifecycle(Theme.System).value
                    ThemePrefItem(label = pref.label, value = value, enabled = pref.enabled) {
                        viewModel.onThemePreferenceChange(it)
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
                    ButtonPrefItem(label = pref.label, buttonLabel = "Select", enabled = pref.enabled) {
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
fun SwitchPrefItem(label: String, value: Boolean, enabled: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .alpha(if (enabled) 1.0f else 0.5f)
    ) {
        Text(text = label, modifier = Modifier.weight(1f))
        Switch(checked = value, onCheckedChange = { onCheckedChange(it) }, enabled = enabled)
    }

}

@Composable
fun ButtonPrefItem(label: String, buttonLabel: String, enabled: Boolean, onButtonPressed: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .alpha(if (enabled) 1.0f else 0.5f)
    ) {
        Text(text = label, modifier = Modifier.weight(1f))
        Button(onClick = onButtonPressed, enabled = enabled) {
            Text(buttonLabel)
        }
    }
}

@Composable
fun ThemePrefItem(label: String, value: Theme, enabled: Boolean, onValueChange: (Theme) -> Unit) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .alpha(if (enabled) 1.0f else 0.5f)
    ) {
        Text(text = "$label:")
        Column(modifier = Modifier.padding(start = 16.dp)) {
            value.javaClass.enumConstants!!.forEach { enumValue ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = enumValue.name.lowercase(), modifier = Modifier.weight(1f))
                    RadioButton(
                        selected = enumValue == value,
                        onClick = { onValueChange(enumValue) },
                        enabled = enabled
                    )
                }
            }
        }
    }
}