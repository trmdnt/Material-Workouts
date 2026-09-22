package eu.trmdnt.workouts.ui.plans.workoutTemplates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.trmdnt.workouts.database.GymRepository
import eu.trmdnt.workouts.database.entities.ExerciseTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = EditPlanViewModel.EditPlanViewModelFactory::class)
class EditPlanViewModel @AssistedInject constructor(
    val gymRepository: GymRepository, @Assisted val workoutTemplateId: Long
) : ViewModel() {
    @AssistedFactory
    interface EditPlanViewModelFactory {
        fun create(workoutTemplateId: Long): EditPlanViewModel
    }

    private val _workoutTemplate =
        gymRepository.getWorkoutTemplateById(workoutTemplateId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _selectedExerciseTemplates =
        gymRepository.getAllExerciseTemplatesFromWorkoutTemplate(workoutTemplateId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _allExerciseTemplates = gymRepository.getAllExerciseTemplates().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )


    val uiState: StateFlow<UiState> = combine(
        _workoutTemplate,
        _selectedExerciseTemplates,
        _allExerciseTemplates,
    ) { workoutTemplate, selectedExercises, allExercises ->
        UiState(
            workoutTemplate?.name,
            selectedExercises ?: emptyList(),
            allExercises ?: emptyList(),

            )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = UiState()
    )

    data class UiState(
        val nameTextField: String? = null,
        val selected: List<ExerciseTemplate> = emptyList(),
        val exercises: List<ExerciseTemplate> = emptyList(),
    )

    fun onExercisePressed(exerciseTemplateId: Long) {
        if (uiState.value.selected.find { it.exerciseTemplateId == exerciseTemplateId } != null) {
            viewModelScope.launch(Dispatchers.IO) {
                gymRepository.removeExerciseFromWorkoutTemplate(
                    workoutTemplateId = workoutTemplateId, exerciseTemplateId = exerciseTemplateId
                )
            }
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                gymRepository.addExerciseToWorkoutTemplate(
                    workoutTemplateId = workoutTemplateId, exerciseTemplateId = exerciseTemplateId
                )
            }
        }
    }

    fun onNameFieldChange(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            gymRepository.updateWorkoutTemplate(_workoutTemplate.value!!.copy(name = name))
        }
    }
}