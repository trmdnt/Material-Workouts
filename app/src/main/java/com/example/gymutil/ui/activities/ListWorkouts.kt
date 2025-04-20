package com.example.gymutil.ui.activities

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gymutil.database.entities.WorkoutTemplate
import com.example.gymutil.ui.components.ConfirmDeleteBottomSheet
import com.example.gymutil.ui.components.SelectionContainerWithTopBar


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
            ListAvailableTemplates(itemsList = uiState.workoutTemplates, onItemSelected = {
                viewModel.onWorkoutTemplateSelected(it)
            })
        }
    }
}

@Composable
fun ListAvailableTemplates(itemsList: List<WorkoutTemplate>, onItemSelected: (Long) -> Unit) {
    println("hello from list: ${itemsList.size}")
    //TODO add ability to start empty workout (without template)
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 8.dp)) {
        items(
            items = itemsList, key = {
                it.workoutTemplateId
            }
        ) {
            Card(
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { onItemSelected(it.workoutTemplateId) })
            ) {
                Text(modifier = Modifier.padding(8.dp), text = it.name, style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

