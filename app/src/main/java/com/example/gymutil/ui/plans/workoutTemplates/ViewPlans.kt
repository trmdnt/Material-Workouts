package com.example.gymutil.ui.plans.workoutTemplates

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gymutil.ui.components.ConfirmDeleteBottomSheet
import com.example.gymutil.ui.components.SelectionContainerWithTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewPlans(
    goToWorkoutTemplate: (workoutTemplateId: Long) -> Unit, navToCreateWorkoutTemplate: () -> Unit
) {
    val viewModel: ViewPlansViewmodel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    viewModel.navigateToWorkoutTemplateId.collectAsState().let { navigateToWorkoutTemplateId ->
        navigateToWorkoutTemplateId.value?.let {
            viewModel.receivedNavEvent()
            goToWorkoutTemplate(it)
        }
    }

    SelectionContainerWithTopBar(
        itemsList = uiState.workoutTemplates,
        selectedItemsList = uiState.selectedWorkoutTemplates,
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
                Text(text = "Last used: ${it.lastUsed?.toString() ?: "Never"}")
            }
        },
        alternativeTopBar = null,
        fabAction = navToCreateWorkoutTemplate,
        getId = {
            it.workoutTemplateId
        }
    )

    if (uiState.confirmBottomSheetShown) {
        ConfirmDeleteBottomSheet(text = uiState.confirmBottomSheetText, onDismiss = {
            viewModel.onCancelSheetPressed()
        }, onConfirm = {
            viewModel.onConfirmButtonPressed()
        })
    }
}