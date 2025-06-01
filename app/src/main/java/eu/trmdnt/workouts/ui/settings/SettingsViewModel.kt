package eu.trmdnt.workouts.ui.settings

import android.content.Context
import android.util.Log
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
    interface PreferenceEntry<T2 : Any> {
        val value: Flow<T2>
        val label: String
    }

    inner class SwitchPreferenceEntry(
        val key: Preferences.Key<Boolean>,
        override val label: String,
    ) : PreferenceEntry<Boolean> {
        override val value: Flow<Boolean> = settingsManager.getBooleanPreference(key)
    }

    inner class TimeSpanPreferenceEntry(val key: Preferences.Key<Int>, override val label: String) :
        PreferenceEntry<Int> {
        override val value = settingsManager.getIntPreference(key)
    }

    inner class ThemePreferenceEntry(
        override val label: String
    ) : PreferenceEntry<Theme> {
        override val value: Flow<Theme> = settingsManager.getThemePreference()
    }

    val preferences: List<PreferenceEntry<*>> = listOf(
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
        ThemePreferenceEntry(
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

    fun onThemePreferenceChange(value: Theme) {
        Log.d("TAG", "onThemePreferenceChange: $value")
        viewModelScope.launch {
            settingsManager.writeThemePreference(value)
        }
    }
}