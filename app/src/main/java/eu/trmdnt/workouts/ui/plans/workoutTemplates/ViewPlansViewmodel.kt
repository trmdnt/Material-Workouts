package eu.trmdnt.workouts.ui.plans.workoutTemplates

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.trmdnt.workouts.database.GymRepository
import eu.trmdnt.workouts.database.entities.ExerciseTemplate
import eu.trmdnt.workouts.database.entities.WorkoutTemplate
import eu.trmdnt.workouts.ui.SelectionEvent
import eu.trmdnt.workouts.ui.SelectionEvent.ToggleSelection
import eu.trmdnt.workouts.ui.SelectionManager
import eu.trmdnt.workouts.ui.navigation.NavEvent
import eu.trmdnt.workouts.ui.navigation.NavEvent.Destination
import eu.trmdnt.workouts.ui.navigation.Screen
import eu.trmdnt.workouts.utils.combine
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ViewPlansEvent {
    data class OnItemPressed(val workoutTemplate: WorkoutTemplate) : ViewPlansEvent()
    data class OnItemLongPressed(val workoutTemplate: WorkoutTemplate) : ViewPlansEvent()
    object OnCreateWorkoutTemplateButtonPressed : ViewPlansEvent()
    object OnCreateWorkoutTemplateDialogDismissed : ViewPlansEvent()
    data class CreateWorkoutTemplate(val name: String) : ViewPlansEvent()
    object OnCreateExerciseButtonPressed : ViewPlansEvent()
    object OnCreateExerciseDialogDismissed : ViewPlansEvent()
    data class CreateExercise(val name: String) : ViewPlansEvent()
    object OpenExerciseTemplates : ViewPlansEvent()


    object ClearSelection : ViewPlansEvent()
    object DeleteSelected : ViewPlansEvent()
    object ConfirmDeletion : ViewPlansEvent()
    object CancelDeletion : ViewPlansEvent()
}

@HiltViewModel
class ViewPlansViewmodel @Inject constructor(private val gymRepository: GymRepository) :
    ViewModel() {
    data class UiState(
        val editMode: Boolean = false,
        val workoutTemplates: List<WorkoutTemplate> = emptyList(),
        val selectedWorkoutTemplates: Set<WorkoutTemplate> = emptySet(),
        val confirmDialogShown: Boolean = false,
        val createWorkoutTemplateDialogShown: Boolean = false,
        val createExerciseDialogShown: Boolean = false,
    )

    val selectionManager: SelectionManager<WorkoutTemplate> = SelectionManager({ deleteItems(it) })


    private val _createWorkoutTemplateDialogShown = MutableStateFlow(false)
    private val _createExerciseDialogShown = MutableStateFlow(false)

    val uiState: StateFlow<UiState> = combine(
        gymRepository.getAllWorkoutTemplates(),
        selectionManager.isEditMode,
        selectionManager.selectedItems,
        selectionManager.confirmDeleteDialogShown,
        _createWorkoutTemplateDialogShown,
        _createExerciseDialogShown
    ) { templates, editMode, selected, confirmDeleteShown, createTemplateShown, createExerciseShown ->
        UiState(
            editMode = editMode,
            workoutTemplates = templates,
            selectedWorkoutTemplates = selected,
            confirmDialogShown = confirmDeleteShown,
            createWorkoutTemplateDialogShown = createTemplateShown,
            createExerciseDialogShown = createExerciseShown
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = UiState()
    )

    private val _navigationEvents = MutableSharedFlow<NavEvent>()
    val navigationEvents: SharedFlow<NavEvent> = _navigationEvents

    fun handleEvent(event: ViewPlansEvent) {
        when (event) {
            is ViewPlansEvent.CreateExercise -> {
                createExercise(event.name)
                _createExerciseDialogShown.value = false
            }

            is ViewPlansEvent.CreateWorkoutTemplate -> {
                createWorkoutTemplate(event.name)
                _createWorkoutTemplateDialogShown.value = false
            }

            ViewPlansEvent.OnCreateExerciseButtonPressed -> {
                _createExerciseDialogShown.value = true
            }

            ViewPlansEvent.OnCreateExerciseDialogDismissed -> {
                _createExerciseDialogShown.value = false
            }

            ViewPlansEvent.OnCreateWorkoutTemplateButtonPressed -> {
                _createWorkoutTemplateDialogShown.value = true
            }

            ViewPlansEvent.OnCreateWorkoutTemplateDialogDismissed -> {
                _createWorkoutTemplateDialogShown.value = false
            }

            is ViewPlansEvent.OnItemPressed -> {
                if (uiState.value.editMode) {
                    selectionManager.handleCommonEvents(ToggleSelection(event.workoutTemplate))
                } else {
                    viewModelScope.launch {
                        _navigationEvents.emit(Destination(Screen.Plans.EditPlan(event.workoutTemplate.workoutTemplateId)))
                    }
                }
            }

            is ViewPlansEvent.OnItemLongPressed -> {
                selectionManager.handleCommonEvents(ToggleSelection(event.workoutTemplate))
            }

            ViewPlansEvent.OpenExerciseTemplates -> {
                viewModelScope.launch {
                    _navigationEvents.emit(Destination(Screen.Plans.ViewExercises))
                }
            }

            ViewPlansEvent.CancelDeletion -> selectionManager.handleCommonEvents(SelectionEvent.DismissDeletionDialog)
            ViewPlansEvent.ClearSelection -> selectionManager.handleCommonEvents(SelectionEvent.ClearSelection)
            ViewPlansEvent.ConfirmDeletion -> selectionManager.handleCommonEvents(SelectionEvent.ConfirmDeletion)
            ViewPlansEvent.DeleteSelected -> selectionManager.handleCommonEvents(SelectionEvent.DeleteSelected)
        }
    }


    fun createWorkoutTemplate(name: String) {
        viewModelScope.launch(IO) {
            val template = WorkoutTemplate(name = name)
            val id = gymRepository.insertWorkoutTemplate(template)
            viewModelScope.launch {
                _navigationEvents.emit(Destination(Screen.Plans.EditPlan(id)))
            }
        }
    }

    private fun createExercise(name: String) {
        viewModelScope.launch(IO) {
            val template = ExerciseTemplate(name = name)
            val id = gymRepository.insertExerciseTemplate(template)
            viewModelScope.launch {
                _navigationEvents.emit(Destination(Screen.Plans.EditExercise(id)))
            }
        }
    }

    private fun deleteItems(items: Set<WorkoutTemplate>) {
        viewModelScope.launch(IO) {
            Log.d("ViewPlansViewModel", "deleteItems: ${uiState.value.selectedWorkoutTemplates}")
            gymRepository.deleteWorkoutTemplates(items.toList())
        }
    }
}