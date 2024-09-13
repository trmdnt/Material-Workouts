package com.example.gymutil.ui.plans.workoutTemplates

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymutil.database.GymRepository
import com.example.gymutil.database.entities.ExerciseTemplate
import com.example.gymutil.database.entities.WorkoutTemplate
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = EditPlanViewModel.EditPlanViewModelFactory::class)
class EditPlanViewModel @AssistedInject constructor(
    val gymRepository: GymRepository, @Assisted val workoutTemplateId: Long
) : ViewModel() {
    @AssistedFactory
    interface EditPlanViewModelFactory {
        fun create(workoutTemplateId: Long): EditPlanViewModel
    }

    val workoutTemplate: LiveData<WorkoutTemplate> = gymRepository.getWorkoutTemplateByIdLive(workoutTemplateId)
    val selected = gymRepository.getAllExerciseTemplatesFromWorkoutTemplate(workoutTemplateId)
    val allExerciseTemplates = gymRepository.getAllExerciseTemplates()

    private val _uiState = MutableStateFlow(UiState(workoutTemplateId.toString(), emptyList()))
    val uiState: StateFlow<UiState> = _uiState

    init {
        workoutTemplate.observeForever { template ->
            template?.let {
                _uiState.value = _uiState.value.copy(
                    nameTextField = it.name, saveNameButtonEnabled = false
                )
            }
        }
        selected.observeForever { exercises ->
            exercises?.let {
                println("selected: $it")
                _uiState.value = _uiState.value.copy(
                    selected = it
                )
            }
        }
        allExerciseTemplates.observeForever { exercises ->
            exercises?.let {
                println("exercises: $it")
                _uiState.value = _uiState.value.copy(
                    exercises = it
                )
            }
        }
    }

    data class UiState(
        val nameTextField: String = "empty name???",
        val selected: List<ExerciseTemplate> = emptyList(),
        val exercises: List<ExerciseTemplate> = emptyList(),
        val saveNameButtonEnabled: Boolean = false
    )

    fun onExercisePressed(exerciseTemplateId: Long) {
        if (uiState.value.selected.find { it -> it.exerciseTemplateId == exerciseTemplateId } != null) {
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
        _uiState.value = _uiState.value.copy(
            nameTextField = name, saveNameButtonEnabled = true
        )
    }

    fun onSaveNameButtonClick() {
        viewModelScope.launch(Dispatchers.IO) {
            gymRepository.updateWorkoutTemplate(workoutTemplate.value!!.copy(name = _uiState.value.nameTextField))
        }
    }
}