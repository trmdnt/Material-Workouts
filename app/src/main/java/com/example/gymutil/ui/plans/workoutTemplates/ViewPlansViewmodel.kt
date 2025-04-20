package com.example.gymutil.ui.plans.workoutTemplates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymutil.database.GymRepository
import com.example.gymutil.database.entities.WorkoutTemplate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewPlansViewmodel @Inject constructor(private val gymRepository: GymRepository) : ViewModel() {
    data class UiState(
        var editMode: Boolean = false,
        var selectAll: Boolean = false,
        val selectedWorkoutTemplates: List<WorkoutTemplate> = emptyList(),
        val workoutTemplates: List<WorkoutTemplate> = emptyList(),
        val confirmBottomSheetShown: Boolean = false,
        val confirmBottomSheetText: String = ""
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private val _navigateToWorkoutTemplateId = MutableStateFlow<Long?>(null)
    val navigateToWorkoutTemplateId: StateFlow<Long?> = _navigateToWorkoutTemplateId

    init {
        gymRepository.getAllWorkoutTemplates().observeForever { workoutTemplates ->
            _uiState.value = _uiState.value.copy(
                workoutTemplates = workoutTemplates,
            )
        }
    }

    fun receivedNavEvent() {
        _navigateToWorkoutTemplateId.value = null
    }

    fun onItemPressed(workoutTemplate: WorkoutTemplate) {
        // return true if event was handled
        if (uiState.value.editMode) {
            toggleSelection(workoutTemplate)
        } else {
            _navigateToWorkoutTemplateId.value = workoutTemplate.workoutTemplateId
        }
    }

    fun onLongItemPressed(workoutTemplate: WorkoutTemplate) {
        toggleSelection(workoutTemplate)
    }

    fun onDeleteButtonPressed() {
        _uiState.value = _uiState.value.copy(
            confirmBottomSheetShown = true,
            confirmBottomSheetText = "Do you really want to delete the ${_uiState.value.selectedWorkoutTemplates.size} selected item(s)"
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
            gymRepository.deleteWorkoutTemplates(uiState.value.selectedWorkoutTemplates)
            getOutOfEditMode()
        }
        onCancelSheetPressed()
    }

    fun onCancelButtonPressed() {
        getOutOfEditMode()
    }

    private fun getOutOfEditMode() {
        _uiState.value = _uiState.value.copy(
            selectedWorkoutTemplates = uiState.value.selectedWorkoutTemplates.toMutableList().apply {
                clear()
            }
        )
        _uiState.value = _uiState.value.copy(editMode = false)
    }

    private fun isInSelectedList(workoutTemplate: WorkoutTemplate): Boolean {
        return uiState.value.selectedWorkoutTemplates.contains(workoutTemplate)
    }

    private fun addToSelectedList(workoutTemplate: WorkoutTemplate) {
        if (!uiState.value.editMode) {
            _uiState.value = _uiState.value.copy(editMode = true)
        }

        _uiState.value = _uiState.value.copy(
            selectedWorkoutTemplates = uiState.value.selectedWorkoutTemplates.toMutableList().apply {
                add(workoutTemplate)
            }
        )
    }

    private fun removeFromSelectedList(workoutTemplate: WorkoutTemplate) {
        _uiState.value = _uiState.value.copy(
            selectedWorkoutTemplates = uiState.value.selectedWorkoutTemplates.toMutableList().apply {
                remove(workoutTemplate)
            }
        )
        if (uiState.value.selectedWorkoutTemplates.isEmpty()) {
            _uiState.value = _uiState.value.copy(editMode = false)
        }
    }

    private fun toggleSelection(workoutTemplate: WorkoutTemplate) {
        if (isInSelectedList(workoutTemplate)) {
            removeFromSelectedList(workoutTemplate)
        } else {
            addToSelectedList(workoutTemplate)
        }
    }
}