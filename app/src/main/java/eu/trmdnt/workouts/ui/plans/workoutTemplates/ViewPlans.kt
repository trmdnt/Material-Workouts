package eu.trmdnt.workouts.ui.plans.workoutTemplates

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import eu.trmdnt.workouts.ui.components.ConfirmDeleteDialog
import eu.trmdnt.workouts.ui.components.CreateNewItemDialog
import eu.trmdnt.workouts.ui.components.FabAction
import eu.trmdnt.workouts.ui.components.MultiItemFab
import eu.trmdnt.workouts.ui.components.SelectionContainerWithTopBar
import eu.trmdnt.workouts.ui.navigation.NavEventHandler

@Composable
fun ViewPlans(
    navEventHandler: NavEventHandler
) {
    val viewModel: ViewPlansViewmodel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.navigationEvents.collect { event ->
                navEventHandler.handle(event)
            }
        }
    }


    val expanded = remember { mutableStateOf(false) }

    SelectionContainerWithTopBar(
        itemsList = uiState.workoutTemplates,
        selectedItemsList = uiState.selectedWorkoutTemplates.toList(),
        onItemClick = {
            viewModel.handleEvent(ViewPlansEvent.OnItemPressed(it))
        },
        onLongItemClick = {
            viewModel.handleEvent(ViewPlansEvent.OnItemLongPressed(it))
        },
        onCancel = {
            viewModel.handleEvent(ViewPlansEvent.ClearSelection)
        },
        onDelete = {
            viewModel.handleEvent(ViewPlansEvent.DeleteSelected)
        },
        editMode = uiState.editMode,
        textContent = { it, selected ->
            Column {
                Text(text = it.name)
                Text(text = "Last used: ${it.lastUsed?.toString() ?: "Never"}")
            }


        },
        alternativeTopBar = null,
        fab = {
            MultiItemFab(
                listOf(
                    FabAction(
                        "create workout template",
                        Icons.Default.Add,
                        onClick = {
                            viewModel.handleEvent(ViewPlansEvent.OnCreateWorkoutTemplateButtonPressed)
                        }
                    ),
                    FabAction(
                        "create exercise template",
                        Icons.Default.Add,
                        onClick = {
                            viewModel.handleEvent(ViewPlansEvent.OnCreateExerciseButtonPressed)
                        }

                    ),
                    FabAction(
                        "edit exercise templates",
                        Icons.Default.Edit,
                        onClick = {
                            viewModel.handleEvent(ViewPlansEvent.OpenExerciseTemplates)
                        }
                    )
                ),
                expanded = expanded.value,
                onButtonPressed = {
                    expanded.value = !expanded.value
                }

            )
        },
        getId = {
            it.workoutTemplateId
        }
    )

    if (uiState.confirmDialogShown) {
        ConfirmDeleteDialog(
            text = uiState.confirmDialogText,
            onDismiss = {
                viewModel.handleEvent(ViewPlansEvent.CancelDeletion)
            },
            onConfirm = {
                viewModel.handleEvent(ViewPlansEvent.ConfirmDeletion)
            },
        )
    }

    if (uiState.createWorkoutTemplateDialogShown) {
        CreateNewItemDialog(
            text = "Create new workout template",
            onDismiss = {
                viewModel.handleEvent(ViewPlansEvent.OnCreateWorkoutTemplateDialogDismissed)
            },
            onCreate = {
                viewModel.handleEvent(ViewPlansEvent.CreateWorkoutTemplate(it))
            },

            )
    }

    if (uiState.createExerciseDialogShown) {
        CreateNewItemDialog(
            text = "Create new exercise",
            onDismiss = {
                viewModel.handleEvent(ViewPlansEvent.OnCreateExerciseDialogDismissed)
            },
            onCreate = {
                viewModel.handleEvent(ViewPlansEvent.CreateExercise(it))
            },
        )
    }
}