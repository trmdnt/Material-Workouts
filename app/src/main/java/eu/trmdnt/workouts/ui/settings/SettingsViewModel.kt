package eu.trmdnt.workouts.ui.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

val startTimerOnSetPreference = booleanPreferencesKey("startTimerOnSet")
val timerDefaultValuePreference = intPreferencesKey("timerDefaultValue")
val alwaysShowTimerUiPreference = booleanPreferencesKey("alwaysShowTimerUi")

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
}