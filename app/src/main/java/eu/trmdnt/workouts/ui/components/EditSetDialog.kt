package eu.trmdnt.workouts.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.trmdnt.workouts.R
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
                Text(stringResource(R.string.delete))
            }
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
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
                        imageVector = Icons.Filled.Remove, contentDescription = stringResource(R.string.remove_one_rep)
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
                        imageVector = Icons.Filled.Add, contentDescription = stringResource(R.string.add_one_rep)
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
                        imageVector = Icons.Filled.Remove, contentDescription = stringResource(R.string.remove_weight)
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
                        imageVector = Icons.Filled.Add, contentDescription = stringResource(R.string.add_weight)
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
        Text(stringResource(R.string.set_type))
        Column(modifier = Modifier.padding(start = 16.dp)) {
            //TODO extract composable
            SetType.entries.forEach { enumValue ->
                val text = when (enumValue) {
                    SetType.Default -> stringResource(R.string.default_)
                    SetType.WarmUp -> stringResource(R.string.warm_up)
                    SetType.Drop -> stringResource(R.string.drop_set)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = text, modifier = Modifier.weight(1f))
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