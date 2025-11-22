package eu.trmdnt.workouts.ui.statistics

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.trmdnt.workouts.database.GymRepository
import eu.trmdnt.workouts.database.entities.ExerciseTemplate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ListStatisticsViewModel @Inject constructor(gymRepository: GymRepository) : ViewModel() {
    data class UiState(
        val availableExercises: List<ExerciseTemplate> = emptyList(),
    )

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private val _navigateToExerciseStats = MutableStateFlow<Long?>(null)
    val navigateToExerciseStats: StateFlow<Long?> = _navigateToExerciseStats

    init {
        gymRepository.getAllExerciseTemplates().observeForever { data ->
            _uiState.value = _uiState.value.copy(availableExercises = data)
        }
    }

    fun onExerciseTemplateClick(exerciseTemplate: ExerciseTemplate) {
        _navigateToExerciseStats.value = exerciseTemplate.exerciseTemplateId
    }

    fun onNavigatedToExerciseStats() {
        _navigateToExerciseStats.value = null
    }
}