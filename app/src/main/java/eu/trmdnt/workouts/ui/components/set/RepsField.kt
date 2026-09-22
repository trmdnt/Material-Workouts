package eu.trmdnt.workouts.ui.components.set

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import eu.trmdnt.workouts.database.entities.ExerciseSet
import eu.trmdnt.workouts.ui.components.TextFieldWithCustomPadding

@Composable
fun RepsField(
    exerciseSet: ExerciseSet,
    onRepsChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    editMode: Boolean = true,
) {
    val text = remember { mutableStateOf(exerciseSet.reps.toString()) }

    LaunchedEffect(exerciseSet.reps) {
        parseReps(text.value)?.let {
            if (it != exerciseSet.reps) {
                text.value = exerciseSet.reps.toString()
            }
        }
    }

    val isError = remember { mutableStateOf(false) }
    TextFieldWithCustomPadding(
        value = text.value,
        onValueChange = {
            text.value = it
            val newValue = parseReps(it)
            if (newValue == null) {
                isError.value = true
            } else {
                isError.value = false
                onRepsChanged(newValue)
            }
        },
        isError = isError.value,
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        label = "reps",
        readOnly = !editMode,
        placeholder = "0"
    )
}

private fun parseReps(text: String): Int? {
    return (text.ifEmpty { "0" }).toIntOrNull()
}