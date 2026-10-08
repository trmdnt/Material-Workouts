package eu.trmdnt.workouts.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private const val START_TIMER_ON_SET = "startTimerOnSet"
private const val TIMER_DEFAULT_VALUE = "timerDefaultValue"
private const val ALWAYS_SHOW_TIMER_UI = "alwaysShowTimerUi"
private const val USE_THEME = "useTheme"
private const val USE_DYNAMIC_COLOR = "useDynamicColor"
private const val DEFAULT_REP_COUNT = "defaultRepCount"
private const val REUSE_LAST_WEIGHT = "reuseLastWeight"
private const val OPEN_EDIT_DIALOG = "openEditDialog"

object Prefs {
    val startTimerOnSet = pref(booleanPreferencesKey(START_TIMER_ON_SET), true)
    val timerDefaultValue = pref(intPreferencesKey(TIMER_DEFAULT_VALUE), 90)
    val alwaysShowTimerUi = pref(booleanPreferencesKey(ALWAYS_SHOW_TIMER_UI), true)
    val useTheme = enumPref(stringPreferencesKey(USE_THEME), Theme.entries[0])
    val useDynamicColor = pref(booleanPreferencesKey(USE_DYNAMIC_COLOR), true)
    val defaultRepCount = pref(intPreferencesKey(DEFAULT_REP_COUNT), 12)
    val reuseLastWeight = pref(booleanPreferencesKey(REUSE_LAST_WEIGHT), true)
    val openEditDialog = pref(booleanPreferencesKey(OPEN_EDIT_DIALOG), true)
}

class Pref<T, S>(val key: Preferences.Key<S>, val default: T, val toStore: (T) -> S, val fromStored: (S) -> T?)

fun <T> pref(key: Preferences.Key<T>, default: T) = Pref(key, default, toStore = { it }, fromStored = { it })
inline fun <reified E : Enum<E>> enumPref(key: Preferences.Key<String>, default: E) =
    Pref(
        key = key,
        default = default,
        toStore = { it.name },
        fromStored = { stored ->
            enumValues<E>().firstOrNull { it.name == stored }
        }
    )

class SettingsManager(private val preferencesDataStore: DataStore<Preferences>) {
    fun <T, S> getPreference(pref: Pref<T, S>) =
        preferencesDataStore.data.map { preferences ->
            preferences[pref.key].let {
                if (it == null) {
                    pref.default
                } else {
                    pref.fromStored(it) ?: pref.default
                }
            }
        }.distinctUntilChanged()

    suspend fun <T, S> writePreference(pref: Pref<T, S>, value: T) {
        preferencesDataStore.edit { preferences ->
            preferences[pref.key] = pref.toStore(value)
        }
    }
}