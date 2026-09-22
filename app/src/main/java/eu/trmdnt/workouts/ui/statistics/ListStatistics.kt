package eu.trmdnt.workouts.ui.statistics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import eu.trmdnt.workouts.database.entities.ExerciseTemplate
import eu.trmdnt.workouts.ui.components.ListAvailableItems
import eu.trmdnt.workouts.ui.navigation.NavEventHandler

@Composable
fun ListStatistics(navEventHandler: NavEventHandler) {
    val viewModel: ListStatisticsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.navigationEvents.collect { event ->
                navEventHandler.handle(event)
            }
        }
    }

    ListStatistics(uiState = uiState) { exercise ->
        viewModel.onExerciseTemplateClick(exercise)
    }
}

@Composable
fun ListStatistics(
    uiState: ListStatisticsViewModel.UiState,
    onExercisePressed: (ExerciseTemplate) -> Unit
) {
    ListAvailableItems(uiState.availableExercises, onExercisePressed, getName = {
        it.name
    })
}