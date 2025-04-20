package com.example.gymutil.ui.plans.exercisesTemplates

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gymutil.database.entities.ExerciseTemplate
import com.example.gymutil.ui.components.ConfirmDeleteBottomSheet
import com.example.gymutil.ui.components.TopAppBarWithBackButton
import com.example.gymutil.ui.components.TopAppBarWithDeleteButton

@Composable
fun ViewExerciseTemplates(
    goToExerciseTemplate: (Long) -> Unit,
    navToCreateExerciseTemplate: () -> Unit,
    onBackPressed: () -> Unit
) {
    val viewModel: ViewExerciseTemplatesViewModel = hiltViewModel()

    //TODO avoid using uistate.value, replace with 'by'
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    //TODO find way to turn all views that use this into a reusable component
    if (uiState.editMode) {
        BackHandler {
            viewModel.onCancelButtonPressed()
        }
    }

    viewModel.navigateToExerciseTemplateId.collectAsState().let { navigateToExerciseTemplateId ->
        val id = navigateToExerciseTemplateId.value
        if (id != null) {
            viewModel.receivedNavEvent()
            goToExerciseTemplate(id)
        }
    }

    Scaffold(topBar = {
        if (uiState.editMode) {
            TopAppBarWithDeleteButton(onClosePressed = {
                viewModel.onCancelButtonPressed()
            }, onDeletePressed = {
                viewModel.onDeleteButtonPressed()
            })
        } else {
            TopAppBarWithBackButton(title = "viewing exercises", onBack = onBackPressed)
        }
    }, floatingActionButton = {
        FloatingActionButton(
            onClick = {
                navToCreateExerciseTemplate()
            }) {
            Icon(Icons.Filled.Add, "Floating action button.")
        }
    }) { contentPadding ->
        if (uiState.confirmBottomSheetShown) {
            ConfirmDeleteBottomSheet(text = uiState.confirmBottomSheetText, onDismiss = {
                viewModel.onCancelSheetPressed()
            }, onConfirm = {
                viewModel.onConfirmButtonPressed()
            })
        }

        // Screen content
        ExerciseTemplateList(
            itemsList = uiState.exerciseTemplates,
            selectedIdsList = uiState.selectedExerciseTemplatesIds,
            onItemPress = {
                viewModel.onItemPressed(it)
            },
            onLongItemPress = {
                viewModel.onLongItemPressed(it)
            },
            paddingValues = contentPadding,
            editMode = uiState.editMode
        )
    }
}

@Composable
fun ExerciseTemplateList(
    itemsList: List<ExerciseTemplate>,
    selectedIdsList: List<Long>,
    onItemPress: (Long) -> Unit,
    onLongItemPress: (Long) -> Unit,
    paddingValues: PaddingValues,
    editMode: Boolean
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = paddingValues
    ) {
        items(
            items = itemsList, key = {
                it.exerciseTemplateId
            }) { exerciseTemplate ->
            ExerciseTemplateListItem(
                exerciseTemplate = exerciseTemplate,
                onItemPress = onItemPress,
                onLongItemPress = onLongItemPress,
                editMode = editMode,
                selected = selectedIdsList.contains(exerciseTemplate.exerciseTemplateId),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExerciseTemplateListItem(
    exerciseTemplate: ExerciseTemplate,
    selected: Boolean,
    editMode: Boolean,
    onItemPress: (Long) -> Unit,
    onLongItemPress: (Long) -> Unit
) {
    val id: Long = exerciseTemplate.exerciseTemplateId
    Card(
        modifier = Modifier
            .combinedClickable(onClick = {
                // dont know how to navigate from viewmodel
                // TODO jank: does not seem like the right way to navigate on viewmodel event (spoiler: it is not)
                onItemPress(id)
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
                Text(text = exerciseTemplate.name)
            }
            Column(
                modifier = Modifier.padding(8.dp),
            ) {
                if (editMode) {
                    Checkbox(
                        checked = selected, onCheckedChange = { _ ->
                            onItemPress(id)
                        })
                } else {
                    Row {
                        OutlinedButton(
                            onClick = {
                                onItemPress(id)
                            },

                            content = {
                                Icon(
                                    imageVector = Icons.Filled.Edit, contentDescription = "Edit exercise"
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}