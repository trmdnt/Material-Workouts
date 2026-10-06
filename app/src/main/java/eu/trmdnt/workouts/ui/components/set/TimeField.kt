package eu.trmdnt.workouts.ui.components.set

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import eu.trmdnt.workouts.R
import eu.trmdnt.workouts.database.entities.ExerciseSet
import eu.trmdnt.workouts.ui.components.SelectTimespanDialog
import eu.trmdnt.workouts.ui.components.TextFieldWithCustomPadding

@Composable
fun TimeField(
    exerciseSet: ExerciseSet,
    onTimeChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    editMode: Boolean = true,
) {
    val timeDialogOpen = remember { mutableStateOf(false) }
    val seconds = (exerciseSet.time % 60).toString().padStart(2, '0')
    val minutes = (exerciseSet.time / 60).toString().padStart(2, '0')

    val interactionSource = remember { MutableInteractionSource() }
    val currentEditMode = rememberUpdatedState(editMode)

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect {
            if (it is PressInteraction.Release && currentEditMode.value) {
                timeDialogOpen.value = true
            }
        }
    }

    TextFieldWithCustomPadding(
        value = "${minutes}:${seconds}",
        onValueChange = {},
        modifier = modifier,
        label = stringResource(R.string.time),
        readOnly = true,
        interactionSource = interactionSource,
        showReadOnly = !editMode
    )

    if (timeDialogOpen.value) {
        //TODO do not use toInt
        SelectTimespanDialog(exerciseSet.time.toInt(), onTimeChanged, {
            timeDialogOpen.value = false
        })
    }
}