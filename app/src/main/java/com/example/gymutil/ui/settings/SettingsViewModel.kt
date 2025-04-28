package com.example.gymutil.ui.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

val startTimerOnSetPreference = booleanPreferencesKey("startTimerOnSet")

@HiltViewModel
class SettingsViewModel @Inject constructor(private val preferencesDataStore: DataStore<Preferences>) : ViewModel() {

    val startTimerOnSet = preferencesDataStore.data.map { preferences ->
        preferences[startTimerOnSetPreference] ?: true
    }

    fun onStartTimerChanged(startTimerOnSetAdded: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.edit { preferences ->
                preferences[startTimerOnSetPreference] = startTimerOnSetAdded
            }
        }

    }
}