package eu.trmdnt.workouts.ui.activities

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.trmdnt.workouts.database.entities.ExerciseSet
import eu.trmdnt.workouts.database.entities.ExerciseTemplate
import eu.trmdnt.workouts.database.entities.ExerciseWithSets
import eu.trmdnt.workouts.ui.components.ListAvailableItems
import eu.trmdnt.workouts.ui.components.SwipeToDeleteContainer
import eu.trmdnt.workouts.ui.components.TopAppBarWithBackButton
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ViewWorkout(
    workoutId: Long,
    editing: Boolean,
    onBackPressed: () -> Unit,
    navigateToExerciseStatistics: (Long) -> Unit,
    navigateToEditExercise: (Long) -> Unit
) {
    val viewModel = hiltViewModel<ViewWorkoutViewModel, ViewWorkoutViewModel.ViewWorkoutViewModelFactory> {
        it.create(workoutId, editing)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.goBack.collectAsStateWithLifecycle().let {
        if (it.value) {
            onBackPressed()
        }
    }

    viewModel.navigateToExerciseStatistics.collectAsStateWithLifecycle().let {
        it.value?.let { exerciseTemplateId ->
            viewModel.onNavigate()
            navigateToExerciseStatistics(exerciseTemplateId)
        }
    }

    viewModel.navigateToEditExerciseTemplate.collectAsStateWithLifecycle().let {
        it.value?.let { exerciseTemplateId ->
            viewModel.onNavigate()
            navigateToEditExercise(exerciseTemplateId)
        }
    }

    Scaffold(snackbarHost = {
        SnackbarHost(hostState = snackbarHostState)
    }, topBar = {
        TopAppBarWithBackButton(title = uiState.topBarTitle, onBackPressed, onAlternativeAction = {
            viewModel.onEditButtonPressed()
        }, alternativeIcon = {
            if (!uiState.editMode) {
                Icon(Icons.Filled.Edit, contentDescription = "enter edit mode")
            } else {
                Icon(Icons.Filled.EditOff, contentDescription = "exit edit mode")
            }
        })
    }, floatingActionButton = {
        if (uiState.editMode) {
            FloatingActionButton(onClick = {
                viewModel.onNewExercisePressed()
            }, content = {
                Icon(Icons.Filled.Add, contentDescription = "Add Exercise")
            })
        }
    }, content = {
        Column(
            modifier = Modifier.padding(it)
        ) {
            uiState.workoutName?.let {
                val text = remember { mutableStateOf(it) }
                OutlinedTextField(
                    value = text.value, label = {
                        Text("workout name")
                    }, onValueChange = {
                        viewModel.onWorkoutRename(it)
                        text.value = it
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp), readOnly = !uiState.editMode
                )
            }


            Row(modifier = Modifier.weight(1f)) {
                ExerciseList(
                    uiState.exercises,
                    onSetAdded = {
                        viewModel.onSetAdded(it)
                    },
                    editMode = uiState.editMode,
                    onWeightChanged = { setId, exerciseId, value ->
                        viewModel.onWeightChanged(setId, exerciseId, value)
                    },
                    onRepsChanged = { setId, exerciseId, value ->
                        viewModel.onRepsChanged(setId, exerciseId, value)
                    },
                    onDistanceChanged = { setId, exerciseId, value ->
                        viewModel.onDistanceChanged(setId, exerciseId, value)
                    },
                    onTimeChanged = { setId, exerciseId, value ->
                        viewModel.onTimeChanged(setId, exerciseId, value)
                    },
                    onDeleteExercisePressed = {
                        viewModel.onDeleteExercisePressed(it)
                    },
                    onDeleteSetPressed = {
                        viewModel.onDeleteSetPressed(it)
                    },
                    onIgnoreAction = { setId, exerciseId ->
                        viewModel.onSetIgnoreAction(setId, exerciseId)
                    },
                    onInfoPressed = {
                        viewModel.onExerciseInfoPressed(it)
                    },
                    onEditPressed = {
                        viewModel.onExerciseEditPressed(it)
                    },
                )
            }

            AnimatedVisibility(visible = uiState.editMode) {
                if (uiState.recommendedExercises.isNotEmpty()) {
                    Column(modifier = Modifier.padding(0.dp)) {
                        FlowRow {
                            for (exerciseTemplate in uiState.recommendedExercises) {
                                TextButton(onClick = {
                                    viewModel.addExercise(exerciseTemplate)
                                }) {
                                    Text(text = exerciseTemplate.name)
                                }
                            }
                        }
                    }
                }
            }


            if (uiState.displaySelectExerciseBottomSheet) {
                val sheetState = rememberModalBottomSheetState()
                ModalBottomSheet(
                    onDismissRequest = {
                        viewModel.onSelectExerciseBottomSheetDismissed()
                    }, sheetState = sheetState
                ) {
                    ListAvailableItems(items = uiState.allAvailableExercises, onItemClick = {
                        viewModel.addExercise(it)
                        viewModel.onSelectExerciseBottomSheetDismissed()
                    }, getName = {
                        it.name
                    })
                }
            }


        }
    })

    //TODO understand LaunchedEffect
    if (uiState.showUndoSnackBar) {
        LaunchedEffect(uiState.showUndoSnackBar) {
            try {
                val result = snackbarHostState.showSnackbar(
                    message = uiState.undoSnackBarMessage.toString(), actionLabel = "Undo",
                    // Defaults to SnackbarDuration.Short
                    duration = SnackbarDuration.Short
                )
                when (result) {
                    SnackbarResult.ActionPerformed -> {
                        viewModel.onUndoPressed()
                    }

                    SnackbarResult.Dismissed -> {/* Handle snackbar dismissed */
                    }
                }
            } finally {
                viewModel.onSnackBarDismissed()
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExerciseList(
    items: List<ExerciseWithSets>,
    onSetAdded: (Long) -> Unit,
    onWeightChanged: (Long, Long, Double) -> Unit,
    onRepsChanged: (Long, Long, Int) -> Unit,
    onDistanceChanged: (Long, Long, Double) -> Unit,
    onTimeChanged: (Long, Long, Long) -> Unit,
    editMode: Boolean,
    onDeleteExercisePressed: (Long) -> Unit,
    onDeleteSetPressed: (Long) -> Unit,
    onIgnoreAction: (Long, Long) -> Unit,
    onInfoPressed: (Long) -> Unit,
    onEditPressed: (Long) -> Unit
) {
    if (items.isEmpty()) {
        if (editMode) {
            Text("add an exercise to get started")
        } else {
            Text("this workout is empty")
        }

    } else {
        val listState = rememberLazyListState()

        //TODO janky and breaks animation (at this point, why even have the expand animation?)
        LaunchedEffect(editMode) {
            if (editMode) {
                val atBottom =
                    listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index == listState.layoutInfo.totalItemsCount - 1
                if (atBottom) {
                    delay(300)
                    listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
                }
            }
        }

        LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            itemsIndexed(
                items = items, key = { _, item -> item.exercise.exerciseId }) { index, exerciseWithSets ->
                SwipeToDeleteContainer(onDelete = {
                    onDeleteExercisePressed(exerciseWithSets.exercise.exerciseId)
                }, enabled = editMode) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        Column(modifier = Modifier.padding(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = exerciseWithSets.exerciseTemplate.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier
                                        .padding(0.dp, 0.dp, 0.dp, 0.dp)
                                        .weight(1f)
                                )

                                IconButton(onClick = {
                                    onInfoPressed(exerciseWithSets.exerciseTemplate.exerciseTemplateId)
                                }) {
                                    Icon(Icons.Default.Info, contentDescription = "exercise info")
                                }

                                AnimatedVisibility(visible = editMode) {
                                    IconButton(onClick = {
                                        onEditPressed(exerciseWithSets.exerciseTemplate.exerciseTemplateId)
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "edit exercise")

                                    }
                                }

                                AnimatedVisibility(visible = editMode) {
                                    IconButton(onClick = {
                                        onSetAdded(exerciseWithSets.exercise.exerciseId)
                                    }) {
                                        Icon(Icons.Default.Add, contentDescription = "add a new set")
                                    }
                                }


                            }

                            if (exerciseWithSets.exerciseSets.isEmpty()) {
                                Text("no sets yet")
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    exerciseWithSets.exerciseSets.fastForEach { set ->
                                        SetListItem(
                                            exerciseSet = set,
                                            exerciseTemplate = exerciseWithSets.exerciseTemplate,
                                            onWeightChanged = {
                                                onWeightChanged(set.id, exerciseWithSets.exercise.exerciseId, it)
                                            },
                                            onRepsChanged = {
                                                onRepsChanged(set.id, exerciseWithSets.exercise.exerciseId, it)
                                            },
                                            onDistanceChanged = {
                                                onDistanceChanged(set.id, exerciseWithSets.exercise.exerciseId, it)
                                            },
                                            onTimeChanged = {
                                                onTimeChanged(set.id, exerciseWithSets.exercise.exerciseId, it)
                                            },
                                            onDeleteSetPressed = {
                                                onDeleteSetPressed(set.id)
                                            },
                                            editMode = editMode,
                                            onIgnoreAction = {
                                                onIgnoreAction(set.id, exerciseWithSets.exercise.exerciseId)
                                            })
                                    }
                                }
                            }
                        }
                    }
                }
                if (index != items.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun SetListItem(
    exerciseSet: ExerciseSet,
    exerciseTemplate: ExerciseTemplate,
    onWeightChanged: (Double) -> Unit,
    onRepsChanged: (Int) -> Unit,
    onDistanceChanged: (Double) -> Unit,
    onTimeChanged: (Long) -> Unit,
    onDeleteSetPressed: () -> Unit,
    onIgnoreAction: () -> Unit,
    editMode: Boolean
) {
    key(exerciseSet.id) {
        SwipeToDeleteContainer(onDelete = onDeleteSetPressed, onIgnoreAction = onIgnoreAction, enabled = editMode) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .alpha(if (exerciseSet.ignoreInStat) 0.6f else 1f)
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    AnimatedVisibility(
                        modifier = Modifier.padding(horizontal = 8.dp), visible = exerciseSet.ignoreInStat
                    ) {
                        Icon(Icons.Default.VisibilityOff, "this item will not be showed in statistics")
                    }

                    if (exerciseTemplate.reps) {
                        val text = remember { mutableStateOf(exerciseSet.reps.toString()) }
                        val isError = remember { mutableStateOf(false) }
                        TextFieldWithCustomPadding(
                            value = text.value,
                            onValueChange = {
                                text.value = it
                                val newValue = (it.ifEmpty { "0" }).toIntOrNull()
                                if (newValue == null) {
                                    isError.value = true
                                } else {
                                    isError.value = false
                                    onRepsChanged(newValue)
                                }
                            },
                            isError = isError.value,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            label = "reps",
                            readOnly = !editMode,
                            placeholder = "0"
                        )
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))

                if (exerciseTemplate.weight) {
                    val text = remember { mutableStateOf(exerciseSet.weight.toString()) }
                    val isError = remember { mutableStateOf(false) }
                    TextFieldWithCustomPadding(
                        value = text.value,
                        onValueChange = {
                            text.value = it
                            val newValue = (if (it.isEmpty()) "0" else it.replace(',', '.')).toDoubleOrNull()
                            if (newValue == null) {
                                isError.value = true
                            } else {
                                isError.value = false
                                onWeightChanged(newValue)
                            }
                        },
                        isError = isError.value,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = "weight",
                        readOnly = !editMode,
                        placeholder = "0.0",
                        suffix = "kg" + if (exerciseTemplate.weightTimesTwo) " × 2" else "",
                    )

                }
                if (exerciseTemplate.distance) {
                    TODO("allow editing distance")
                }
                if (exerciseTemplate.time) {
                    TODO("allow editing time")
                }

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFieldWithCustomPadding(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    label: String,
    readOnly: Boolean,
    placeholder: String,
    suffix: String? = null,
    padding: Dp = 10.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val colors = OutlinedTextFieldDefaults.colors()

    val textColor = colors.textColor(true, isError, true)

    // Merge with your typography but force the correct color
    val textStyle = LocalTextStyle.current
        .merge(MaterialTheme.typography.bodyMedium)
        .copy(color = textColor)

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        readOnly = readOnly,
        keyboardOptions = keyboardOptions,
        textStyle = textStyle,
        cursorBrush = SolidColor(colors.cursorColor(isError = isError)),
        interactionSource = interactionSource
    ) { innerTextField ->
        OutlinedTextFieldDefaults.DecorationBox(
            value = value,
            innerTextField = innerTextField,
            enabled = !readOnly,
            singleLine = true,
            isError = isError,
            visualTransformation = VisualTransformation.None,
            label = {
                Text(label)
            },
            placeholder = {
                Text(placeholder)
            },
            trailingIcon = null,
            leadingIcon = null,
            suffix = {
                suffix?.let {
                    Text(it)
                }
            },
            interactionSource = interactionSource,
            contentPadding = PaddingValues(
                padding
            ),
            colors = colors,
            container = {
                OutlinedTextFieldDefaults.Container(
                    enabled = !readOnly,
                    isError = isError,
                    interactionSource = interactionSource,
                    colors = colors,
                )
            }
        )
    }
}



