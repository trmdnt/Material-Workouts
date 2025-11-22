package eu.trmdnt.workouts.ui.plans.workoutTemplates

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.trmdnt.workouts.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewPlans(
    goToWorkoutTemplate: (workoutTemplateId: Long) -> Unit,
    goToExerciseTemplate: (exerciseTemplateId: Long) -> Unit,
    goToViewExerciseTemplates: () -> Unit,
) {
    val viewModel: ViewPlansViewmodel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    viewModel.navigateToWorkoutTemplateId.collectAsState().let { navigateToWorkoutTemplateId ->
        navigateToWorkoutTemplateId.value?.let {
            viewModel.receivedNavEvent()
            goToWorkoutTemplate(it)
        }
    }

    viewModel.navigateToExerciseTemplateId.collectAsState().let { navigateToExerciseTemplateId ->
        navigateToExerciseTemplateId.value?.let {
            viewModel.receivedNavEvent()
            goToExerciseTemplate(it)
        }
    }

    val expanded = remember { mutableStateOf(false) }

    SelectionContainerWithTopBar(
        itemsList = uiState.workoutTemplates,
        selectedItemsList = uiState.selectedWorkoutTemplates,
        onItemClick = viewModel::onItemPressed,
        onLongItemClick = viewModel::onLongItemPressed,
        onCancel = viewModel::onCancelButtonPressed,
        onDelete = viewModel::onDeleteButtonPressed,
        editMode = uiState.editMode,
        textContent = { it, mod ->
            Column(
                modifier = mod
                    .padding(8.dp)
            ) {
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
                        onClick = viewModel::onCreateWorkoutTemplateButtonPressed
                    ),
                    FabAction(
                        "create exercise template",
                        Icons.Default.Add,
                        onClick = viewModel::onCreateExerciseButtonPressed

                    ),
                    FabAction(
                        "edit exercise templates",
                        Icons.Default.Edit,
                        onClick = goToViewExerciseTemplates
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
            onDismiss = viewModel::onCancelDialogPressed,
            onConfirm = viewModel::onConfirmButtonPressed
        )
    }

    if (uiState.createWorkoutTemplateDialogShown) {
        CreateNewItemDialog(
            text = "Create new workout template",
            onDismiss = viewModel::onCreateWorkoutTemplateDialogDismissed,
            onCreate = viewModel::onCreateWorkoutTemplate

        )
    }

    if (uiState.createExerciseDialogShown) {
        CreateNewItemDialog(
            text = "Create new exercise",
            onDismiss = viewModel::onCreateExerciseDialogDismissed,
            onCreate = viewModel::onCreateExercise
        )
    }
}