package eu.trmdnt.workouts.ui.plans.exercisesTemplates

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import eu.trmdnt.workouts.ui.components.ConfirmDeleteDialog
import eu.trmdnt.workouts.ui.components.CreateNewItemDialog
import eu.trmdnt.workouts.ui.components.SelectionContainerWithTopBar
import eu.trmdnt.workouts.ui.components.TopAppBarWithBackButton
import eu.trmdnt.workouts.ui.navigation.NavEventHandler

@Composable
fun ViewExerciseTemplates(
    navEventHandler: NavEventHandler
) {
    val viewModel: ViewExerciseTemplatesViewModel = hiltViewModel()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.navigationEvents.collect { event ->
                navEventHandler.handle(event)
            }
        }
    }


    SelectionContainerWithTopBar(
        itemsList = uiState.exerciseTemplates,
        selectedItemsList = uiState.selectedExerciseTemplates.toList(),
        onItemClick = {
            viewModel.handleEvent(ViewExerciseTemplatesEvent.OnItemPressed(it))
        },
        onLongItemClick = {
            viewModel.handleEvent(ViewExerciseTemplatesEvent.OnItemLongPressed(it))
        },
        onCancel = {
            viewModel.handleEvent(ViewExerciseTemplatesEvent.ClearSelection)
        },
        onDelete = {
            viewModel.handleEvent(ViewExerciseTemplatesEvent.DeleteSelected)
        },
        editMode = uiState.editMode,
        textContent = { it, selected ->
            Text(text = it.name)
        },
        alternativeTopBar = {
            TopAppBarWithBackButton(title = "viewing exercises", onBack = {
                navEventHandler.goBack()
            })
        },
        fab = {
            FloatingActionButton(onClick = {
                viewModel.handleEvent(ViewExerciseTemplatesEvent.OnCreateExerciseTemplateButtonPressed)
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
            }
        },
        getId = {
            it.exerciseTemplateId
        })

    if (uiState.confirmDialogShow) {
        ConfirmDeleteDialog(
            text = uiState.confirmDialogText,
            onDismiss = { viewModel.handleEvent(ViewExerciseTemplatesEvent.CancelDeletion) },
            onConfirm = { viewModel.handleEvent(ViewExerciseTemplatesEvent.ConfirmDeletion) })
    }

    if (uiState.createExerciseDialogShown) {
        CreateNewItemDialog(
            text = "Create a new exercise",
            onDismiss = { viewModel.handleEvent(ViewExerciseTemplatesEvent.OnCreateExerciseTemplateDialogDismissed) },
            onCreate = { viewModel.handleEvent(ViewExerciseTemplatesEvent.CreateExerciseTemplates(it)) }
        )
    }
}