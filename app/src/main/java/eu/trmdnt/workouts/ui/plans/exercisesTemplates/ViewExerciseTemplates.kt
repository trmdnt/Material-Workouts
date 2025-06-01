package eu.trmdnt.workouts.ui.plans.exercisesTemplates

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.trmdnt.workouts.ui.components.ConfirmDeleteDialog
import eu.trmdnt.workouts.ui.components.CreateNewItemDialog
import eu.trmdnt.workouts.ui.components.SelectionContainerWithTopBar
import eu.trmdnt.workouts.ui.components.TopAppBarWithBackButton

@Composable
fun ViewExerciseTemplates(
    goToExerciseTemplate: (Long) -> Unit, onBackPressed: () -> Unit
) {
    val viewModel: ViewExerciseTemplatesViewModel = hiltViewModel()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    viewModel.navigateToExerciseTemplateId.collectAsState().let { navigateToExerciseTemplateId ->
        val id = navigateToExerciseTemplateId.value
        if (id != null) {
            viewModel.receivedNavEvent()
            goToExerciseTemplate(id)
        }
    }

    SelectionContainerWithTopBar(
        itemsList = uiState.exerciseTemplates,
        selectedItemsList = uiState.selectedExerciseTemplates,
        onItemClick = {
            viewModel.onItemPressed(it)
        },
        onLongItemClick = {
            viewModel.onLongItemPressed(it)
        },
        onCancel = {
            viewModel.onCancelselectionPressed()
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
        alternativeTopBar = {
            TopAppBarWithBackButton(title = "viewing exercises", onBack = onBackPressed)
        },
        fabAction = {
            viewModel.onCreateExerciseButtonPressed()
        },
        getId = {
            it.exerciseTemplateId
        })

    if (uiState.confirmDialogShow) {
        ConfirmDeleteDialog(
            text = uiState.confirmDialogText,
            onDismiss = { viewModel.onCancelDialogPressed() },
            onConfirm = { viewModel.onConfirmButtonPressed() })
    }

    if (uiState.createExerciseDialogShown) {
        CreateNewItemDialog(
            text = "Create a new exercise",
            onDismiss = { viewModel.onCreateExerciseDialogDismissed() },
            onCreate = { viewModel.onCreateExercise(it) }
        )
    }
}