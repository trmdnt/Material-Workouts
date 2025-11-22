package eu.trmdnt.workouts.ui.plans.workoutTemplates

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.trmdnt.workouts.database.entities.ExerciseTemplate
import eu.trmdnt.workouts.ui.components.TopAppBarWithBackButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPlan(workoutTemplateId: Long, onBackPressed: () -> Unit, navToViewExercises: () -> Unit) {
    val viewModel = hiltViewModel<EditPlanViewModel, EditPlanViewModel.EditPlanViewModelFactory> { factory ->
        factory.create(workoutTemplateId)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    uiState.nameTextField


    Scaffold(topBar = {
        TopAppBarWithBackButton(title = "edit workout template", onBackPressed)
    }, content = {
        Column(modifier = Modifier.padding(it)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.nameTextField, singleLine = true, onValueChange = {
                        viewModel.onNameFieldChange(it)
                    }, keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ), keyboardActions = KeyboardActions(
                        onDone = {
                            viewModel.onSaveNameButtonClick()
                        }), modifier = Modifier.weight(1f)

                )
                Button(onClick = { viewModel.onSaveNameButtonClick() }, enabled = uiState.saveNameButtonEnabled) {
                    Text("Save")
                }
            }
            Button(onClick = navToViewExercises) {
                Text("view exercises")
            }
            ExerciseList(uiState.exercises, uiState.selected) {
                viewModel.onExercisePressed(it)
            }

        }
    })
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExerciseList(
    exerciseList: List<ExerciseTemplate>, selectedList: List<ExerciseTemplate>, onItemPress: (Long) -> Unit
) {
    if (exerciseList.isNotEmpty()) {
        Text("selected exercises:")
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(
                items = exerciseList, key = {
                    it.exerciseTemplateId
                }) { exerciseTemplate ->
                ExerciseTemplateListItem(
                    onItemPress = { onItemPress(exerciseTemplate.exerciseTemplateId) },
                    exerciseTemplate = exerciseTemplate,
                    selected = selectedList
                )
            }
        }
    } else {
        Text("no exercises")
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExerciseTemplateListItem(
    onItemPress: () -> Unit, exerciseTemplate: ExerciseTemplate, selected: List<ExerciseTemplate>
) {
    Card(
        modifier = Modifier
            .combinedClickable(
                onClick = onItemPress
            )
            .fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp, 0.dp, 8.dp, 0.dp)) {
            Text(text = exerciseTemplate.name, modifier = Modifier.weight(1f))
            Checkbox(checked = selected.find { it.exerciseTemplateId == exerciseTemplate.exerciseTemplateId } != null,
                onCheckedChange = { onItemPress() })
        }
    }
}