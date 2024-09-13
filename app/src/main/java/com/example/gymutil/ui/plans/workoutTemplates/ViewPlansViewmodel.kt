package com.example.gymutil.ui.plans.workoutTemplates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymutil.database.GymRepository
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
        val selectedWorkoutTemplateIds: List<Long> = emptyList<Long>(),
        val confirmBottomSheetShown: Boolean = false,
        val confirmBottomSheetText: String = ""
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    val workoutTemplates = gymRepository.getAllWorkoutTemplates()

    fun onItemPressed(id: Long): Boolean {
        // return true if event was handled
        if (uiState.value.editMode) {
            toggleSelection(id)
            return true
        } else {
            return false
        }
    }

    fun onLongItemPressed(id: Long) {
        toggleSelection(id)
    }

    fun onDeleteButtonPressed() {
        _uiState.value = _uiState.value.copy(
            confirmBottomSheetShown = true,
            confirmBottomSheetText = "Do you really want to delete the ${_uiState.value.selectedWorkoutTemplateIds.size} selected item(s)"
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
            gymRepository.deleteWorkoutTemplateWithIds(uiState.value.selectedWorkoutTemplateIds.toList())
            getOutOfEditMode()
        }
        onCancelSheetPressed()
    }

    fun onCancelButtonPressed() {
        getOutOfEditMode()
    }

    private fun getOutOfEditMode() {
        _uiState.value = _uiState.value.copy(
            selectedWorkoutTemplateIds = uiState.value.selectedWorkoutTemplateIds.toMutableList().apply {
                clear()
            }
        )
        _uiState.value = _uiState.value.copy(editMode = false)
    }

    private fun isInSelectedList(id: Long): Boolean {
        return uiState.value.selectedWorkoutTemplateIds.contains(id)
    }

    private fun addToSelectedList(id: Long) {
        if (!uiState.value.editMode) {
            _uiState.value = _uiState.value.copy(editMode = true)
        }

        _uiState.value = _uiState.value.copy(
            selectedWorkoutTemplateIds = uiState.value.selectedWorkoutTemplateIds.toMutableList().apply {
                add(id)
            }
        )
    }

    private fun removeFromSelectedList(id: Long) {

        _uiState.value = _uiState.value.copy(
            selectedWorkoutTemplateIds = uiState.value.selectedWorkoutTemplateIds.toMutableList().apply {
                remove(id)
            }
        )
        if (uiState.value.selectedWorkoutTemplateIds.isEmpty()) {
            _uiState.value = _uiState.value.copy(editMode = false)
        }
    }

    private fun toggleSelection(id: Long) {
        if (isInSelectedList(id)) {
            removeFromSelectedList(id)
        } else {
            addToSelectedList(id)
        }
    }
}