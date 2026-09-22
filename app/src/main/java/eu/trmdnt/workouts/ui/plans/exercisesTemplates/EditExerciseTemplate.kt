package eu.trmdnt.workouts.ui.plans.exercisesTemplates

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.trmdnt.workouts.ui.components.TopAppBarWithBackButton
import eu.trmdnt.workouts.ui.navigation.NavEventHandler

//TODO make composable independant of viewModel
@Composable
fun EditExerciseTemplate(exerciseTemplateId: Long, navEventHandler: NavEventHandler) {
    val viewModel: EditExerciseTemplateViewModel =
        hiltViewModel<EditExerciseTemplateViewModel, EditExerciseTemplateViewModel.EditExerciseTemplateViewModelFactory> { factory ->
            factory.create(exerciseTemplateId)
        }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(containerColor = Color.Transparent, topBar = {
        TopAppBarWithBackButton(title = "Edit exercise", onBack = {
            navEventHandler.goBack()
        })
    }) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(4.dp)
        ) {
            uiState.exerciseTemplate?.let { exerciseTemplate ->
                val text = remember { mutableStateOf(exerciseTemplate.name) }
                OutlinedTextField(
                    value = text.value, singleLine = true, onValueChange = {
                        text.value = it
                        viewModel.onNameChange(it)
                    }, keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ), modifier = Modifier.fillMaxWidth()
                )
                Row {
                    Text("reps", modifier = Modifier.weight(1F))
                    Checkbox(
                        checked = exerciseTemplate.reps,
                        onCheckedChange = {
                            viewModel.onRepsSelected()
                        },
                    )
                }
                Row {
                    Text("weight", modifier = Modifier.weight(1F))
                    Checkbox(
                        checked = exerciseTemplate.weight,
                        onCheckedChange = {
                            viewModel.onWeightSelected()
                        },
                    )
                }
                if (exerciseTemplate.weight) {
                    Row {
                        Text("2x weight", modifier = Modifier.weight(1F))
                        Checkbox(
                            checked = exerciseTemplate.weightTimesTwo,
                            onCheckedChange = {
                                viewModel.onWeightTimesTwoSelected()
                            },
                        )
                    }
                }
                Row {
                    Text("time", modifier = Modifier.weight(1F))
                    Checkbox(
                        checked = exerciseTemplate.time,
                        onCheckedChange = {
                            viewModel.onTimeSelected()
                        },
                    )
                }
                Row {
                    Text("distance", modifier = Modifier.weight(1F))
                    Checkbox(
                        checked = exerciseTemplate.distance,
                        onCheckedChange = {
                            viewModel.onDistanceSelected()
                        },
                    )
                }
            }
        }
    }
}