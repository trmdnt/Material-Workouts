package eu.trmdnt.workouts.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.trmdnt.workouts.R
import eu.trmdnt.workouts.database.backup.BackupManager
import eu.trmdnt.workouts.settings.Pref
import eu.trmdnt.workouts.settings.Prefs
import eu.trmdnt.workouts.settings.SettingsManager
import eu.trmdnt.workouts.settings.Theme
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
    abstract inner class PreferenceEntry<T, S>(
        open val pref: Pref<T, S>
    ) {
        val value: Flow<T> = settingsManager.getPreference(pref)
        abstract val label: String
        abstract val enabled: Boolean

        fun onValueChanged(value: T) {
            onPreferenceChange(pref, value)
        }
    }

    inner class SwitchPreferenceEntry(
        pref: Pref<Boolean, Boolean>,
        override val label: String,
        override val enabled: Boolean = true,
    ) : PreferenceEntry<Boolean, Boolean>(pref)

    inner class TimeSpanPreferenceEntry(
        pref: Pref<Int, Int>,
        override val label: String,
        override val enabled: Boolean = true
    ) : PreferenceEntry<Int, Int>(pref)

    inner class SliderPreferenceEntry(
        pref: Pref<Int, Int>,
        val lowerBound: Int,
        val upperBound: Int,
        override val label: String,
        override val enabled: Boolean = true
    ) : PreferenceEntry<Int, Int>(pref)

    inner class EnumPreferenceEntry<T : Enum<T>>(
        pref: Pref<T, String>,
        val entries: List<Pair<T, String>>,
        override val label: String, override val enabled: Boolean = true
    ) : PreferenceEntry<T, String>(pref)


    private val _restoreState: MutableStateFlow<BackupManager.RestoreResult?> = MutableStateFlow(null)
    val restoreState: StateFlow<BackupManager.RestoreResult?> = _restoreState

    val preferences: List<PreferenceEntry<*, *>> = buildList {
        add(
            TimeSpanPreferenceEntry(
                Prefs.timerDefaultValue,
                appContext.getString(R.string.timer_default_value_description)
            )
        )
        add(
            SwitchPreferenceEntry(
                Prefs.startTimerOnSet,
                appContext.getString(R.string.start_timer_on_set_added_description)
            )
        )
        add(
            SwitchPreferenceEntry(
                Prefs.alwaysShowTimerUi,
                appContext.getString(R.string.always_show_timer_description)
            )
        )
        add(
            EnumPreferenceEntry(
                Prefs.useTheme,
                Theme.entries.map { theme ->
                    Pair(
                        theme,
                        when (theme) {
                            Theme.System -> appContext.getString(R.string.system_theme)
                            Theme.Light -> appContext.getString(R.string.light_theme)
                            Theme.Dark -> appContext.getString(R.string.dark_theme)
                            Theme.Oled -> appContext.getString(R.string.oled_theme)
                        }
                    )
                },
                appContext.getString(R.string.use_theme_description)
            )
        )
        add(
            SwitchPreferenceEntry(
                Prefs.useDynamicColor,
                appContext.getString(R.string.use_dynamic_color_description),
                enabled = supportsDynamicColor()
            )
        )
        add(
            SliderPreferenceEntry(
                Prefs.defaultRepCount,
                lowerBound = 0,
                upperBound = 35,
                label = appContext.getString(R.string.default_reps_pref_description),
            )
        )
        add(
            SwitchPreferenceEntry(
                Prefs.reuseLastWeight,
                label = appContext.getString(R.string.reuse_last_used_weight_pref_description)
            )
        )
        add(
            SwitchPreferenceEntry(
                Prefs.openEditDialog,
                label = appContext.getString(R.string.open_edit_dialog_pref_description)
            )
        )
    }

    private fun <T, S> onPreferenceChange(pref: Pref<T, S>, value: T) {
        viewModelScope.launch {
            settingsManager.writePreference(pref, value)
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

