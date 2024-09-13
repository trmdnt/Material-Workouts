package com.example.gymutil.ui.plans.workoutTemplates

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LiveData
import com.example.gymutil.database.entities.WorkoutTemplate
import com.example.gymutil.ui.components.ConfirmDeleteBottomSheet
import com.example.gymutil.ui.components.TopAppBarWithDeleteButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewPlans(
    goToWorkoutTemplate: (workoutTemplateId: Long) -> Unit,
    navToCreateWorkoutTemplate: () -> Unit
) {
    val viewModel: ViewPlansViewmodel = hiltViewModel()
    val uiState = viewModel.uiState.collectAsState()

    //TODO find way to turn all views that use this into a reusable component
    if (uiState.value.editMode) {
        BackHandler {
            viewModel.onCancelButtonPressed()
        }
    }

    Scaffold(topBar = {
        AnimatedVisibility(
            visible = uiState.value.editMode, enter = expandVertically(
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
                navToCreateWorkoutTemplate()
            }) {
            Icon(Icons.Filled.Add, "Floating action button.")
        }
    }) { contentPadding ->
        if (uiState.value.confirmBottomSheetShown) {
            ConfirmDeleteBottomSheet(text = uiState.value.confirmBottomSheetText, onDismiss = {
                viewModel.onCancelSheetPressed()
            }, onConfirm = {
                viewModel.onConfirmButtonPressed()
            })
        }

        // Screen content
        PlansList(
            itemsList = viewModel.workoutTemplates,
            selectedIdsList = uiState.value.selectedWorkoutTemplateIds,
            goToWorkoutTemplate = {
                goToWorkoutTemplate(it)
            },
            onItemPress = {
                viewModel.onItemPressed(it)
            },
            onLongItemPress = {
                viewModel.onLongItemPressed(it)
            },
            paddingValues = contentPadding,
            editMode = uiState.value.editMode
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlansList(
    itemsList: LiveData<List<WorkoutTemplate>>,
    selectedIdsList: List<Long>,
    goToWorkoutTemplate: (workoutTemplateId: Long) -> Unit,
    paddingValues: PaddingValues,
    onItemPress: (id: Long) -> Boolean,
    onLongItemPress: (id: Long) -> Unit,
    editMode: Boolean
) {
    val workoutTemplatesList = itemsList.observeAsState().value
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = paddingValues
    ) {
        items(
            items = workoutTemplatesList ?: emptyList(), key = {
                it.workoutTemplateId
            }) { workoutTemplate ->
            workoutTemplateListItem(
                workoutTemplate = workoutTemplate,
                onItemPress = onItemPress,
                onLongItemPress = onLongItemPress,
                goToWorkoutTemplate = goToWorkoutTemplate,
                editMode = editMode,
                selectedIdsList = selectedIdsList
            )
        }
    }
}

//TODO refactor this
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun workoutTemplateListItem(
    workoutTemplate: WorkoutTemplate,
    onItemPress: (id: Long) -> Boolean,
    onLongItemPress: (id: Long) -> Unit,
    goToWorkoutTemplate: (workoutTemplateId: Long) -> Unit,
    editMode: Boolean,
    selectedIdsList: List<Long>
) {
    val id: Long = workoutTemplate.workoutTemplateId
    Card(
        modifier = Modifier
            .combinedClickable(onClick = {
                // dont know how to navigate from viewmodel
                // TODO jank: move navigation to viewmodel
                //
                if (!onItemPress(id)) {
                    goToWorkoutTemplate(id)
                }
            }, onLongClick = {
                onLongItemPress(id)
            })
            .fillMaxWidth()
    ) {

        Row {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)
            ) {
                Text(text = workoutTemplate.name)
                Text(text = "Last used: ${workoutTemplate.lastUsed?.toString() ?: "Never"}")
            }
            Column(
                modifier = Modifier.padding(8.dp),
            ) {
                if (editMode) {
                    Checkbox(
                        checked = selectedIdsList.contains(id), onCheckedChange = { _ ->
                            onItemPress(id)
                        })
                } else {
                    Row {
                        OutlinedButton(
                            onClick = {
                                goToWorkoutTemplate(id)
                            },

                            content = {
                                Icon(
                                    imageVector = Icons.Filled.Edit, contentDescription = "Edit plan"
                                )
                            },
                        )
                        Button(
                            content = {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow, contentDescription = "start workout"
                                )
                            },
                            onClick = {
                                //TODO implement start workout and navigate to it
                            },
                        )
                    }
                }
            }
        }
    }
}