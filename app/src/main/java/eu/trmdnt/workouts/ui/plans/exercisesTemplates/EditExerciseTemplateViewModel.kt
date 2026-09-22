package eu.trmdnt.workouts.ui.plans.exercisesTemplates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.trmdnt.workouts.database.GymRepository
import eu.trmdnt.workouts.database.entities.ExerciseTemplate
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.*
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
        val exerciseTemplate: ExerciseTemplate? = null
    )

    private var exerciseTemplate: Flow<ExerciseTemplate?> =
        gymRepository.getExerciseTemplateById(exerciseTemplateId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val uiState: StateFlow<UiState> = exerciseTemplate.mapLatest { exerciseTemplate ->
        UiState(exerciseTemplate = exerciseTemplate)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState()
    )

    fun onNameChange(name: String) {
        uiState.value.exerciseTemplate?.let {
            updateExerciseTemplate(it.copy(name = name))
        }
    }

    fun onWeightSelected() {
        uiState.value.exerciseTemplate?.let {
            updateExerciseTemplate(it.copy(weight = !it.weight))
        }
    }

    fun onRepsSelected() {
        uiState.value.exerciseTemplate?.let {
            updateExerciseTemplate(it.copy(reps = !it.reps))
        }
    }

    fun onTimeSelected() {
        uiState.value.exerciseTemplate?.let {
            updateExerciseTemplate(it.copy(time = !it.time))
        }
    }

    fun onDistanceSelected() {
        uiState.value.exerciseTemplate?.let {
            updateExerciseTemplate(it.copy(distance = !it.distance))
        }
    }

    fun onWeightTimesTwoSelected() {
        uiState.value.exerciseTemplate?.let {
            updateExerciseTemplate(it.copy(weightTimesTwo = !it.weightTimesTwo))
        }
    }

    //TODO disallow tracking 0 stats
    private fun updateExerciseTemplate(exerciseTemplate: ExerciseTemplate) {
        viewModelScope.launch(IO) {
            gymRepository.updateExerciseTemplate(exerciseTemplate)
        }
    }
}