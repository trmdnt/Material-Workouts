package eu.trmdnt.workouts.ui.plans.workoutTemplates

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CreatePlan(
    onCreateWorkoutTemplate: (id: Long) -> Unit
) {
    val viewModel =
        hiltViewModel<CreatePlanDialogViewModel, CreatePlanDialogViewModel.CreatePlanDialogViewModelFactory> { factory ->
            factory.create(onCreateWorkoutTemplate)
        }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
    ) {
        OutlinedTextField(
            value = uiState.text,
            singleLine = true,
            onValueChange = {
                viewModel.textEdit(it)
            },
            isError = uiState.isError,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    viewModel.createButtonPressed()
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp, 8.dp, 8.dp, 0.dp)

        )
        Button(
            onClick = {
                viewModel.createButtonPressed()
            },
            modifier = Modifier
                .padding(8.dp)
        ) {
            Text("create")
        }
    }
}