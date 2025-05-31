package eu.trmdnt.workouts.ui.settings

import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.trmdnt.workouts.settings.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SettingsViewModel @Inject constructor(private val settingsManager: SettingsManager) : ViewModel() {
    interface PreferenceEntry<T1 : Any, T2 : Any> {
        val key: Preferences.Key<T1>
        val value: Flow<T2>
    }

    inner class SwitchPreferenceEntry(
        override val key: Preferences.Key<Boolean>,

        ) : PreferenceEntry<Boolean, Boolean> {
        override val value: Flow<Boolean> = settingsManager.getBooleanPreference(key)
    }

    inner class TimeSpanPreferenceEntry(override val key: Preferences.Key<Int>) : PreferenceEntry<Int, Int> {
        override val value = settingsManager.getIntPreference(key)

    }

    inner class RadioPreferenceEntry<T : Enum<T>>(
        override val key: Preferences.Key<String>,
        enumClass: Class<T>,
    ) : PreferenceEntry<String, T> {
        override val value: Flow<T> = settingsManager.getEnumPreference(key, enumClass)
    }

    val preferences: List<PreferenceEntry<*, *>> = listOf(
        SwitchPreferenceEntry(startTimerOnSetPreferenceKey),
        TimeSpanPreferenceEntry(timerDefaultValuePreferenceKey),
        SwitchPreferenceEntry(alwaysShowTimerUiPreferenceKey),
        RadioPreferenceEntry(useThemePreferenceKey, Theme.System.javaClass),
        SwitchPreferenceEntry(useDynamicColorPreferenceKey)
    )

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

    fun onEnumPreferenceChange(key: Preferences.Key<String>, value: Enum<*>) {
        viewModelScope.launch {
            settingsManager.writeEnumPreference(key, value)
        }
    }
}