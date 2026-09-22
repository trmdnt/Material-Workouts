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
fun DistanceField(
    exerciseSet: ExerciseSet,
    onDistanceChanged: (Double) -> Unit,
    modifier: Modifier = Modifier,
    editMode: Boolean = true,
) {
    val text = remember { mutableStateOf(exerciseSet.distance.toString()) }

    LaunchedEffect(exerciseSet.distance) {
        parseDistance(text.value)?.let {
            if (it != exerciseSet.distance) {
                text.value = exerciseSet.distance.toString()
            }
        }
    }

    val isError = remember { mutableStateOf(false) }
    TextFieldWithCustomPadding(
        value = text.value,
        onValueChange = {
            text.value = it
            val newValue = parseDistance(it)
            if (newValue == null) {
                isError.value = true
            } else {
                isError.value = false
                onDistanceChanged(newValue)
            }
        },
        isError = isError.value,
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        label = "distance",
        readOnly = !editMode,
        placeholder = "0.0",
        suffix = "m",
    )
}

private fun parseDistance(text: String): Double? {
    return (if (text.isEmpty()) "0" else text.replace(',', '.')).toDoubleOrNull()
}