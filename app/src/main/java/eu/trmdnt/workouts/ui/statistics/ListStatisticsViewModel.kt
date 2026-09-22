package eu.trmdnt.workouts.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.trmdnt.workouts.database.GymRepository
import eu.trmdnt.workouts.database.entities.ExerciseTemplate
import eu.trmdnt.workouts.ui.navigation.NavEvent
import eu.trmdnt.workouts.ui.navigation.Screen
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListStatisticsViewModel @Inject constructor(gymRepository: GymRepository) : ViewModel() {
    data class UiState(
        val availableExercises: List<ExerciseTemplate> = emptyList(),
    )

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private val _navigationEvents = MutableSharedFlow<NavEvent>()
    val navigationEvents: SharedFlow<NavEvent> = _navigationEvents

    init {
        viewModelScope.launch {
            gymRepository.getAllExerciseTemplates().collect { data ->
                _uiState.value = _uiState.value.copy(availableExercises = data)
            }
        }
    }

    fun onExerciseTemplateClick(exerciseTemplate: ExerciseTemplate) {
        viewModelScope.launch {
            _navigationEvents.emit(
                NavEvent.Destination(
                    Screen.Statistics.ViewWeightPerRep(
                        exerciseTemplate.exerciseTemplateId
                    )
                )
            )
        }
    }
}