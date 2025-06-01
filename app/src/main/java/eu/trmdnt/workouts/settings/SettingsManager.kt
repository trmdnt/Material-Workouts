package eu.trmdnt.workouts.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.map

const val START_TIMER_ON_SET = "startTimerOnSet"
const val TIMER_DEFAULT_VALUE = "timerDefaultValue"
const val ALWAYS_SHOW_TIMER_UI = "alwaysShowTimerUi"
const val USE_THEME = "useTheme"
const val USE_DYNAMIC_COLOR = "useDynamicColor"

val startTimerOnSetPreferenceKey = booleanPreferencesKey(START_TIMER_ON_SET)
val timerDefaultValuePreferenceKey = intPreferencesKey(TIMER_DEFAULT_VALUE)
val alwaysShowTimerUiPreferenceKey = booleanPreferencesKey(ALWAYS_SHOW_TIMER_UI)
val useThemePreferenceKey = stringPreferencesKey(USE_THEME)
val useDynamicColorPreferenceKey = booleanPreferencesKey(USE_DYNAMIC_COLOR)

class SettingsManager(private val preferencesDataStore: DataStore<Preferences>) {
    fun getBooleanPreference(pref: Preferences.Key<Boolean>) = preferencesDataStore.data.map { preferences ->
        preferences[pref] ?: true
    }

    fun getThemePreference() = preferencesDataStore.data.map { preferences ->
        try {
            Theme.valueOf(preferences[useThemePreferenceKey] ?: Theme.entries[0].name)
        } catch (e: Exception) {
            Theme.entries[0]
        }
    }

    fun getIntPreference(pref: Preferences.Key<Int>) = preferencesDataStore.data.map { preferences ->
        preferences[pref] ?: 90
    }

    suspend fun writeBooleanPreference(pref: Preferences.Key<Boolean>, value: Boolean) {
        preferencesDataStore.edit { preferences ->
            preferences[pref] = value
        }
    }

    suspend fun writeIntPreference(pref: Preferences.Key<Int>, value: Int) {
        preferencesDataStore.edit { preferences ->
            preferences[pref] = value
        }
    }

    suspend fun writeThemePreference(value: Theme) {
        preferencesDataStore.edit { preferences ->
            preferences[useThemePreferenceKey] = value.name
        }
    }
}