package com.example.gymutil.ui.plans.exercisesTemplates

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gymutil.ui.components.TopAppBarWithBackButton

@Composable
fun EditExerciseTemplate(exerciseTemplateId: Long, onBackPressed: () -> Unit) {
    val viewModel: EditExerciseTemplateViewModel =
        hiltViewModel<EditExerciseTemplateViewModel, EditExerciseTemplateViewModel.EditExerciseTemplateViewModelFactory> { factory ->
            factory.create(exerciseTemplateId)
        }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(topBar = {
        TopAppBarWithBackButton(title = "Edit exercise", onBack = onBackPressed)
    }) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(4.dp)
        ) {
            OutlinedTextField(
                value = uiState.nameTextField, singleLine = true, onValueChange = {
                    viewModel.onNameChange(it)
                }, keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ), keyboardActions = KeyboardActions(
                    onDone = {
                        viewModel.onSaveButtonPressed()
                    }), modifier = Modifier.fillMaxWidth()
            )
            Row {
                Text("reps", modifier = Modifier.weight(1F))
                Checkbox(
                    checked = uiState.repsSelected,
                    onCheckedChange = {
                        viewModel.onRepsSelected()
                    },
                )
            }
            Row {
                Text("weight", modifier = Modifier.weight(1F))
                Checkbox(
                    checked = uiState.weightSelected,
                    onCheckedChange = {
                        viewModel.onWeightSelected()
                    },
                )
            }
            Row {
                Text("time (not implemented)", modifier = Modifier.weight(1F))
                Checkbox(
                    checked = uiState.timeSelected,
                    onCheckedChange = {
                        viewModel.onTimeSelected()
                    },
                )
            }
            Row {
                Text("distance (not implemented)", modifier = Modifier.weight(1F))
                Checkbox(
                    checked = uiState.distanceSelected,
                    onCheckedChange = {
                        viewModel.onDistanceSelected()
                    },
                )
            }
            Button(onClick = {
                viewModel.onSaveButtonPressed()
            }, enabled = uiState.saveButtonEnabled) {
                Text("Save")
            }
        }
    }
}