package eu.trmdnt.workouts.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.map

public val startTimerOnSetPreferenceKey = booleanPreferencesKey("startTimerOnSet")
public val timerDefaultValuePreferenceKey = intPreferencesKey("timerDefaultValue")
public val alwaysShowTimerUiPreferenceKey = booleanPreferencesKey("alwaysShowTimerUi")
public val useThemePreferenceKey = stringPreferencesKey("useTheme")
public val useDynamicColorPreferenceKey = booleanPreferencesKey("useDynamicColor")

class SettingsManager(private val preferencesDataStore: DataStore<Preferences>) {


    public fun getBooleanPreference(pref: Preferences.Key<Boolean>) = preferencesDataStore.data.map { preferences ->
        preferences[pref] ?: true
    }

    public fun getThemePreference() = preferencesDataStore.data.map { preferences ->
        try {
            Theme.valueOf(preferences[useThemePreferenceKey] ?: Theme.entries[0].name)
        } catch (e: Exception) {
            Theme.entries[0]
        }
    }

    public fun <T : Enum<T>> getEnumPreference(pref: Preferences.Key<String>, enumClass: Class<T>) =
        preferencesDataStore.data.map { preferences ->
            try {
                val prefValue = preferences[pref]
                if (prefValue == null) {
                    enumClass.enumConstants!!.first()
                }
                enumClass.enumConstants!!.first { it.name == prefValue }
            } catch (e: Exception) {
                enumClass.enumConstants!!.first()
            }
        }

    public fun getIntPreference(pref: Preferences.Key<Int>) = preferencesDataStore.data.map { preferences ->
        preferences[pref] ?: 90
    }

    suspend fun writeBooleanPreference(pref: Preferences.Key<Boolean>, value: Boolean) {
        preferencesDataStore.edit { preferences ->
            preferences[pref] = value
        }
    }

    suspend fun writeEnumPreference(pref: Preferences.Key<String>, value: Enum<*>) {
        preferencesDataStore.edit { preferences ->
            preferences[pref] = value.toString()
        }
    }

    suspend fun writeIntPreference(pref: Preferences.Key<Int>, value: Int) {
        preferencesDataStore.edit { preferences ->
            preferences[pref] = value
        }
    }
}