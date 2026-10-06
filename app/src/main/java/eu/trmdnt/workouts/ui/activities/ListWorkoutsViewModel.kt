package eu.trmdnt.workouts.ui.activities

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.trmdnt.workouts.R
import eu.trmdnt.workouts.database.GymRepository
import eu.trmdnt.workouts.database.StatisticsRepository
import eu.trmdnt.workouts.database.entities.Workout
import eu.trmdnt.workouts.database.entities.WorkoutTemplate
import eu.trmdnt.workouts.database.entities.statistics.RecentStatistics
import eu.trmdnt.workouts.database.entities.statistics.WorkoutWithDuration
import eu.trmdnt.workouts.ui.SelectionEvent
import eu.trmdnt.workouts.ui.SelectionEvent.ToggleSelection
import eu.trmdnt.workouts.ui.SelectionManager
import eu.trmdnt.workouts.ui.navigation.NavEvent
import eu.trmdnt.workouts.ui.navigation.Screen
import eu.trmdnt.workouts.utils.combine
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

sealed class ListWorkoutsEvent {
    data class OnItemPressed(val workout: WorkoutWithDuration) : ListWorkoutsEvent()
    data class OnItemLongPressed(val workout: WorkoutWithDuration) : ListWorkoutsEvent()
    data class OnWorkoutTemplateSelected(val workoutTemplate: WorkoutTemplate) : ListWorkoutsEvent()
    object OnFabExtended : ListWorkoutsEvent()
    data class OnEditWorkout(val workout: WorkoutWithDuration) : ListWorkoutsEvent()

    object ClearSelection : ListWorkoutsEvent()
    object DeleteSelected : ListWorkoutsEvent()
    object ConfirmDeletion : ListWorkoutsEvent()
    object CancelDeletion : ListWorkoutsEvent()
}

@HiltViewModel
class ListWorkoutsViewModel @Inject constructor(
    private val gymRepository: GymRepository,
    private val appContext: Context,
    statisticsRepository: StatisticsRepository
) :
    ViewModel() {
    data class UiState(
        var editMode: Boolean = false,
        val workouts: List<WorkoutWithDuration> = emptyList(),
        val selectedWorkouts: Set<WorkoutWithDuration> = emptySet(),
        val confirmDialogShown: Boolean = false,
        val newWorkoutSelectionFabOpen: Boolean = false,
        val workoutTemplates: List<WorkoutTemplate> = emptyList(),
        val recentStatistics: RecentStatistics = RecentStatistics(0, 0, 0, 0, 0, 0, 0, 0)
    )

    private val _newWorkoutSelectionFabOpen = MutableStateFlow(false)
    val selectionManager: SelectionManager<WorkoutWithDuration> = SelectionManager({ delete(it) })

    val uiState: StateFlow<UiState> = combine(
        gymRepository.getAllWorkoutsWithDuration(),
        gymRepository.getAllWorkoutTemplates().map {
            val new = it.toMutableList()
            new.add(
                WorkoutTemplate(
                    workoutTemplateId = 0,
                    name = appContext.getString(R.string.empty_workout),
                )
            )
            new.toList()
        },
        selectionManager.isEditMode,
        selectionManager.selectedItems,
        selectionManager.confirmDeleteDialogShown,
        _newWorkoutSelectionFabOpen,
        statisticsRepository.getRecentStatistics(),
    ) { workouts, workoutTemplates, editMode, selectedWorkouts, confirmDialogShown, newWorkoutSelectionFabOpen, recentStats ->
        UiState(
            editMode = editMode,
            workouts = workouts,
            selectedWorkouts = selectedWorkouts,
            confirmDialogShown = confirmDialogShown,
            newWorkoutSelectionFabOpen = newWorkoutSelectionFabOpen,
            workoutTemplates = workoutTemplates,
            recentStatistics = recentStats
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = UiState()
    )


    private val _navigationEvents = MutableSharedFlow<NavEvent>()
    val navigationEvents: SharedFlow<NavEvent> = _navigationEvents


    private fun onWorkoutTemplateSelected(workoutTemplate: WorkoutTemplate) {
        val workout = Workout(
            name = workoutTemplate.name,
            dateStarted = Instant.now().epochSecond,
            workoutTemplateId = if (workoutTemplate.workoutTemplateId == 0.toLong()) null else workoutTemplate.workoutTemplateId,
        )
        viewModelScope.launch(IO) {
            val id = gymRepository.createWorkout(workout)
            _newWorkoutSelectionFabOpen.value = false
            viewModelScope.launch {
                _navigationEvents.emit(
                    NavEvent.Destination(
                        Screen.Activities.ViewWorkout(
                            id,
                            true
                        )
                    )
                )
            }
        }
    }

    private fun goToWorkout(workout: WorkoutWithDuration, edit: Boolean) {
        viewModelScope.launch {
            _navigationEvents.emit(
                NavEvent.Destination(
                    Screen.Activities.ViewWorkout(
                        workout.workout.id,
                        edit
                    )
                )
            )
        }
    }

    private fun delete(items: Set<WorkoutWithDuration>) {
        viewModelScope.launch(IO) {
            gymRepository.deleteWorkouts(items.map {
                it.workout
            }.toList())
        }
    }

    fun handleEvent(event: ListWorkoutsEvent) = when (event) {
        ListWorkoutsEvent.CancelDeletion -> selectionManager.handleCommonEvents(SelectionEvent.DismissDeletionDialog)
        ListWorkoutsEvent.ClearSelection -> selectionManager.handleCommonEvents(SelectionEvent.ClearSelection)
        ListWorkoutsEvent.ConfirmDeletion -> selectionManager.handleCommonEvents(SelectionEvent.ConfirmDeletion)
        ListWorkoutsEvent.DeleteSelected -> selectionManager.handleCommonEvents(SelectionEvent.DeleteSelected)
        is ListWorkoutsEvent.OnItemLongPressed -> selectionManager.handleCommonEvents(
            ToggleSelection(
                event.workout
            )
        )

        is ListWorkoutsEvent.OnItemPressed -> {
            if (uiState.value.editMode) {
                selectionManager.handleCommonEvents(ToggleSelection(event.workout))
            } else {
                goToWorkout(event.workout, false)
            }
        }

        is ListWorkoutsEvent.OnWorkoutTemplateSelected -> onWorkoutTemplateSelected(event.workoutTemplate)
        ListWorkoutsEvent.OnFabExtended -> _newWorkoutSelectionFabOpen.value =
            !_newWorkoutSelectionFabOpen.value

        is ListWorkoutsEvent.OnEditWorkout -> goToWorkout(event.workout, true)
    }

}