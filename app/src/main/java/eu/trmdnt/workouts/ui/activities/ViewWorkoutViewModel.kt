package eu.trmdnt.workouts.ui.activities

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.trmdnt.workouts.R
import eu.trmdnt.workouts.database.GymRepository
import eu.trmdnt.workouts.database.StatisticsRepository
import eu.trmdnt.workouts.database.entities.*
import eu.trmdnt.workouts.database.entities.statistics.LastWeightForExercise
import eu.trmdnt.workouts.service.TimerServiceManager
import eu.trmdnt.workouts.settings.Prefs
import eu.trmdnt.workouts.settings.SettingsManager
import eu.trmdnt.workouts.ui.navigation.NavEvent
import eu.trmdnt.workouts.ui.navigation.NavEvent.Destination
import eu.trmdnt.workouts.ui.navigation.Screen.Plans.EditExercise
import eu.trmdnt.workouts.ui.navigation.Screen.Statistics.ViewWeightPerRep
import eu.trmdnt.workouts.utils.combine
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant

sealed class ViewWorkoutEvent {
    sealed class Set : ViewWorkoutEvent() {
        data class WeightChanged(val setId: Long, val exerciseId: Long, val weight: Double) : Set()
        data class RepsChanged(val setId: Long, val exerciseId: Long, val reps: Int) : Set()
        data class DistanceChanged(val setId: Long, val exerciseId: Long, val distance: Double) :
            Set()

        data class TimeChanged(val setId: Long, val exerciseId: Long, val time: Int) : Set()
        data class SetTypeChanged(val setId: Long, val exerciseId: Long, val setType: SetType) :
            Set()

        data class Added(val exerciseId: Long) : Set()
        data class Deleted(val setId: Long) : Set()
        data class EditAction(val setId: Long, val exerciseId: Long) : Set()
    }

    sealed class Exercise : ViewWorkoutEvent() {
        data class Added(val template: ExerciseTemplate) : Exercise()
        data class Deleted(val exerciseId: Long) : Exercise()
        data class InfoPressed(val id: Long) : Exercise()
        data class EditPressed(val id: Long) : Exercise()
    }

    data class WorkoutRenamed(val name: String) : ViewWorkoutEvent()
    data class SearchChanged(val query: String) : ViewWorkoutEvent()
    data object UndoPressed : ViewWorkoutEvent()
    data object SnackBarDismissed : ViewWorkoutEvent()
    data object EditButtonPressed : ViewWorkoutEvent()
    data object NewExercisePressed : ViewWorkoutEvent()
    data object SelectExerciseDismissed : ViewWorkoutEvent()

    data object EditSetDialogDismissed : ViewWorkoutEvent()
    data class ReachedBottom(val value: Boolean) : ViewWorkoutEvent()
}

