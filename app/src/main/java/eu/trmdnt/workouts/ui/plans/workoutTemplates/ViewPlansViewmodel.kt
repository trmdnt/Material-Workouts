package eu.trmdnt.workouts.ui.plans.workoutTemplates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.trmdnt.workouts.database.GymRepository
import eu.trmdnt.workouts.database.entities.ExerciseTemplate
import eu.trmdnt.workouts.database.entities.WorkoutTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
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
        val confirmDialogShown: Boolean = false,
        val confirmDialogText: String = "",
        val createWorkoutTemplateDialogShown: Boolean = false,
        val createExerciseDialogShown: Boolean = false,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private val _navigateToWorkoutTemplateId = MutableStateFlow<Long?>(null)
    val navigateToWorkoutTemplateId: StateFlow<Long?> = _navigateToWorkoutTemplateId

    private val _navigateToExerciseTemplateId = MutableStateFlow<Long?>(null)
    val navigateToExerciseTemplateId: StateFlow<Long?> = _navigateToExerciseTemplateId

    init {
        gymRepository.getAllWorkoutTemplates().observeForever { workoutTemplates ->
            _uiState.value = _uiState.value.copy(
                workoutTemplates = workoutTemplates,
            )
        }
    }

    fun receivedNavEvent() {
        _navigateToWorkoutTemplateId.value = null
        _navigateToExerciseTemplateId.value = null
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
            confirmDialogShown = true,
            confirmDialogText = "Do you really want to delete the ${_uiState.value.selectedWorkoutTemplates.size} selected item(s)"
        )
    }

    fun onCancelDialogPressed() {
        _uiState.value = _uiState.value.copy(
            confirmDialogShown = false,
            confirmDialogText = ""
        )
    }

    fun onConfirmButtonPressed() {
        viewModelScope.launch(Dispatchers.IO) {
            gymRepository.deleteWorkoutTemplates(uiState.value.selectedWorkoutTemplates)
            getOutOfEditMode()
        }
        onCancelDialogPressed()
    }

    fun onCancelButtonPressed() {
        getOutOfEditMode()
    }

    fun onCreateWorkoutTemplateButtonPressed() {
        _uiState.value = _uiState.value.copy(
            createWorkoutTemplateDialogShown = true,
        )
    }

    fun onCreateWorkoutTemplateDialogDismissed() {
        _uiState.value = _uiState.value.copy(
            createWorkoutTemplateDialogShown = false,
        )
    }

    fun onCreateWorkoutTemplate(name: String) {
        _uiState.value = _uiState.value.copy(
            createWorkoutTemplateDialogShown = false,
        )
        viewModelScope.launch(IO) {
            val template = WorkoutTemplate(name = name)
            val id = gymRepository.insertWorkoutTemplate(template)
            _navigateToWorkoutTemplateId.value = id
        }
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