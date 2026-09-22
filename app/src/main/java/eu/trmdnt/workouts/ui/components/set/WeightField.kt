package eu.trmdnt.workouts.ui.components.set

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import eu.trmdnt.workouts.database.entities.ExerciseSet
import eu.trmdnt.workouts.database.entities.ExerciseTemplate
import eu.trmdnt.workouts.ui.components.TextFieldWithCustomPadding

@Composable
fun WeightField(
    exerciseSet: ExerciseSet,
    onWeightChanged: (Double) -> Unit,
    exerciseTemplate: ExerciseTemplate,
    modifier: Modifier = Modifier,
    editMode: Boolean = true,
) {
    val text = remember { mutableStateOf(exerciseSet.weight.toString()) }

    LaunchedEffect(exerciseSet.weight) {
        parseWeight(text.value)?.let {
            if (it != exerciseSet.weight) {
                text.value = exerciseSet.weight.toString()
            }
        }
    }

    val isError = remember { mutableStateOf(false) }
    TextFieldWithCustomPadding(
        value = text.value,
        onValueChange = {
            text.value = it
            val newValue = parseWeight(it)
            if (newValue == null) {
                isError.value = true
            } else {
                isError.value = false
                onWeightChanged(newValue)
            }
        },
        isError = isError.value,
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        label = "weight",
        readOnly = !editMode,
        placeholder = "0.0",
        suffix = "kg" + if (exerciseTemplate.weightTimesTwo) " × 2" else "",
    )
}

private fun parseWeight(text: String): Double? {
    return (if (text.isEmpty()) "0" else text.replace(',', '.')).toDoubleOrNull()
}