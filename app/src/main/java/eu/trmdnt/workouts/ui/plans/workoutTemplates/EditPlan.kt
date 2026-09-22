package eu.trmdnt.workouts.ui.plans.workoutTemplates

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.trmdnt.workouts.database.entities.ExerciseTemplate
import eu.trmdnt.workouts.ui.components.TopAppBarWithBackButton
import eu.trmdnt.workouts.ui.components.selection.ItemsList
import eu.trmdnt.workouts.ui.navigation.NavEventHandler

@Composable
fun EditPlan(workoutTemplateId: Long, navEventHandler: NavEventHandler) {
    val viewModel =
        hiltViewModel<EditPlanViewModel, EditPlanViewModel.EditPlanViewModelFactory> { factory ->
            factory.create(workoutTemplateId)
        }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(containerColor = Color.Transparent, topBar = {
        TopAppBarWithBackButton(title = "edit workout template", {
            navEventHandler.goBack()
        })
    }, content = {
        Column(modifier = Modifier.padding(it)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(10.dp)
            ) {
                val text = remember { mutableStateOf(uiState.nameTextField ?: "") }

                //TODO this seems janky
                LaunchedEffect(uiState.nameTextField != null) {
                    text.value = uiState.nameTextField ?: ""
                }

                OutlinedTextField(
                    value = text.value, singleLine = true, onValueChange = { nText ->
                        text.value = nText
                        viewModel.onNameFieldChange(nText)
                    }, keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ), modifier = Modifier.weight(1f)

                )
            }
            ExerciseList(uiState.exercises, uiState.selected) {
                viewModel.onExercisePressed(it)
            }

        }
    })
}

@Composable
fun ExerciseList(
    exerciseList: List<ExerciseTemplate>,
    selectedList: List<ExerciseTemplate>,
    onItemPress: (Long) -> Unit
) {
    if (exerciseList.isNotEmpty()) {
        Row(modifier = Modifier.padding(10.dp, 0.dp)) {
            Text("selected exercises:", modifier = Modifier.weight(1f))
        }

        ItemsList(itemsList = exerciseList, selectedItemsList = selectedList, onItemPress = {
            onItemPress(it.exerciseTemplateId)
        }, onLongItemPress = {
            onItemPress(it.exerciseTemplateId)
        }, paddingValues = PaddingValues.Zero, editMode = true, textContent = { item, _ ->
            Text(text = item.name)
        }, getId = {
            it.exerciseTemplateId
        })
    } else {
        Text("no exercises, start by creating an exercise")
    }
}