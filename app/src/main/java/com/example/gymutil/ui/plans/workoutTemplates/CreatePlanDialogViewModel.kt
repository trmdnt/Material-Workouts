package com.example.gymutil.ui.plans.workoutTemplates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymutil.database.GymRepository
import com.example.gymutil.database.entities.WorkoutTemplate
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = CreatePlanDialogViewModel.CreatePlanDialogViewModelFactory::class)
class CreatePlanDialogViewModel @AssistedInject constructor(
    private val gymRepository: GymRepository,
    @Assisted val onCreateWorkoutTemplate: (id: Long) -> Unit
) : ViewModel() {
    data class UiState(
        var text: String = "",
        var isError: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())

    var uiState: StateFlow<UiState> = _uiState

    fun textEdit(text: String) {
        _uiState.value = _uiState.value.copy(text = text)
    }

    fun createButtonPressed() {
        if (uiState.value.text.isBlank()) {
            _uiState.value = _uiState.value.copy(isError = true)
        } else {
            viewModelScope.launch(IO) {
                val workoutTemplateId: Long = createWorkoutTemplate(name = uiState.value.text)
                viewModelScope.launch {
                    onCreateWorkoutTemplate(workoutTemplateId)
                }

            }

        }
    }

    private fun createWorkoutTemplate(name: String): Long {
        return gymRepository.insertWorkoutTemplate(WorkoutTemplate(name = name))
    }

    @AssistedFactory
    interface CreatePlanDialogViewModelFactory {
        fun create(onCreateWorkoutTemplate: (id: Long) -> Unit): CreatePlanDialogViewModel
    }
}