package eu.trmdnt.workouts.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.trmdnt.workouts.database.entities.ExerciseSet
import eu.trmdnt.workouts.database.entities.ExerciseTemplate
import eu.trmdnt.workouts.database.entities.SetType
import eu.trmdnt.workouts.ui.components.set.DistanceField
import eu.trmdnt.workouts.ui.components.set.RepsField
import eu.trmdnt.workouts.ui.components.set.TimeField
import eu.trmdnt.workouts.ui.components.set.WeightField

@Composable
fun EditSetDialog(
    onDismiss: () -> Unit,
    exerciseSet: ExerciseSet,
    exerciseTemplate: ExerciseTemplate,
    onWeightChanged: (Double) -> Unit,
    onRepsChanged: (Int) -> Unit,
    onDistanceChanged: (Double) -> Unit,
    onTimeChanged: (Int) -> Unit,
    onSetTypeChanged: (SetType) -> Unit,
    onDelete: () -> Unit
) {
    DialogBase(onDismissRequest = onDismiss) {
        EditSetDialogContents(
            onWeightChanged = onWeightChanged,
            onRepsChanged = onRepsChanged,
            onSetTypeChanged = onSetTypeChanged,
            exerciseTemplate = exerciseTemplate,
            exerciseSet = exerciseSet,
            onDistanceChanged = onDistanceChanged,
            onTimeChanged = onTimeChanged,
        )
        Row {
            Spacer(Modifier.weight(1f))
            TextButton(
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors().copy(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete")
            }
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    }
}

@Composable
fun EditSetDialogContents(
    exerciseSet: ExerciseSet,
    exerciseTemplate: ExerciseTemplate,
    onWeightChanged: (Double) -> Unit,
    onRepsChanged: (Int) -> Unit,
    onSetTypeChanged: (SetType) -> Unit,
    onDistanceChanged: (Double) -> Unit,
    onTimeChanged: (Int) -> Unit,
) {
    Column {
        if (exerciseTemplate.reps) {
            Row {
                IconButton(onClick = {
                    onRepsChanged(exerciseSet.reps - 1)
                }) {
                    Icon(
                        imageVector = Icons.Filled.Remove, contentDescription = "remove one rep"
                    )
                }
                RepsField(
                    exerciseSet = exerciseSet,
                    onRepsChanged = onRepsChanged,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    onRepsChanged(exerciseSet.reps + 1)
                }) {
                    Icon(
                        imageVector = Icons.Filled.Add, contentDescription = "add one rep"
                    )
                }
            }
        }
        if (exerciseTemplate.weight) {
            Row {
                IconButton(onClick = {
                    onWeightChanged(exerciseSet.weight - 1)
                }) {
                    Icon(
                        imageVector = Icons.Filled.Remove, contentDescription = "remove weight"
                    )
                }
                WeightField(
                    exerciseSet = exerciseSet,
                    onWeightChanged = onWeightChanged,
                    exerciseTemplate = exerciseTemplate,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = {
                    onWeightChanged(exerciseSet.weight + 1)
                }) {
                    Icon(
                        imageVector = Icons.Filled.Add, contentDescription = "add weight"
                    )
                }
            }
        }

        if (exerciseTemplate.distance) {
            DistanceField(exerciseSet, onDistanceChanged, Modifier.fillMaxWidth())
        }

        if (exerciseTemplate.time) {
            TimeField(exerciseSet, onTimeChanged, Modifier.fillMaxWidth())
        }
        Text("Set type")
        Column(modifier = Modifier.padding(start = 16.dp)) {
            SetType::class.java.enumConstants!!.forEach { enumValue ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = enumValue.name.lowercase(), modifier = Modifier.weight(1f))
                    RadioButton(
                        selected = enumValue == exerciseSet.setType,
                        onClick = { onSetTypeChanged(enumValue) },
                        enabled = true
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun EditSetDialogContentsPreview() {
    EditSetDialogContents(
        onWeightChanged = { _ -> },
        onRepsChanged = { _ -> },
        onSetTypeChanged = { _ -> },
        onDistanceChanged = { _ -> },
        onTimeChanged = { _ -> },
        exerciseSet = ExerciseSet(0, 0, 0, 0, 0.0, 0.0, 0),
        exerciseTemplate = ExerciseTemplate(
            0, "Exercise", weight = true, time = false,
            reps = true,
            distance = false,
            hidden = false,
            weightTimesTwo = true
        )
    )
}