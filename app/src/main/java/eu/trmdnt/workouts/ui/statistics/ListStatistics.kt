package eu.trmdnt.workouts.ui.statistics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.trmdnt.workouts.database.entities.ExerciseTemplate
import eu.trmdnt.workouts.ui.components.ListAvailableItems

@Composable
fun ListStatistics(navigateToExerciseStats: (Long) -> Unit) {
    val viewModel: ListStatisticsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    viewModel.navigateToExerciseStats.collectAsStateWithLifecycle().value?.let { id ->
        navigateToExerciseStats(id)
        viewModel.onNavigatedToExerciseStats()
    }

    ListStatistics(uiState = uiState) { exercise ->
        viewModel.onExerciseTemplateClick(exercise)
    }
}

@Composable
fun ListStatistics(uiState: ListStatisticsViewModel.UiState, onExercisePressed: (ExerciseTemplate) -> Unit) {
    ListAvailableItems(uiState.availableExercises, onExercisePressed, getName = {
        it.name
    })
}