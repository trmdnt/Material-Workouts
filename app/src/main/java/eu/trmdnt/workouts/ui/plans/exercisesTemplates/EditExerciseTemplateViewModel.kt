package eu.trmdnt.workouts.ui.plans.exercisesTemplates

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.trmdnt.workouts.database.GymRepository
import eu.trmdnt.workouts.database.entities.ExerciseTemplate
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = EditExerciseTemplateViewModel.EditExerciseTemplateViewModelFactory::class)
class EditExerciseTemplateViewModel @AssistedInject constructor(
    val gymRepository: GymRepository,
    @Assisted private val exerciseTemplateId: Long,
) : ViewModel() {
    @AssistedFactory
    interface EditExerciseTemplateViewModelFactory {
        fun create(exerciseTemplateId: Long): EditExerciseTemplateViewModel
    }

    data class UiState(
        val nameTextField: String,
        val weightSelected: Boolean,
        val timeSelected: Boolean,
        val repsSelected: Boolean,
        val distanceSelected: Boolean,
        val saveButtonEnabled: Boolean,
        val weightTimesTwo: Boolean,
    )


    var exerciseTemplate: LiveData<ExerciseTemplate> = gymRepository.getExerciseTemplateById(exerciseTemplateId)

    private val _uiState = MutableStateFlow<UiState>(
        UiState(
            nameTextField = "",
            weightSelected = false,
            timeSelected = false,
            repsSelected = false,
            distanceSelected = false,
            saveButtonEnabled = false,
            weightTimesTwo = false,
        )
    )
    val uiState: StateFlow<UiState> = _uiState

    private val exerciseTemplateObserver = Observer<ExerciseTemplate> { it ->
        it.let { exerciseTemplate ->
            println(exerciseTemplate)
            _uiState.value = UiState(
                nameTextField = exerciseTemplate.name,
                weightSelected = exerciseTemplate.weight,
                timeSelected = exerciseTemplate.time,
                repsSelected = exerciseTemplate.reps,
                distanceSelected = exerciseTemplate.distance,
                saveButtonEnabled = false,
                weightTimesTwo = exerciseTemplate.weightTimesTwo
            )
        }
    }

    init {
        exerciseTemplate.observeForever(exerciseTemplateObserver)
    }

    override fun onCleared() {
        super.onCleared()
        exerciseTemplate.removeObserver(exerciseTemplateObserver)
    }

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(nameTextField = name, saveButtonEnabled = true)
    }

    fun onWeightSelected() {
        _uiState.value = _uiState.value.copy(weightSelected = !_uiState.value.weightSelected, saveButtonEnabled = true)
    }

    fun onRepsSelected() {
        _uiState.value = _uiState.value.copy(repsSelected = !_uiState.value.repsSelected, saveButtonEnabled = true)
    }

    fun onTimeSelected() {
        _uiState.value = _uiState.value.copy(timeSelected = !_uiState.value.timeSelected, saveButtonEnabled = true)
    }

    fun onDistanceSelected() {
        _uiState.value =
            _uiState.value.copy(distanceSelected = !_uiState.value.distanceSelected, saveButtonEnabled = true)
    }

    fun onWeightTimesTwoSelected() {
        _uiState.value = _uiState.value.copy(
            weightTimesTwo = !_uiState.value.weightTimesTwo, saveButtonEnabled = true
        )
    }

    fun onSaveButtonPressed() {
        val et = exerciseTemplate.value!!.copy(
            name = _uiState.value.nameTextField,
            weight = _uiState.value.weightSelected,
            time = _uiState.value.timeSelected,
            reps = _uiState.value.repsSelected,
            distance = _uiState.value.distanceSelected,
            weightTimesTwo = _uiState.value.weightTimesTwo
        )
        viewModelScope.launch(IO) {
            gymRepository.updateExerciseTemplate(et)
        }
    }


}