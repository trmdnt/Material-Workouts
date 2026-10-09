package eu.trmdnt.workouts.ui.plans.workoutTemplates

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import eu.trmdnt.workouts.R
import eu.trmdnt.workouts.ui.components.*
import eu.trmdnt.workouts.ui.navigation.NavEventHandler
import java.time.ZoneId
import java.time.format.FormatStyle

@Composable
fun ViewPlans(
    navEventHandler: NavEventHandler
) {
    val viewModel: ViewPlansViewmodel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.navigationEvents.collect { event ->
                navEventHandler.handle(event)
            }
        }
    }


    val expanded = remember { mutableStateOf(false) }

    SelectionContainerWithTopBar(
        itemsList = uiState.workoutTemplates.map { it.workoutTemplate },
        selectedItemsList = uiState.selectedWorkoutTemplates.toList(),
        onItemClick = {
            viewModel.handleEvent(ViewPlansEvent.OnItemPressed(it))
        },
        onLongItemClick = {
            viewModel.handleEvent(ViewPlansEvent.OnItemLongPressed(it))
        },
        onCancel = {
            viewModel.handleEvent(ViewPlansEvent.ClearSelection)
        },
        onDelete = {
            viewModel.handleEvent(ViewPlansEvent.DeleteSelected)
        },
        editMode = uiState.editMode,
        textContent = { it, selected ->
            Column {
                Text(text = it.name)
                //TODO fix jank
                val lastUsed = uiState.workoutTemplates.find { s -> s.workoutTemplate == it }?.lastUsed?.let {
                    java.time.format.DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
                        .withLocale(LocalLocale.current.platformLocale).withZone(
                            ZoneId.systemDefault()
                        )
                        .format(java.time.Instant.ofEpochSecond(it))

                }
                Text(
                    text = stringResource(
                        R.string.last_used,
                        lastUsed ?: stringResource(R.string.never)
                    )
                )
            }


        },
        alternativeTopBar = null,
        fab = {
            MultiItemFab(
                listOf(
                    FabAction(
                        "create workout template",
                        Icons.Default.Add,
                        onClick = {
                            viewModel.handleEvent(ViewPlansEvent.OnCreateWorkoutTemplateButtonPressed)
                        }
                    ),
                    FabAction(
                        "create exercise template",
                        Icons.Default.Add,
                        onClick = {
                            viewModel.handleEvent(ViewPlansEvent.OnCreateExerciseButtonPressed)
                        }

                    ),
                    FabAction(
                        "edit exercise templates",
                        Icons.Default.Edit,
                        onClick = {
                            viewModel.handleEvent(ViewPlansEvent.OpenExerciseTemplates)
                        }
                    )
                ),
                expanded = expanded.value,
                onButtonPressed = {
                    expanded.value = !expanded.value
                }

            )
        },
        getId = {
            it.workoutTemplateId
        }
    )

    if (uiState.confirmDialogShown) {
        ConfirmDeleteDialog(
            text = stringResource(
                R.string.do_you_really_want_to_delete_the_selected_item_s,
                uiState.selectedWorkoutTemplates.size
            ),
            onDismiss = {
                viewModel.handleEvent(ViewPlansEvent.CancelDeletion)
            },
            onConfirm = {
                viewModel.handleEvent(ViewPlansEvent.ConfirmDeletion)
            },
        )
    }

    if (uiState.createWorkoutTemplateDialogShown) {
        CreateNewItemDialog(
            text = stringResource(R.string.create_new_workout_template),
            onDismiss = {
                viewModel.handleEvent(ViewPlansEvent.OnCreateWorkoutTemplateDialogDismissed)
            },
            onCreate = {
                viewModel.handleEvent(ViewPlansEvent.CreateWorkoutTemplate(it))
            },

            )
    }

    if (uiState.createExerciseDialogShown) {
        CreateNewItemDialog(
            text = stringResource(R.string.create_new_exercise),
            onDismiss = {
                viewModel.handleEvent(ViewPlansEvent.OnCreateExerciseDialogDismissed)
            },
            onCreate = {
                viewModel.handleEvent(ViewPlansEvent.CreateExercise(it))
            },
        )
    }
}