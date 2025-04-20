package com.example.gymutil.ui.plans.exercisesTemplates

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gymutil.ui.components.SelectionContainerWithTopBar
import com.example.gymutil.ui.components.TopAppBarWithBackButton

@Composable
fun ViewExerciseTemplates(
    goToExerciseTemplate: (Long) -> Unit,
    navToCreateExerciseTemplate: () -> Unit,
    onBackPressed: () -> Unit
) {
    val viewModel: ViewExerciseTemplatesViewModel = hiltViewModel()

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
        alternativeTopBar = {
            TopAppBarWithBackButton(title = "viewing exercises", onBack = onBackPressed)
        },
        fabAction = {
            navToCreateExerciseTemplate()
        },
        getId = {
            it.exerciseTemplateId
        }
    )
}