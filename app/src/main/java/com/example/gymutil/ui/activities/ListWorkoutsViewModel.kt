package com.example.gymutil.ui.activities

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymutil.database.GymRepository
import com.example.gymutil.database.entities.Workout
import com.example.gymutil.database.entities.WorkoutTemplate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ListWorkoutsViewModel @Inject constructor(private val gymRepository: GymRepository) : ViewModel() {
    data class UiState(
        var editMode: Boolean = false,
        var selectAll: Boolean = false,
        val workouts: List<Workout> = emptyList(),
        val selectedWorkoutIds: List<Long> = emptyList(),
        val confirmBottomSheetShown: Boolean = false,
        val confirmBottomSheetText: String = "",
        val startWorkoutBottomSheetShown: Boolean = false,
        val workoutTemplates: List<WorkoutTemplate> = emptyList(),
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private val _navigateToExerciseTemplateId = MutableStateFlow<Pair<Long, Boolean>?>(null)
    val navigateToExerciseTemplateId: StateFlow<Pair<Long, Boolean>?> = _navigateToExerciseTemplateId

    private val workouts = gymRepository.getAllWorkouts()
    private val workoutsObserver = Observer<List<Workout>> {
        it.let { workouts ->
            println("hello2 ${workouts.size}")
            _uiState.value = _uiState.value.copy(
                workouts = workouts,
            )
        }
    }

    private val workoutTemplates = gymRepository.getAllWorkoutTemplates()
    private val workoutTemplatesObserver = Observer<List<WorkoutTemplate>> {
        it.let { workoutTemplates ->
            _uiState.value = _uiState.value.copy(
                workoutTemplates = workoutTemplates,
            )
            println("hello ${workoutTemplates.size}")
        }

    }

    init {
        workouts.observeForever(workoutsObserver)
        workoutTemplates.observeForever(workoutTemplatesObserver)
    }

    override fun onCleared() {
        super.onCleared()
        workouts.removeObserver { workoutsObserver }
        workoutTemplates.removeObserver(workoutTemplatesObserver)
    }

    fun onItemPressed(id: Long) {
        // return true if event was handled
        if (uiState.value.editMode) {
            toggleSelection(id)
        } else {
            _navigateToExerciseTemplateId.value = Pair(id, false)
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
            confirmBottomSheetText = "Do you really want to delete the ${_uiState.value.selectedWorkoutIds.size} selected item(s)"
        )
    }

    fun onCancelConfirmSheetPressed() {
        _uiState.value = _uiState.value.copy(
            confirmBottomSheetShown = false,
            confirmBottomSheetText = ""
        )
    }

    fun onCancelStartSheetPressed() {
        _uiState.value = _uiState.value.copy(
            startWorkoutBottomSheetShown = false,
        )
    }

    fun onConfirmButtonPressed() {
        viewModelScope.launch(IO) {
            gymRepository.deleteWorkoutsById(uiState.value.selectedWorkoutIds.toList())
            getOutOfEditMode()
        }
        onCancelConfirmSheetPressed()
    }

    fun onCancelButtonPressed() {
        getOutOfEditMode()
    }

    fun onStartWorkoutButtonPressed() {
        _uiState.value = _uiState.value.copy(
            startWorkoutBottomSheetShown = true,
        )
    }

    fun onWorkoutTemplateSelected(id: Long) {
        val workoutTemplate: WorkoutTemplate = workoutTemplates.value?.find {
            it.workoutTemplateId == id
        }!!
        val workout = Workout(
            name = "${workoutTemplate.name} on ${
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            }",
            dateStarted = System.currentTimeMillis(),
            workoutTemplateId = workoutTemplate.workoutTemplateId,
        )
        viewModelScope.launch(IO) {
            val id = gymRepository.createWorkout(workout)
            _uiState.value = _uiState.value.copy(
                startWorkoutBottomSheetShown = false
            )
            _navigateToExerciseTemplateId.value = Pair(id, true)
        }
    }

    private fun getOutOfEditMode() {
        _uiState.value = _uiState.value.copy(
            selectedWorkoutIds = uiState.value.selectedWorkoutIds.toMutableList().apply {
                clear()
            }
        )
        _uiState.value = _uiState.value.copy(editMode = false)
    }

    private fun isInSelectedList(id: Long): Boolean {
        return uiState.value.selectedWorkoutIds.contains(id)
    }

    private fun addToSelectedList(id: Long) {
        if (!uiState.value.editMode) {
            _uiState.value = _uiState.value.copy(editMode = true)
        }

        _uiState.value = _uiState.value.copy(
            selectedWorkoutIds = uiState.value.selectedWorkoutIds.toMutableList().apply {
                add(id)
            }
        )
    }

    private fun removeFromSelectedList(id: Long) {

        _uiState.value = _uiState.value.copy(
            selectedWorkoutIds = uiState.value.selectedWorkoutIds.toMutableList().apply {
                remove(id)
            }
        )
        if (uiState.value.selectedWorkoutIds.isEmpty()) {
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