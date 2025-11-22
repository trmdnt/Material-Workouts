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
import eu.trmdnt.workouts.database.entities.ExerciseSet
import eu.trmdnt.workouts.database.entities.ExerciseTemplate
import eu.trmdnt.workouts.database.entities.statistics.WeightOnDate
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ViewExerciseStatisticViewModel.ViewWeightPerRepViewModelFactory::class)
class ViewExerciseStatisticViewModel @AssistedInject constructor(
    val gymRepository: GymRepository,
    val statisticsRepository: StatisticsRepository,
    @Assisted private val exerciseTemplateId: Long,
) : ViewModel() {
    @AssistedFactory
    interface ViewWeightPerRepViewModelFactory {
        fun create(exerciseTemplateId: Long): ViewExerciseStatisticViewModel
    }

    companion object {
        val TAG: String = ViewExerciseStatisticViewModel::class.java.simpleName
    }

    data class UiState(
        val exerciseTemplate: ExerciseTemplate? = null,
        val data: List<WeightOnDate> = emptyList(),
        val selectedPoint: Int? = null,
        val setsOnSelectedDate: List<ExerciseSet>? = null
    )

    private fun refreshSetsOnDate() {
        uiState.value.selectedPoint?.let {
            viewModelScope.launch(IO) {
                _uiState.value = _uiState.value.copy(
                    setsOnSelectedDate = statisticsRepository.getExerciseHistory(
                        exerciseTemplateId, uiState.value.data[it].dateTimeStamp
                    )
                )
            }
        }
    }

    fun onPlotPointSelected(index: Int) {
        _uiState.value = _uiState.value.copy(
            selectedPoint = index,
        )

        refreshSetsOnDate()
    }

    fun onPreviousPressed() {
        when (uiState.value.selectedPoint) {
            null -> {
                if (uiState.value.data.isEmpty()) {
                    return
                }

                _uiState.value = uiState.value.copy(
                    selectedPoint = uiState.value.data.size - 1
                )
            }

            0 -> {
                return
            }

            else -> {
                _uiState.value = uiState.value.copy(
                    selectedPoint = uiState.value.selectedPoint!! - 1
                )
            }
        }
        refreshSetsOnDate()
    }

    fun onNextPressed() {
        when (uiState.value.selectedPoint) {
            null -> {
                if (uiState.value.data.isEmpty()) {
                    return
                }

                _uiState.value = uiState.value.copy(
                    selectedPoint = uiState.value.data.size - 1
                )
            }

            uiState.value.data.size - 1 -> {
                return
            }

            else -> {
                _uiState.value = uiState.value.copy(
                    selectedPoint = uiState.value.selectedPoint!! + 1
                )
            }
        }
        refreshSetsOnDate()
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    init {
        gymRepository.getExerciseTemplateById(exerciseTemplateId).observeForever {
            _uiState.value = _uiState.value.copy(exerciseTemplate = it)
        }

        viewModelScope.launch(IO) {
            statisticsRepository.getWeightPerRepHistory(exerciseTemplateId).let {
                Log.d(TAG, ": $it")
                _uiState.value = _uiState.value.copy(data = it, selectedPoint = if (it.isEmpty()) null else it.size - 1)
                refreshSetsOnDate()
            }
        }
    }

}