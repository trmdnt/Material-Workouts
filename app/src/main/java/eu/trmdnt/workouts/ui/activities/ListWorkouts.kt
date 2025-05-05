package eu.trmdnt.workouts.ui.activities

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.trmdnt.workouts.ui.components.ConfirmDeleteBottomSheet
import eu.trmdnt.workouts.ui.components.ListAvailableItems
import eu.trmdnt.workouts.ui.components.SelectionContainerWithTopBar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListWorkouts(navigateToWorkout: (Long, Boolean) -> Unit) {
    val viewModel: ListWorkoutsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    viewModel.navigateToExerciseTemplateId.collectAsStateWithLifecycle().let {
        it.value?.let {
            viewModel.receivedNavEvent()
            navigateToWorkout(it.first, it.second)
        }
    }

    SelectionContainerWithTopBar(
        itemsList = uiState.workouts,
        selectedItemsList = uiState.selectedWorkouts,
        onItemClick = {
            viewModel.onItemPressed(it)
        },
        onLongItemClick = {
            viewModel.onLongItemPressed(it)
        },
        onCancel = {
            viewModel.onCancelButtonPressed()
        },
        onDelete = {
            viewModel.onDeleteButtonPressed()
        },
        editMode = uiState.editMode,
        textContent = { it, mod ->
            Column(
                modifier = mod
                    .padding(8.dp)
            ) {
                Text(text = it.name)
            }
        },
        alternativeTopBar = null,
        fabAction = {
            viewModel.onStartWorkoutButtonPressed()
        },
        getId = {
            it.id
        }
    )

    if (uiState.confirmBottomSheetShown) {
        ConfirmDeleteBottomSheet(text = uiState.confirmBottomSheetText, onDismiss = {
            viewModel.onCancelConfirmSheetPressed()
        }, onConfirm = {
            viewModel.onConfirmButtonPressed()
        })
    }
    if (uiState.startWorkoutBottomSheetShown) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = {
                viewModel.onCancelStartSheetPressed()
            },
            sheetState = sheetState,
        ) {
            ListAvailableItems(
                items = uiState.workoutTemplates,
                onItemClick = {
                    viewModel.onWorkoutTemplateSelected(it)
                },
                getName = {
                    it.name
                }
            )
        }
    }
}

