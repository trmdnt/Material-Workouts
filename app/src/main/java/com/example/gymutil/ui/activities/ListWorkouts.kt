package com.example.gymutil.ui.activities

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gymutil.database.entities.Workout
import com.example.gymutil.database.entities.WorkoutTemplate
import com.example.gymutil.ui.components.ConfirmDeleteBottomSheet
import com.example.gymutil.ui.components.TopAppBarWithDeleteButton


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

    //TODO find way to turn all views that use this into a reusable component
    if (uiState.editMode) {
        BackHandler {
            viewModel.onCancelButtonPressed()
        }
    }

    Scaffold(topBar = {
        AnimatedVisibility(
            visible = uiState.editMode, enter = expandVertically(
                // Expand from the top.
                expandFrom = Alignment.Top
            ), exit = shrinkVertically()
        ) {
            TopAppBarWithDeleteButton(onClosePressed = {
                viewModel.onCancelButtonPressed()
            }, onDeletePressed = {
                viewModel.onDeleteButtonPressed()
            })
        }
    }, floatingActionButton = {
        FloatingActionButton(
            onClick = {
                viewModel.onStartWorkoutButtonPressed()
            }) {
            Icon(Icons.Filled.Add, "Floating action button.")
        }
    }) { contentPadding ->
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

        Column(Modifier.padding(contentPadding)) {
            WorkoutList(
                itemsList = uiState.workouts,
                selectedIdsList = uiState.selectedWorkoutIds,
                onItemPress = {
                    viewModel.onItemPressed(it)
                },
                onLongItemPress = {
                    viewModel.onLongItemPressed(it)
                },
                editMode = uiState.editMode
            )
        }
    }
}

@Composable
fun WorkoutList(
    itemsList: List<Workout>,
    selectedIdsList: List<Long>,
    onItemPress: (Long) -> Unit,
    onLongItemPress: (Long) -> Unit,
    editMode: Boolean
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = itemsList, key = {
                it.id
            }) { workout ->
            WorkoutListItem(
                workout = workout,
                onItemPress = { onItemPress(workout.id) },
                onLongItemPress = { onLongItemPress(workout.id) },
                editMode = editMode,
                selected = selectedIdsList.contains(workout.id),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WorkoutListItem(
    workout: Workout,
    onItemPress: () -> Unit,
    onLongItemPress: () -> Unit,
    editMode: Boolean,
    selected: Boolean
) {
    Card(
        modifier = Modifier
            .combinedClickable(onClick = {
                onItemPress()
            }, onLongClick = {
                onLongItemPress()
            })
            .fillMaxWidth()
    ) {

        Row {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)
            ) {
                Text(text = workout.name)
            }
            Column(
                modifier = Modifier.padding(8.dp),
            ) {
                if (editMode) {
                    Checkbox(
                        checked = selected,
                        onCheckedChange = {
                            onItemPress()
                        })
                } else {
                    Row {
                        //TODO move to own component
                        //TODO actually open edit mode
                        OutlinedButton(
                            onClick = {
                                onItemPress()
                            },

                            content = {
                                Icon(
                                    imageVector = Icons.Filled.Edit, contentDescription = "Edit workout"
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

//TODO finish this
@Composable
fun ListAvailableTemplates(itemsList: List<WorkoutTemplate>, onItemSelected: (Long) -> Unit) {
    println("hello from list: ${itemsList.size}")
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                Text(modifier = Modifier.padding(8.dp), text = it.name)
            }
        }
    }
}

