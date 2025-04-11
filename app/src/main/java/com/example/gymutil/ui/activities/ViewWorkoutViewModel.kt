package com.example.gymutil.ui.activities

import androidx.lifecycle.*
import com.example.gymutil.database.GymRepository
import com.example.gymutil.database.entities.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ViewWorkoutViewModel.ViewWorkoutViewModelFactory::class)
class ViewWorkoutViewModel @AssistedInject constructor(
    val gymRepository: GymRepository, @Assisted private val workoutId: Long, @Assisted private val editing: Boolean
) : ViewModel() {
    @AssistedFactory
    interface ViewWorkoutViewModelFactory {
        fun create(workoutId: Long, editing: Boolean): ViewWorkoutViewModel
    }

    data class UiState(
        val topBarTitle: String = "Editing Workout",
        val editMode: Boolean,
        val exercises: List<ExerciseWithSets> = emptyList(),
        val recommendedExercises: List<ExerciseTemplate> = emptyList(),
    )

    private val _uiState = MutableStateFlow<UiState>(UiState(editMode = editing))
    val uiState: StateFlow<UiState> = _uiState

    private val workout = gymRepository.getWorkoutById(workoutId)
    private val workoutObserver = Observer<Workout> {
        if (workoutTemplateId.value == null) {
            workoutTemplateId.value = it.workoutTemplateId
        }
    }

    private val exercises = gymRepository.getAllExercisesByWorkoutId(workoutId)
    private val exercisesObserver = Observer<List<ExerciseWithSets>> {
        _uiState.value = _uiState.value.copy(exercises = it)
        updateRecommended()
    }


    private val workoutTemplateId: MutableLiveData<Long?> = MutableLiveData(null)
    private var available: LiveData<List<ExerciseTemplate>>? = null
    private val availableObserver = Observer<List<ExerciseTemplate>> {
        updateRecommended()
    }

    init {
        workout.observeForever(workoutObserver)
        workoutTemplateId.observeForever {
            it?.let {
                available = gymRepository.getAllExerciseTemplatesFromWorkoutTemplate(it)
                available?.observeForever(availableObserver)
            }
        }
        exercises.observeForever(exercisesObserver)
    }

    private fun updateRecommended() {
        available?.value?.let {
            val recommended = it.filter { exerciseTemplate ->
                println("recomm found ${uiState.value.exercises.find { it.exercise.exerciseTemplateId == exerciseTemplate.exerciseTemplateId }}")
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
        super.onCleared()
        workout.removeObserver(workoutObserver)
        exercises.removeObserver(exercisesObserver)
    }

    fun addExercise(exerciseTemplateId: Long) {
        val exercise = Exercise(
            workoutId = workoutId, exerciseTemplateId = exerciseTemplateId
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
            exerciseId = exerciseId, date = System.currentTimeMillis()
        )
        insertSet(exerciseSet)
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

    fun findSet(setId: Long, exerciseId: Long): ExerciseSet? {
        exercises.value.let {
            return it?.find { it.exercise.exerciseId == exerciseId }?.exerciseSets?.find { it.id == setId }
        }
        return null
    }

    fun onDeleteExercisePressed(exerciseId: Long) {
        viewModelScope.launch(IO) {
            gymRepository.deleteExerciseById(exerciseId)
        }
    }

    fun onDeleteSetPressed(setId: Long) {
        viewModelScope.launch(IO) {
            gymRepository.deleteSetById(setId)
        }
    }
}