package eu.trmdnt.workouts.ui.activities

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.trmdnt.workouts.ui.components.ConfirmDeleteDialog
import eu.trmdnt.workouts.ui.components.ListAvailableItems
import eu.trmdnt.workouts.ui.components.SelectionContainerWithTopBar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListWorkouts(navigateToWorkout: (Long, Boolean) -> Unit) {
    val viewModel: ListWorkoutsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    viewModel.navigateToWorkout.collectAsStateWithLifecycle().let {
        it.value?.let { pair ->
            viewModel.receivedNavEvent()
            navigateToWorkout(pair.first, pair.second)
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
        fab = {
            FloatingActionButton(onClick = {
                viewModel.onStartWorkoutButtonPressed()
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
            }
        },
        getId = {
            it.id
        },
        onItemEditClicked = {
            viewModel.onEditWorkout(it)
        }
    )

    if (uiState.confirmDialogShown) {
        ConfirmDeleteDialog(text = uiState.confirmDialogText, onDismiss = {
            viewModel.onCancelConfirmDialogPressed()
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

