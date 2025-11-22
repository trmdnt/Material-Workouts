package eu.trmdnt.workouts.ui.activities

import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.trmdnt.workouts.database.GymRepository
import eu.trmdnt.workouts.database.entities.*
import eu.trmdnt.workouts.service.TimerServiceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant

@HiltViewModel(assistedFactory = ViewWorkoutViewModel.ViewWorkoutViewModelFactory::class)
class ViewWorkoutViewModel @AssistedInject constructor(
    val gymRepository: GymRepository,
    @Assisted private val workoutId: Long,
    @Assisted private val editing: Boolean,
    private val timerServiceManager: TimerServiceManager
) : ViewModel() {
    @AssistedFactory
    interface ViewWorkoutViewModelFactory {
        fun create(workoutId: Long, editing: Boolean): ViewWorkoutViewModel
    }

    data class UiState(
        val topBarTitle: String = "Viewing workout",
        val editMode: Boolean,
        val exercises: List<ExerciseWithSets> = emptyList(),
        val recommendedExercises: List<ExerciseTemplate> = emptyList(),
        val allAvailableExercises: List<ExerciseTemplate> = emptyList(),
        val displaySelectExerciseBottomSheet: Boolean = false,
        val showUndoSnackBar: Boolean = false,
        val undoSnackBarMessage: String? = null,
        val workoutName: String? = null
    )

    private val _uiState = MutableStateFlow<UiState>(UiState(editMode = editing))
    val uiState: StateFlow<UiState> = _uiState

    private val _goBack: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val goBack: StateFlow<Boolean> = _goBack

    private val _navigateToEditExerciseTemplate: MutableStateFlow<Long?> = MutableStateFlow(null)
    val navigateToEditExerciseTemplate: StateFlow<Long?> = _navigateToEditExerciseTemplate

    private val _navigateToExerciseStatistics: MutableStateFlow<Long?> = MutableStateFlow(null)
    val navigateToExerciseStatistics: StateFlow<Long?> = _navigateToExerciseStatistics


    private val workout = gymRepository.getWorkoutById(workoutId)
    private val workoutObserver = Observer<Workout?> {
        if (it == null) {
            _goBack.value = true
            return@Observer
        }
        if (workoutTemplateId.value == null) {
            it.workoutTemplateId?.let { id ->
                recommended = gymRepository.getAllExerciseTemplatesFromWorkoutTemplate(id)
                recommended?.observeForever(recommendedObserver)
            }
        }
        _uiState.value = _uiState.value.copy(
            workoutName = it.name,
        )
    }

    private val exercises = gymRepository.getAllExercisesByWorkoutId(workoutId)
    private val exercisesObserver = Observer<List<ExerciseWithSets>> {
        _uiState.value = _uiState.value.copy(exercises = it)
        updateRecommended()
    }

    private val allAvailableExercises = gymRepository.getAllExerciseTemplates()
    private val allAvailableExercisesObserver = Observer<List<ExerciseTemplate>> {
        _uiState.value = _uiState.value.copy(allAvailableExercises = it)
    }


    private val workoutTemplateId: MutableLiveData<Long?> = MutableLiveData(null)
    private var recommended: LiveData<List<ExerciseTemplate>>? = null
    private val recommendedObserver = Observer<List<ExerciseTemplate>> {
        updateRecommended()
    }

    init {
        workout.observeForever(workoutObserver)
        exercises.observeForever(exercisesObserver)
        allAvailableExercises.observeForever(allAvailableExercisesObserver)
    }

    private var lastActionToUndo: Any? = null

    private fun updateRecommended() {
        recommended?.value?.let {
            val recommended = it.filter { exerciseTemplate ->
                uiState.value.exercises.find { it.exercise.exerciseTemplateId == exerciseTemplate.exerciseTemplateId } == null
            }

            viewModelScope.launch(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(
                    recommendedExercises = recommended,
                )
            }
        }
    }

    override fun onCleared() {
        //TODO check if cancelling observers is really necessary if the viewmodel does not exist anymore
        super.onCleared()
        workout.removeObserver(workoutObserver)
        exercises.removeObserver(exercisesObserver)
        allAvailableExercises.removeObserver(allAvailableExercisesObserver)
    }

    fun addExercise(exerciseTemplate: ExerciseTemplate) {
        val exercise = Exercise(
            workoutId = workoutId, exerciseTemplateId = exerciseTemplate.exerciseTemplateId,
        )
        viewModelScope.launch(IO) {
            gymRepository.insertExercise(exercise)
        }
    }

    fun onWeightChanged(setId: Long, exerciseId: Long, nWeight: Double) {
        val set = findSet(setId, exerciseId)
        set?.let {
            updateSet(it.copy(weight = nWeight))
        }
    }

    fun onRepsChanged(setId: Long, exerciseId: Long, nReps: Int) {
        val set = findSet(setId, exerciseId)
        set?.let {
            updateSet(it.copy(reps = nReps))
        }
    }

    fun onTimeChanged(setId: Long, exerciseId: Long, nTime: Long) {
        TODO()
    }

    fun onDistanceChanged(setId: Long, exerciseId: Long, nDistance: Double) {
        TODO()
    }

    fun onSetAdded(exerciseId: Long) {
        val exerciseSet = ExerciseSet(
            exerciseId = exerciseId, date = Instant.now().epochSecond
        )
        insertSet(exerciseSet)
        viewModelScope.launch(IO) {
            timerServiceManager.startTimer(workoutId)
        }
    }

    fun insertSet(exerciseSet: ExerciseSet) {
        viewModelScope.launch(IO) {
            gymRepository.insertSet(set = exerciseSet)
        }
    }

    fun updateSet(exerciseSet: ExerciseSet) {
        viewModelScope.launch(IO) {
            gymRepository.updateSet(set = exerciseSet)
        }
    }

    private fun findSet(setId: Long, exerciseId: Long): ExerciseSet? {
        exercises.value.let {
            return it?.find { it.exercise.exerciseId == exerciseId }?.exerciseSets?.find { it.id == setId }
        }
        return null
    }

    fun onDeleteExercisePressed(exerciseId: Long) {
        viewModelScope.launch(IO) {
            val exerciseWithSets = gymRepository.getExerciseWithSets(exerciseId)
            gymRepository.deleteExerciseById(exerciseId)
            lastActionToUndo = exerciseWithSets
            _uiState.value = _uiState.value.copy(
                showUndoSnackBar = true, undoSnackBarMessage = "deleted exercise"
            )
        }
    }

    fun onDeleteSetPressed(setId: Long) {
        viewModelScope.launch(IO) {
            val set = gymRepository.getSetById(setId)
            gymRepository.deleteSetById(setId)
            lastActionToUndo = set
            _uiState.value = _uiState.value.copy(
                showUndoSnackBar = true, undoSnackBarMessage = "deleted set"
            )
        }
    }

    fun onSnackBarDismissed() {
        _uiState.value = _uiState.value.copy(
            showUndoSnackBar = false, undoSnackBarMessage = null
        )
    }

    fun onUndoPressed() {
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
        _uiState.value = _uiState.value.copy(
            showUndoSnackBar = false, undoSnackBarMessage = null
        )
    }

    fun onNewExercisePressed() {
        _uiState.value = _uiState.value.copy(displaySelectExerciseBottomSheet = true)
    }

    fun onSelectExerciseBottomSheetDismissed() {
        _uiState.value = _uiState.value.copy(displaySelectExerciseBottomSheet = false)
    }

    fun onEditButtonPressed() {
        _uiState.value = _uiState.value.copy(
            editMode = !_uiState.value.editMode,
        )
    }

    fun onWorkoutRename(nName: String) {
        val nWorkout = workout.value!!.copy(
            name = nName
        )
        viewModelScope.launch(IO) {
            gymRepository.updateWorkout(nWorkout)
        }
    }

    fun onSetIgnoreAction(setId: Long, exerciseId: Long) {
        val set = findSet(setId, exerciseId)
        set?.let {
            viewModelScope.launch(IO) {
                gymRepository.updateSet(it.copy(ignoreInStat = !it.ignoreInStat))
            }
        }
    }

    fun onExerciseInfoPressed(id: Long) {
        _navigateToExerciseStatistics.value = id
    }

    fun onExerciseEditPressed(id: Long) {
        _navigateToEditExerciseTemplate.value = id
    }

    fun onNavigate() {
        _navigateToExerciseStatistics.value = null
        _navigateToEditExerciseTemplate.value = null
    }

}