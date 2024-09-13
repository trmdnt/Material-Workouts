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
        val selectedExerciseTemplatesIds: List<Long> = emptyList(),

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

    fun onItemPressed(id: Long) {
        // return true if event was handled
        if (uiState.value.editMode) {
            toggleSelection(id)
        } else {
            _navigateToExerciseTemplateId.value = id
        }
    }

    fun receivedNavEvent() {
        _navigateToExerciseTemplateId.value = null
    }

    fun onLongItemPressed(id: Long) {
        toggleSelection(id)
    }

    fun onDeleteButtonPressed() {
        _uiState.value = _uiState.value.copy(
            confirmBottomSheetShown = true,
            confirmBottomSheetText = "Do you really want to delete the ${_uiState.value.selectedExerciseTemplatesIds.size} selected item(s)"
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
            gymRepository.deleteExerciseTemplatesByIds(uiState.value.selectedExerciseTemplatesIds.toList())
            getOutOfEditMode()
        }
        onCancelSheetPressed()
    }

    fun onCancelButtonPressed() {
        getOutOfEditMode()
    }

    private fun getOutOfEditMode() {
        _uiState.value = _uiState.value.copy(
            selectedExerciseTemplatesIds = uiState.value.selectedExerciseTemplatesIds.toMutableList().apply {
                clear()
            }
        )
        _uiState.value = _uiState.value.copy(editMode = false)
    }

    private fun isInSelectedList(id: Long): Boolean {
        return uiState.value.selectedExerciseTemplatesIds.contains(id)
    }

    private fun addToSelectedList(id: Long) {
        if (!uiState.value.editMode) {
            _uiState.value = _uiState.value.copy(editMode = true)
        }

        _uiState.value = _uiState.value.copy(
            selectedExerciseTemplatesIds = uiState.value.selectedExerciseTemplatesIds.toMutableList().apply {
                add(id)
            }
        )
    }

    private fun removeFromSelectedList(id: Long) {

        _uiState.value = _uiState.value.copy(
            selectedExerciseTemplatesIds = uiState.value.selectedExerciseTemplatesIds.toMutableList().apply {
                remove(id)
            }
        )
        if (uiState.value.selectedExerciseTemplatesIds.isEmpty()) {
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