package eu.trmdnt.workouts.ui.plans.exercisesTemplates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.trmdnt.workouts.database.GymRepository
import eu.trmdnt.workouts.database.entities.ExerciseTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
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
        val confirmDialogShow: Boolean = false,
        val confirmDialogText: String = "",
        val createExerciseDialogShown: Boolean = false,
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
            confirmDialogShow = true,
            confirmDialogText = "Do you really want to delete the ${_uiState.value.selectedExerciseTemplates.size} selected item(s)"
        )
    }

    fun onCancelDialogPressed() {
        _uiState.value = _uiState.value.copy(
            confirmDialogShow = false,
            confirmDialogText = ""
        )
    }

    fun onConfirmButtonPressed() {
        viewModelScope.launch(Dispatchers.IO) {
            gymRepository.deleteExerciseTemplates(_uiState.value.selectedExerciseTemplates)
            getOutOfEditMode()
        }
        onCancelDialogPressed()
    }

    fun onCancelselectionPressed() {
        getOutOfEditMode()
    }

    fun onCreateExerciseButtonPressed() {
        _uiState.value = _uiState.value.copy(
            createExerciseDialogShown = true,
        )
    }

    fun onCreateExerciseDialogDismissed() {
        _uiState.value = _uiState.value.copy(
            createExerciseDialogShown = false,
        )
    }

    fun onCreateExercise(name: String) {
        _uiState.value = _uiState.value.copy(
            createExerciseDialogShown = false,
        )
        viewModelScope.launch(IO) {
            val template = ExerciseTemplate(name = name)
            val id = gymRepository.insertExerciseTemplate(template)
            _navigateToExerciseTemplateId.value = id
        }
    }

    private fun getOutOfEditMode() {
        _uiState.value = _uiState.value.copy(
            selectedExerciseTemplates = uiState.value.selectedExerciseTemplates.toMutableList().apply {
                clear()
            },
            editMode = false
        )
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