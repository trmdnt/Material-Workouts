package eu.trmdnt.workouts.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.trmdnt.workouts.R
import eu.trmdnt.workouts.database.backup.BackupManager
import eu.trmdnt.workouts.ui.components.SelectTimespanDialog
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun Settings() {
    val viewModel: SettingsViewModel = hiltViewModel()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    )
    { contentPadding ->
        Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(contentPadding)) {
            for ((i, element) in viewModel.preferences.withIndex()) {
                when (val pref = element) {
                    is SettingsViewModel.SwitchPreferenceEntry -> {
                        val value = pref.value.collectAsStateWithLifecycle(true).value
                        SwitchPrefItem(label = pref.label, value = value, enabled = pref.enabled) {
                            pref.onValueChanged(it)
                        }
                    }

                    is SettingsViewModel.EnumPreferenceEntry -> {
                        EnumEntryRow(pref)
                    }

                    is SettingsViewModel.TimeSpanPreferenceEntry -> {
                        val value = pref.value.collectAsStateWithLifecycle(90).value

                        val openAlertDialog = remember { mutableStateOf(false) }
                        when {
                            openAlertDialog.value -> {
                                SelectTimespanDialog(initialValue = value, onConfirmValue = {
                                    pref.onValueChanged(it)
                                    openAlertDialog.value = false
                                }, onDismiss = {
                                    openAlertDialog.value = false
                                })
                            }
                        }
                        ButtonPrefItem(
                            label = pref.label,
                            buttonLabel = stringResource(R.string.select),
                            enabled = pref.enabled
                        ) {
                            openAlertDialog.value = true
                        }

                    }

                    is SettingsViewModel.SliderPreferenceEntry -> {
                        val value = pref.value.collectAsStateWithLifecycle(12).value
                        SliderPrefItem(
                            label = pref.label,
                            value = value,
                            enabled = pref.enabled,
                            onValueChange = { pref.onValueChanged(it) },
                            lowerBound = pref.lowerBound,
                            upperBound = pref.upperBound
                        )
                    }
                }
                if (i != viewModel.preferences.lastIndex) {
                    HorizontalDivider()
                }
            }

            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.OpenDocumentTree()
            ) { uri -> viewModel.onBackupFolderPicked(uri) }

            HorizontalDivider()

            ButtonPrefItem(
                stringResource(R.string.backup_db_to_external_storage),
                stringResource(R.string.choose_folder), true, {
                    launcher.launch(null)
                })

            val launcher2 = rememberLauncherForActivityResult(
                ActivityResultContracts.OpenDocument()
            ) { uri -> viewModel.onRestoreFilePicked(uri) }

            HorizontalDivider()

            ButtonPrefItem(
                stringResource(R.string.restore_db_from_external_storage),
                stringResource(R.string.choose_file), true, {
                    launcher2.launch(arrayOf("*/*"))
                })

            val restoreResult by viewModel.restoreState.collectAsStateWithLifecycle()

            val successMessage = stringResource(R.string.db_will_be_restored_during_the_next_launch)
            val fileErrorMessage = stringResource(R.string.not_a_valid_file)
            val otherErrorMessage = stringResource(R.string.could_not_restore_file)

            LaunchedEffect(restoreResult) {
                restoreResult?.let {
                    scope.launch {
                        val message = when (it) {
                            BackupManager.RestoreResult.SUCCESS -> successMessage
                            BackupManager.RestoreResult.BAD_FILE -> fileErrorMessage
                            BackupManager.RestoreResult.OTHER -> otherErrorMessage
                        }
                        snackbarHostState.showSnackbar(message = message)
                    }
                }
            }
        }
    }
}

@Composable
private fun <E : Enum<E>> EnumEntryRow(
    entry: SettingsViewModel.EnumPreferenceEntry<E>,
) {
    val value by entry.value.collectAsStateWithLifecycle(entry.pref.default)
    EnumPrefItem(
        label = entry.label,
        value = value,
        enabled = entry.enabled,
        entries = entry.entries,
    ) { entry.onValueChanged(it) }
}

@Composable
fun SwitchPrefItem(
    label: String,
    value: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
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
fun ButtonPrefItem(
    label: String,
    buttonLabel: String,
    enabled: Boolean,
    onButtonPressed: () -> Unit
) {
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
fun <E : Enum<E>> EnumPrefItem(
    label: String,
    value: E,
    entries: List<Pair<E, String>>,
    enabled: Boolean,
    onValueChange: (E) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .alpha(if (enabled) 1.0f else 0.5f)
    ) {
        Text(text = "$label:")
        Column(modifier = Modifier.padding(start = 16.dp)) {
            entries.forEach { (enumValue, text) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = text, modifier = Modifier.weight(1f))
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

@Composable
fun SliderPrefItem(
    label: String,
    value: Int,
    enabled: Boolean,
    onValueChange: (Int) -> Unit,
    lowerBound: Int = 0,
    upperBound: Int = 35
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .alpha(if (enabled) 1.0f else 0.5f),
    ) {
        Text(text = "$label:")
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val sliderState = rememberSliderState(
                value = value.toFloat(),
                steps = upperBound - lowerBound - 1,
                trackRange = lowerBound.toFloat()..upperBound.toFloat()
            )

            Slider(
                state = sliderState,
                onValueChange = {
                    sliderState.value = it
                    onValueChange(it.roundToInt())
                },
                modifier = Modifier.weight(1f)
            )

            Text(value.toString())
        }
    }
}