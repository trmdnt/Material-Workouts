package com.example.gymutil.ui.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun Settings() {
    val viewModel: SettingsViewModel = hiltViewModel()

    val startTimerOnSet by viewModel.startTimerOnSet.collectAsState(true)

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text = "start timer after adding set", modifier = Modifier.weight(1f))
        Switch(
            checked = startTimerOnSet,
            onCheckedChange = { viewModel.onStartTimerChanged(it) }
        )

    }
}