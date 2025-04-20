package com.example.gymutil.ui.plans.exercisesTemplates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymutil.database.GymRepository
import com.example.gymutil.database.entities.ExerciseTemplate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewExerciseTemplatesViewModel @Inject constructor(private val gymRepository: GymRepository) : ViewModel() {
    data class UiState(
        var editMode: Boolean = false,
        var selectAll: Boolean = false,
        val exerciseTemplates: List<ExerciseTemplate> = emptyList(),
        val selectedExerciseTemplates: List<ExerciseTemplate> = emptyList(),

        val confirmBottomSheetShown: Boolean = false,
        val confirmBottomSheetText: String = ""
    )

    init {
        gymRepository.getAllExerciseTemplates().observeForever { exerciseTemplates ->
            exerciseTemplates.let {
                _uiState.value = _uiState.value.copy(exerciseTemplates = it)
            }
        }
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private val _navigateToExerciseTemplateId = MutableStateFlow<Long?>(null)
    val navigateToExerciseTemplateId: StateFlow<Long?> = _navigateToExerciseTemplateId

    fun onItemPressed(exerciseTemplate: ExerciseTemplate) {
        // return true if event was handled
        if (uiState.value.editMode) {
            toggleSelection(exerciseTemplate)
        } else {
            _navigateToExerciseTemplateId.value = exerciseTemplate.exerciseTemplateId
        }
    }

    fun receivedNavEvent() {
        _navigateToExerciseTemplateId.value = null
    }

    fun onLongItemPressed(exerciseTemplate: ExerciseTemplate) {
        toggleSelection(exerciseTemplate)
    }

    fun onDeleteButtonPressed() {
        _uiState.value = _uiState.value.copy(
            confirmBottomSheetShown = true,
            confirmBottomSheetText = "Do you really want to delete the ${_uiState.value.selectedExerciseTemplates.size} selected item(s)"
        )
    }

    fun onCancelSheetPressed() {
        _uiState.value = _uiState.value.copy(
            confirmBottomSheetShown = false,
            confirmBottomSheetText = ""
        )
    }

    fun onConfirmButtonPressed() {
        viewModelScope.launch(Dispatchers.IO) {
            gymRepository.deleteExerciseTemplates(_uiState.value.selectedExerciseTemplates)
            getOutOfEditMode()
        }
        onCancelSheetPressed()
    }

    fun onCancelButtonPressed() {
        getOutOfEditMode()
    }

    private fun getOutOfEditMode() {
        _uiState.value = _uiState.value.copy(
            selectedExerciseTemplates = uiState.value.selectedExerciseTemplates.toMutableList().apply {
                clear()
            }
        )
        _uiState.value = _uiState.value.copy(editMode = false)
    }

    private fun isInSelectedList(exerciseTemplate: ExerciseTemplate): Boolean {
        return uiState.value.selectedExerciseTemplates.contains(exerciseTemplate)
    }

    private fun addToSelectedList(exerciseTemplate: ExerciseTemplate) {
        if (!uiState.value.editMode) {
            _uiState.value = _uiState.value.copy(editMode = true)
        }

        _uiState.value = _uiState.value.copy(
            selectedExerciseTemplates = uiState.value.selectedExerciseTemplates.toMutableList().apply {
                add(exerciseTemplate)
            }
        )
    }

    private fun removeFromSelectedList(exerciseTemplate: ExerciseTemplate) {

        _uiState.value = _uiState.value.copy(
            selectedExerciseTemplates = uiState.value.selectedExerciseTemplates.toMutableList().apply {
                remove(exerciseTemplate)
            }
        )
        if (uiState.value.selectedExerciseTemplates.isEmpty()) {
            _uiState.value = _uiState.value.copy(editMode = false)
        }
    }

    private fun toggleSelection(exerciseTemplate: ExerciseTemplate) {
        if (isInSelectedList(exerciseTemplate)) {
            removeFromSelectedList(exerciseTemplate)
        } else {
            addToSelectedList(exerciseTemplate)
        }
    }
}