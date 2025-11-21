package eu.trmdnt.workouts.ui.activities

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.trmdnt.workouts.ui.components.ConfirmDeleteDialog
import eu.trmdnt.workouts.ui.components.FabAction
import eu.trmdnt.workouts.ui.components.MultiItemFab
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
                modifier = mod.padding(8.dp)
            ) {
                Text(text = it.name)
            }
        },
        alternativeTopBar = null,
        fab = {
            MultiItemFab(
                expanded = uiState.newWorkoutSelectionFabOpen,
                onButtonPressed = {
                    viewModel.onFabPressed()
                },
                actions = uiState.workoutTemplates.map {
                    FabAction(
                        it.name,
                        onClick = {
                            viewModel.onWorkoutTemplateSelected(it)
                        },
                    )
                }
            )
        },
        getId = {
            it.id
        },
        onItemEditClicked = {
            viewModel.onEditWorkout(it)
        })

    if (uiState.confirmDialogShown) {
        ConfirmDeleteDialog(text = uiState.confirmDialogText, onDismiss = {
            viewModel.onCancelConfirmDialogPressed()
        }, onConfirm = {
            viewModel.onConfirmButtonPressed()
        })
    }
}

