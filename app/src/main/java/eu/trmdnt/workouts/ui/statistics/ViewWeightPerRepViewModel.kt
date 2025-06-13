package eu.trmdnt.workouts.ui.statistics

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.trmdnt.workouts.database.GymRepository
import eu.trmdnt.workouts.database.StatisticsRepository
import eu.trmdnt.workouts.database.entities.ExerciseTemplate
import eu.trmdnt.workouts.database.entities.statistics.WeightOnDate
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ViewWeightPerRepViewModel.ViewWeightPerRepViewModelFactory::class)
class ViewWeightPerRepViewModel @AssistedInject constructor(
    val gymRepository: GymRepository,
    val statisticsRepository: StatisticsRepository,
    @Assisted private val exerciseTemplateId: Long,
) : ViewModel() {
    @AssistedFactory
    interface ViewWeightPerRepViewModelFactory {
        fun create(exerciseTemplateId: Long): ViewWeightPerRepViewModel
    }

    companion object {
        val TAG: String = ViewWeightPerRepViewModel::class.java.simpleName
    }

    data class UiState(
        val exerciseTemplate: ExerciseTemplate? = null,
        val data: List<WeightOnDate> = emptyList(),
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    init {

        gymRepository.getExerciseTemplateById(exerciseTemplateId).observeForever {
            _uiState.value = _uiState.value.copy(exerciseTemplate = it)
        }


        viewModelScope.launch(IO) {
            statisticsRepository.getWeightPerRepHistory(exerciseTemplateId).let {
                Log.d(TAG, ": $it")
                _uiState.value = _uiState.value.copy(data = it)
            }
        }
    }

}