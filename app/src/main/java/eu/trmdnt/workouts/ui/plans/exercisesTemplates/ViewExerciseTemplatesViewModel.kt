package eu.trmdnt.workouts.ui.plans.exercisesTemplates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.trmdnt.workouts.database.GymRepository
import eu.trmdnt.workouts.database.entities.ExerciseTemplate
import eu.trmdnt.workouts.ui.SelectionEvent
import eu.trmdnt.workouts.ui.SelectionEvent.ToggleSelection
import eu.trmdnt.workouts.ui.SelectionManager
import eu.trmdnt.workouts.ui.navigation.NavEvent
import eu.trmdnt.workouts.ui.navigation.NavEvent.Destination
import eu.trmdnt.workouts.ui.navigation.Screen
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ViewExerciseTemplatesEvent {
    data class OnItemPressed(val exerciseTemplate: ExerciseTemplate) : ViewExerciseTemplatesEvent()
    data class OnItemLongPressed(val exerciseTemplate: ExerciseTemplate) :
        ViewExerciseTemplatesEvent()

    object OnCreateExerciseTemplateButtonPressed : ViewExerciseTemplatesEvent()
    object OnCreateExerciseTemplateDialogDismissed : ViewExerciseTemplatesEvent()
    data class CreateExerciseTemplates(val name: String) : ViewExerciseTemplatesEvent()

    object ClearSelection : ViewExerciseTemplatesEvent()
    object DeleteSelected : ViewExerciseTemplatesEvent()
    object ConfirmDeletion : ViewExerciseTemplatesEvent()
    object CancelDeletion : ViewExerciseTemplatesEvent()
}

@HiltViewModel
class ViewExerciseTemplatesViewModel @Inject constructor(private val gymRepository: GymRepository) :
    ViewModel() {
    data class UiState(
        var editMode: Boolean = false,
        val exerciseTemplates: List<ExerciseTemplate> = emptyList(),
        val selectedExerciseTemplates: Set<ExerciseTemplate> = emptySet(),
        val confirmDialogShow: Boolean = false,
        val createExerciseDialogShown: Boolean = false,
    ) {
        val confirmDialogText: String
            get() = "Do you really want to delete the ${selectedExerciseTemplates.size} selected item(s)?"
    }

    val selectionManager: SelectionManager<ExerciseTemplate> = SelectionManager({ deleteItems(it) })

    private val _showCreateExerciseDialog = MutableStateFlow(false)

    val uiState: StateFlow<UiState> = combine(
        selectionManager.isEditMode,
        gymRepository.getAllExerciseTemplates(),
        selectionManager.selectedItems,
        selectionManager.confirmDeleteDialogShown,
        _showCreateExerciseDialog
    ) { isEditMode, items, selectedItems, confirmShown, createShow ->
        UiState(
            editMode = isEditMode,
            exerciseTemplates = items,
            selectedExerciseTemplates = selectedItems,
            confirmDialogShow = confirmShown,
            createExerciseDialogShown = createShow,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = UiState()
    )

    private val _navigationEvents = MutableSharedFlow<NavEvent>()
    val navigationEvents: SharedFlow<NavEvent> = _navigationEvents

    fun createExercise(name: String) {
        viewModelScope.launch(IO) {
            val template = ExerciseTemplate(name = name)
            val id = gymRepository.insertExerciseTemplate(template)
            viewModelScope.launch {
                _navigationEvents.emit(NavEvent.Destination(Screen.Plans.EditExercise(id)))
            }
        }
    }

    private fun deleteItems(items: Set<ExerciseTemplate>) {
        viewModelScope.launch(IO) {
            gymRepository.deleteExerciseTemplates(items.toList())
        }
    }

    fun handleEvent(event: ViewExerciseTemplatesEvent) {
        when (event) {
            ViewExerciseTemplatesEvent.CancelDeletion -> selectionManager.handleCommonEvents(
                SelectionEvent.DismissDeletionDialog
            )

            ViewExerciseTemplatesEvent.ClearSelection -> selectionManager.handleCommonEvents(
                SelectionEvent.ClearSelection
            )

            ViewExerciseTemplatesEvent.ConfirmDeletion -> selectionManager.handleCommonEvents(
                SelectionEvent.ConfirmDeletion
            )

            ViewExerciseTemplatesEvent.DeleteSelected -> selectionManager.handleCommonEvents(
                SelectionEvent.DeleteSelected
            )

            is ViewExerciseTemplatesEvent.CreateExerciseTemplates -> {
                createExercise(event.name)
                _showCreateExerciseDialog.value = false
            }

            ViewExerciseTemplatesEvent.OnCreateExerciseTemplateButtonPressed -> {
                _showCreateExerciseDialog.value = true
            }

            ViewExerciseTemplatesEvent.OnCreateExerciseTemplateDialogDismissed -> {
                _showCreateExerciseDialog.value = false
            }

            is ViewExerciseTemplatesEvent.OnItemLongPressed -> {
                selectionManager.handleCommonEvents(ToggleSelection(event.exerciseTemplate))
            }

            is ViewExerciseTemplatesEvent.OnItemPressed -> {
                if (uiState.value.editMode) {
                    selectionManager.handleCommonEvents(ToggleSelection(event.exerciseTemplate))
                } else {
                    viewModelScope.launch {
                        _navigationEvents.emit(Destination(Screen.Plans.EditExercise(event.exerciseTemplate.exerciseTemplateId)))
                    }
                }
            }
        }
    }
}