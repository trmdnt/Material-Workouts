package eu.trmdnt.workouts.ui.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.trmdnt.workouts.R
import eu.trmdnt.workouts.settings.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsManager: SettingsManager, @ApplicationContext private val appContext: Context
) : ViewModel() {
    interface PreferenceEntry<T1 : Any, T2 : Any> {
        val key: Preferences.Key<T1>
        val value: Flow<T2>
        val label: String
    }

    inner class SwitchPreferenceEntry(
        override val key: Preferences.Key<Boolean>,
        override val label: String,
    ) : PreferenceEntry<Boolean, Boolean> {
        override val value: Flow<Boolean> = settingsManager.getBooleanPreference(key)
    }

    inner class TimeSpanPreferenceEntry(override val key: Preferences.Key<Int>, override val label: String) :
        PreferenceEntry<Int, Int> {
        override val value = settingsManager.getIntPreference(key)

    }

    inner class RadioPreferenceEntry<T : Enum<T>>(
        override val key: Preferences.Key<String>, enumClass: Class<T>, override val label: String
    ) : PreferenceEntry<String, T> {
        override val value: Flow<T> = settingsManager.getEnumPreference(key, enumClass)
    }

    val preferences: List<PreferenceEntry<*, *>> = listOf(
        SwitchPreferenceEntry(
            startTimerOnSetPreferenceKey, appContext.getString(R.string.start_timer_on_set_added_description)
        ),
        TimeSpanPreferenceEntry(
            timerDefaultValuePreferenceKey,
            appContext.getString(R.string.timer_default_value_description)
        ),
        SwitchPreferenceEntry(
            alwaysShowTimerUiPreferenceKey,
            appContext.getString(R.string.always_show_timer_description)
        ),
        RadioPreferenceEntry(
            useThemePreferenceKey,
            Theme.System.javaClass,
            appContext.getString(R.string.use_theme_description)
        ),
        SwitchPreferenceEntry(
            useDynamicColorPreferenceKey,
            appContext.getString(R.string.use_dynamic_color_description)
        )
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