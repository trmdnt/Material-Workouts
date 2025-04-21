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
        val selectedWorkouts: List<Workout> = emptyList(),
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
            workoutTemplates.toMutableList().apply {
                add(
                    0, WorkoutTemplate(
                        workoutTemplateId = 0,
                        name = "Empty workout",
                    )
                )
            }
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

    fun onItemPressed(workout: Workout) {
        // return true if event was handled
        if (uiState.value.editMode) {
            toggleSelection(workout)
        } else {
            _navigateToExerciseTemplateId.value = Pair(workout.id, false)
        }
    }

    fun receivedNavEvent() {
        _navigateToExerciseTemplateId.value = null
    }

    fun onLongItemPressed(workout: Workout) {
        toggleSelection(workout)
    }

    fun onDeleteButtonPressed() {
        _uiState.value = _uiState.value.copy(
            confirmBottomSheetShown = true,
            confirmBottomSheetText = "Do you really want to delete the ${_uiState.value.selectedWorkouts.size} selected item(s)"
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
            gymRepository.deleteWorkouts(uiState.value.selectedWorkouts)
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

    fun onWorkoutTemplateSelected(workoutTemplate: WorkoutTemplate) {
        val workout = Workout(
            name = "${workoutTemplate.name} on ${
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            }",
            dateStarted = System.currentTimeMillis(),
            workoutTemplateId = if (workoutTemplate.workoutTemplateId == 0.toLong()) null else workoutTemplate.workoutTemplateId,
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
            selectedWorkouts = uiState.value.selectedWorkouts.toMutableList().apply {
                clear()
            }
        )
        _uiState.value = _uiState.value.copy(editMode = false)
    }

    private fun isInSelectedList(workout: Workout): Boolean {
        return uiState.value.selectedWorkouts.contains(workout)
    }

    private fun addToSelectedList(workout: Workout) {
        if (!uiState.value.editMode) {
            _uiState.value = _uiState.value.copy(editMode = true)
        }

        _uiState.value = _uiState.value.copy(
            selectedWorkouts = uiState.value.selectedWorkouts.toMutableList().apply {
                add(workout)
            }
        )
    }

    private fun removeFromSelectedList(workout: Workout) {
        _uiState.value = _uiState.value.copy(
            selectedWorkouts = uiState.value.selectedWorkouts.toMutableList().apply {
                remove(workout)
            }
        )
        if (uiState.value.selectedWorkouts.isEmpty()) {
            _uiState.value = _uiState.value.copy(editMode = false)
        }
    }

    private fun toggleSelection(workout: Workout) {
        if (isInSelectedList(workout)) {
            removeFromSelectedList(workout)
        } else {
            addToSelectedList(workout)
        }
    }
}