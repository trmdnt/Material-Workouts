package eu.trmdnt.workouts.ui.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

val startTimerOnSetPreference = booleanPreferencesKey("startTimerOnSet")
val timerDefaultValuePreference = intPreferencesKey("timerDefaultValue")
val alwaysShowTimerUiPreference = booleanPreferencesKey("alwaysShowTimerUi")
val useThemePreference = stringPreferencesKey("useTheme")

enum class Theme {
    System,
    Light,
    Dark,
    Oled
}

@HiltViewModel
class SettingsViewModel @Inject constructor(private val preferencesDataStore: DataStore<Preferences>) : ViewModel() {

    val startTimerOnSet = preferencesDataStore.data.map { preferences ->
        preferences[startTimerOnSetPreference] ?: true
    }
    val timerDefaultValue = preferencesDataStore.data.map { preferences ->
        preferences[timerDefaultValuePreference] ?: 90
    }
    val alwaysShowTimerUi = preferencesDataStore.data.map { preferences ->
        preferences[alwaysShowTimerUiPreference] ?: true
    }

    val useTheme = preferencesDataStore.data.map { preferences ->
        try {
            Theme.valueOf(preferences[useThemePreference] ?: Theme.entries[0].name)
        } catch (e: Exception) {
            Theme.entries[0]
        }

    }

    fun onStartTimerChanged(startTimerOnSetAdded: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.edit { preferences ->
                preferences[startTimerOnSetPreference] = startTimerOnSetAdded
            }
        }
    }

    fun onTimerDefaultValueChanged(timerDefaultValue: Int) {
        viewModelScope.launch {
            preferencesDataStore.edit { preferences ->
                preferences[timerDefaultValuePreference] = timerDefaultValue
            }
        }
    }

    fun onAlwaysShowTimerChanged(alwaysShowTimer: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.edit { preferences ->
                preferences[alwaysShowTimerUiPreference] = alwaysShowTimer
            }
        }
    }

    fun onThemeChanged(theme: Theme) {
        viewModelScope.launch {
            preferencesDataStore.edit { preferences ->
                preferences[useThemePreference] = theme.toString()
            }
        }
    }
}