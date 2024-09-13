package com.example.gymutil.ui.plans.exercisesTemplates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymutil.database.GymRepository
import com.example.gymutil.database.entities.ExerciseTemplate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateExerciseDialogViewModel @Inject constructor(private val gymRepository: GymRepository) : ViewModel() {
    data class UiState(
        var text: String = "",
        var isError: Boolean = false,
        var exerciseTemplateId: Long? = null,
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
                //TODO fix navigating to it
                val exerciseTemplateId: Long = createExercise(name = uiState.value.text)
                _uiState.value = _uiState.value.copy(exerciseTemplateId = exerciseTemplateId)
            }
        }
    }

    private fun createExercise(name: String): Long {
        return gymRepository.insertExerciseTemplate(
            ExerciseTemplate(
                name = name,
                weight = true,
                time = false,
                reps = true,
                hidden = false,
                distance = false
            )
        )
    }
}