@HiltViewModel(assistedFactory = ViewWorkoutViewModel.ViewWorkoutViewModelFactory::class)
class ViewWorkoutViewModel @AssistedInject constructor(
    private val gymRepository: GymRepository,
    private val statisticsRepository: StatisticsRepository,
    @Assisted private val workoutId: Long,
    @Assisted private val editing: Boolean,
    private val timerServiceManager: TimerServiceManager,
    private val settingsManager: SettingsManager,
    private val appContext: Context,
) : ViewModel() {
    companion object {
        const val TAG = "ViewWorkoutViewModel"
    }

    @AssistedFactory
    interface ViewWorkoutViewModelFactory {
        fun create(workoutId: Long, editing: Boolean): ViewWorkoutViewModel
    }

    private val _editMode = MutableStateFlow(editing)
    private val _displaySelectExerciseBottomSheet = MutableStateFlow(false)
    private val _undoSnackbarText: MutableStateFlow<String?> = MutableStateFlow(null)
    private val _scrollEnd = MutableStateFlow(false)

    private val _editDialogOpened: MutableStateFlow<Pair<Long, Long>?> = MutableStateFlow(null)

    private val _exerciseSearchQuery = MutableStateFlow("")
    private val workout: StateFlow<Workout?> =
        gymRepository.getWorkoutById(workoutId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val exercises: StateFlow<List<ExerciseWithSets>> =
        gymRepository.getAllExercisesByWorkoutId(workoutId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    data class UiState(
        val editMode: Boolean = false,
        val exercises: List<ExerciseWithSets> = emptyList(),
        val recommendedExercises: List<ExerciseTemplate> = emptyList(),
        val availableExerciseTemplates: List<ExerciseTemplate> = emptyList(),
        val displaySelectExerciseDialog: Boolean = false,
        val undoSnackBarMessage: String? = null,
        val workoutName: String? = null,
        val scrollEnd: Boolean = false,
        val exerciseSearchQuery: String = "",
        val editDialogOpened: Pair<Long, Long>? = null
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<UiState> = combine(
        _editMode,
        exercises,
        _exerciseSearchQuery.flatMapLatest {
            gymRepository.searchExerciseTemplates(it)
        },
        workout.flatMapLatest {
            gymRepository.getAllExerciseTemplatesFromWorkoutTemplate(
                it?.workoutTemplateId ?: 0
            )
        },
        _displaySelectExerciseBottomSheet,
        _undoSnackbarText,
        workout,
        _scrollEnd,
        _exerciseSearchQuery,
        _editDialogOpened
    ) { editMode, exercises, allAvailableExercises, recommended, selectExerciseBottomSheet, undoText, workout, scrollEnd, exerciseSearchQuery, editDialogOpened ->
        // remove the ones that are already present
        val filteredRecommended = recommended.filter { exerciseTemplate ->
            exercises.find { it.exercise.exerciseTemplateId == exerciseTemplate.exerciseTemplateId } == null
        }

        UiState(
            editMode = editMode,
            exercises = exercises,
            recommendedExercises = filteredRecommended,
            availableExerciseTemplates = allAvailableExercises,
            displaySelectExerciseDialog = selectExerciseBottomSheet,
            undoSnackBarMessage = undoText,
            workoutName = workout?.name,
            scrollEnd = scrollEnd,
            exerciseSearchQuery = exerciseSearchQuery,
            editDialogOpened = editDialogOpened
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = UiState()
    )

    private val _navigationEvents = MutableSharedFlow<NavEvent>()
    val navigationEvents: SharedFlow<NavEvent> = _navigationEvents

    private var lastActionToUndo: Any? = null

    fun handleEvent(event: ViewWorkoutEvent) {
        when (event) {
            ViewWorkoutEvent.EditButtonPressed -> {
                _editMode.value = !_editMode.value
            }

            is ViewWorkoutEvent.Exercise.Added -> {
                val exercise = Exercise(
                    workoutId = workoutId, exerciseTemplateId = event.template.exerciseTemplateId,
                )
                viewModelScope.launch(IO) {
                    gymRepository.insertExercise(exercise)
                }
                _displaySelectExerciseBottomSheet.value = false
                _exerciseSearchQuery.value = ""
            }

            is ViewWorkoutEvent.Exercise.Deleted -> {
                viewModelScope.launch(IO) {
                    val exerciseWithSets = gymRepository.getExerciseWithSets(event.exerciseId)
                    lastActionToUndo = exerciseWithSets
                    gymRepository.deleteExerciseById(event.exerciseId)
                    _undoSnackbarText.value = appContext.getString(R.string.deleted_exercise)
                }
            }

            is ViewWorkoutEvent.Exercise.EditPressed -> {
                viewModelScope.launch {
                    _navigationEvents.emit(Destination(EditExercise(event.id)))
                }
            }

            is ViewWorkoutEvent.Exercise.InfoPressed -> {
                viewModelScope.launch {
                    _navigationEvents.emit(
                        Destination(
                            ViewWeightPerRep(
                                event.id
                            )
                        )
                    )
                }
            }

            ViewWorkoutEvent.NewExercisePressed -> {
                _displaySelectExerciseBottomSheet.value = true
            }

            ViewWorkoutEvent.SelectExerciseDismissed -> {
                _displaySelectExerciseBottomSheet.value = false
            }

            is ViewWorkoutEvent.Set.Added -> {
                viewModelScope.launch(IO) {
                    val exerciseTemplateId =
                        uiState.value.exercises.find { it.exercise.exerciseId == event.exerciseId }?.exerciseTemplate?.exerciseTemplateId

                    val shouldReuseWeight = settingsManager.getPreference(
                        Prefs.reuseLastWeight
                    ).first()
                    val lastWeight = if (!shouldReuseWeight || exerciseTemplateId == null) {
                        LastWeightForExercise(0.0, 0.0, 0.0)
                    } else {
                        statisticsRepository.getLastWeightByExerciseTemplate(exerciseTemplateId)
                    }

                    val defaultReps =
                        settingsManager.getPreference(Prefs.defaultRepCount).first()
                    val exerciseSet = ExerciseSet(
                        exerciseId = event.exerciseId,
                        date = Instant.now().epochSecond,
                        reps = defaultReps,
                        weight = lastWeight.default
                    )

                    val setId = gymRepository.insertSet(set = exerciseSet).first()

                    val shouldOpenDialog = settingsManager.getPreference(Prefs.openEditDialog).first()

                    if (shouldOpenDialog) {
                        _editDialogOpened.value = Pair(event.exerciseId, setId)
                    }
                }
                viewModelScope.launch(IO) {
                    timerServiceManager.startTimer(workoutId)
                }
            }

            is ViewWorkoutEvent.Set.Deleted -> {
                viewModelScope.launch(IO) {
                    val set = gymRepository.getSetById(event.setId)
                    lastActionToUndo = set
                    gymRepository.deleteSetById(event.setId)
                    _undoSnackbarText.value = appContext.getString(R.string.deleted_set)
                }
            }

            is ViewWorkoutEvent.Set.DistanceChanged -> {
                val set = findSet(event.setId, event.exerciseId)
                set?.let {
                    updateSet(it.copy(distance = event.distance))
                }
            }

            is ViewWorkoutEvent.Set.EditAction -> {
                _editDialogOpened.value = Pair(event.exerciseId, event.setId)
            }

            is ViewWorkoutEvent.Set.RepsChanged -> {
                val set = findSet(event.setId, event.exerciseId)
                set?.let {
                    updateSet(it.copy(reps = event.reps))
                }
            }

            is ViewWorkoutEvent.Set.TimeChanged -> {
                val set = findSet(event.setId, event.exerciseId)
                set?.let {
                    updateSet(it.copy(time = event.time.toLong()))
                }
            }

            is ViewWorkoutEvent.Set.WeightChanged -> {
                val set = findSet(event.setId, event.exerciseId)
                set?.let {
                    updateSet(it.copy(weight = event.weight))
                }
            }

            ViewWorkoutEvent.SnackBarDismissed -> {
                _undoSnackbarText.value = null
            }

            ViewWorkoutEvent.UndoPressed -> {
                lastActionToUndo?.let {
                    when (it) {
                        is ExerciseWithSets -> {
                            viewModelScope.launch(IO) {
                                gymRepository.insertExercise(it.exercise)
                                gymRepository.insertSets(it.exerciseSets)
                            }
                        }

                        is ExerciseSet -> {
                            viewModelScope.launch(IO) {
                                gymRepository.insertSet(it)
                            }
                        }
                    }
                }
                _undoSnackbarText.value = null
            }

            is ViewWorkoutEvent.WorkoutRenamed -> {
                val nWorkout = workout.value!!.copy(
                    name = event.name
                )
                viewModelScope.launch(IO) {
                    gymRepository.updateWorkout(nWorkout)
                }
            }

            is ViewWorkoutEvent.ReachedBottom -> {
                _scrollEnd.value = event.value
            }

            is ViewWorkoutEvent.SearchChanged -> {
                _exerciseSearchQuery.value = event.query
            }

            is ViewWorkoutEvent.Set.SetTypeChanged -> {
                val set = findSet(event.setId, event.exerciseId)
                set?.let {
                    viewModelScope.launch(IO) {
                        val exerciseTemplateId =
                            uiState.value.exercises.find { it.exercise.exerciseId == event.exerciseId }?.exerciseTemplate?.exerciseTemplateId
                        val lastWeight = if (exerciseTemplateId == null) {
                            LastWeightForExercise(0.0, 0.0, 0.0)
                        } else {
                            statisticsRepository.getLastWeightByExerciseTemplate(exerciseTemplateId)
                        }

                        val shouldReuseWeight = settingsManager.getPreference(
                            Prefs.reuseLastWeight
                        ).first()

                        // if the user has not changed the weight, we can set the weight from the last set
                        if (shouldReuseWeight && lastWeight.chooseWeight(set.setType) == set.weight) {
                            updateSet(
                                it.copy(
                                    setType = event.setType,
                                    weight = lastWeight.chooseWeight(event.setType)
                                )
                            )
                        } else {
                            updateSet(it.copy(setType = event.setType))
                        }
                    }


                }
            }

            ViewWorkoutEvent.EditSetDialogDismissed -> {
                _editDialogOpened.value = null
            }
        }
    }

    private fun updateSet(exerciseSet: ExerciseSet) {
        viewModelScope.launch(IO) {
            gymRepository.updateSet(set = exerciseSet)
        }
    }

    private fun findSet(setId: Long, exerciseId: Long): ExerciseSet? {
        exercises.value.let { value ->
            return value.find { it.exercise.exerciseId == exerciseId }?.exerciseSets?.find { it.id == setId }
        }
    }
}