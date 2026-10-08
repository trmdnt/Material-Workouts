package eu.trmdnt.workouts.ui.settings

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.trmdnt.workouts.R
import eu.trmdnt.workouts.database.backup.BackupManager
import eu.trmdnt.workouts.settings.*
import eu.trmdnt.workouts.ui.theme.supportsDynamicColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsManager: SettingsManager,
    @ApplicationContext private val appContext: Context,
    private val backupManager: BackupManager
) : ViewModel() {
    interface PreferenceEntry<T : Any> {
        val value: Flow<T>
        val label: String
        val enabled: Boolean
    }

    inner class SwitchPreferenceEntry(
        val key: Preferences.Key<Boolean>,
        override val label: String,
        override val enabled: Boolean = true,
    ) : PreferenceEntry<Boolean> {
        override val value: Flow<Boolean> = settingsManager.getBooleanPreference(key)
    }

    inner class TimeSpanPreferenceEntry(
        val key: Preferences.Key<Int>,
        override val label: String,
        override val enabled: Boolean = true
    ) : PreferenceEntry<Int> {
        override val value = settingsManager.getIntPreference(key)
    }

    inner class SliderPreferenceEntry(
        val key: Preferences.Key<Int>,
        val lowerBound: Int,
        val upperBound: Int,
        override val label: String,
        override val enabled: Boolean = true
    ) : PreferenceEntry<Int> {
        override val value = settingsManager.getIntPreference(key)
    }

    inner class ThemePreferenceEntry(
        override val label: String, override val enabled: Boolean = true
    ) : PreferenceEntry<Theme> {
        override val value: Flow<Theme> = settingsManager.getThemePreference()
    }


    private val _restoreState: MutableStateFlow<BackupManager.RestoreResult?> = MutableStateFlow(null)
    val restoreState: StateFlow<BackupManager.RestoreResult?> = _restoreState

    val preferences: List<PreferenceEntry<*>> = buildList {
        add(
            TimeSpanPreferenceEntry(
                timerDefaultValuePreferenceKey,
                appContext.getString(R.string.timer_default_value_description)
            )
        )
        add(
            SwitchPreferenceEntry(
                startTimerOnSetPreferenceKey,
                appContext.getString(R.string.start_timer_on_set_added_description)
            )
        )
        add(
            SwitchPreferenceEntry(
                alwaysShowTimerUiPreferenceKey,
                appContext.getString(R.string.always_show_timer_description)
            )
        )
        add(
            ThemePreferenceEntry(
                appContext.getString(R.string.use_theme_description)
            )
        )
        add(
            SwitchPreferenceEntry(
                useDynamicColorPreferenceKey,
                appContext.getString(R.string.use_dynamic_color_description),
                enabled = supportsDynamicColor()
            )
        )
        add(
            SliderPreferenceEntry(
                defaultRepCountPreferenceKey,
                lowerBound = 0,
                upperBound = 35,
                label = appContext.getString(R.string.default_reps_pref_description),
            )
        )
        add(
            SwitchPreferenceEntry(
                reuseLastWeightPreferenceKey,
                label = appContext.getString(R.string.reuse_last_used_weight_pref_description)
            )
        )
        add(
            SwitchPreferenceEntry(
                openEditDialogPreferenceKey,
                label = appContext.getString(R.string.open_edit_dialog_pref_description)
            )
        )
    }

    fun onBooleanPreferenceChange(key: Preferences.Key<Boolean>, value: Boolean) {
        viewModelScope.launch {
            settingsManager.writeBooleanPreference(key, value)
        }
    }

    fun onIntPreferenceChange(key: Preferences.Key<Int>, value: Int) {
        viewModelScope.launch {
            settingsManager.writeIntPreference(key, value)
        }
    }

    fun onThemePreferenceChange(value: Theme) {
        Log.d("TAG", "onThemePreferenceChange: $value")
        viewModelScope.launch {
            settingsManager.writeThemePreference(value)
        }
    }

    fun onBackupFolderPicked(uri: Uri?) {
        uri?.let {
            viewModelScope.launch {
                backupManager.backup(uri)
            }

        }
    }

    fun onRestoreFilePicked(uri: Uri?) {
        uri?.let {
            viewModelScope.launch {
                _restoreState.value = backupManager.restore(uri)
            }

        }
    }
}

