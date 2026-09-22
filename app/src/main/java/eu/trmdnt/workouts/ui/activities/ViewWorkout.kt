package eu.trmdnt.workouts.ui.activities

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import eu.trmdnt.workouts.database.entities.ExerciseSet
import eu.trmdnt.workouts.database.entities.ExerciseTemplate
import eu.trmdnt.workouts.database.entities.ExerciseWithSets
import eu.trmdnt.workouts.database.entities.SetType
import eu.trmdnt.workouts.ui.components.*
import eu.trmdnt.workouts.ui.components.set.DistanceField
import eu.trmdnt.workouts.ui.components.set.RepsField
import eu.trmdnt.workouts.ui.components.set.TimeField
import eu.trmdnt.workouts.ui.components.set.WeightField
import eu.trmdnt.workouts.ui.navigation.NavEvent
import eu.trmdnt.workouts.ui.navigation.NavEventHandler

@Composable
fun ViewWorkout(
    workoutId: Long, editing: Boolean, navEventHandler: NavEventHandler
) {
    val viewModel = hiltViewModel<ViewWorkoutViewModel, ViewWorkoutViewModel.ViewWorkoutViewModelFactory> {
        it.create(workoutId, editing)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.navigationEvents.collect { event ->
                navEventHandler.handle(event)
            }
        }
    }

    uiState.editDialogOpened?.let { (exerciseId, setId) ->
        val exercise = uiState.exercises.find {
            it.exercise.exerciseId == exerciseId
        }

        val exerciseSet = exercise?.let { exercise ->
            exercise.exerciseSets.find {
                it.id == setId
            }
        }

        exerciseSet?.let {
            EditSetDialog(onDismiss = {
                val event = ViewWorkoutEvent.EditSetDialogDismissed
                viewModel.handleEvent(event)
            }, exerciseSet = exerciseSet, exerciseTemplate = exercise.exerciseTemplate, onWeightChanged = { value ->
                val event = ViewWorkoutEvent.Set.WeightChanged(setId, exerciseId, value)
                viewModel.handleEvent(event)
            }, onRepsChanged = { value ->
                val event = ViewWorkoutEvent.Set.RepsChanged(setId, exerciseId, value)
                viewModel.handleEvent(event)
            }, onSetTypeChanged = { type ->
                val event = ViewWorkoutEvent.Set.SetTypeChanged(setId, exerciseId, type)
                viewModel.handleEvent(event)
            }, onDelete = {
                val event = ViewWorkoutEvent.Set.Deleted(setId)
                viewModel.handleEvent(event)
            }, onDistanceChanged = { value ->
                val event = ViewWorkoutEvent.Set.DistanceChanged(setId, exerciseId, value)
                viewModel.handleEvent(event)
            }, onTimeChanged = { value ->
                val event = ViewWorkoutEvent.Set.TimeChanged(setId, exerciseId, value)
                viewModel.handleEvent(event)
            })
        }
    }

    Scaffold(containerColor = Color.Transparent, snackbarHost = {
        SnackbarHost(hostState = snackbarHostState)
    }, topBar = {
        TopAppBarWithBackButton(title = uiState.topBarTitle, {
            navEventHandler.handle(NavEvent.Back)
        }, onAlternativeAction = {
            val event = ViewWorkoutEvent.EditButtonPressed
            viewModel.handleEvent(event)
        }, alternativeIcon = {
            if (!uiState.editMode) {
                Icon(Icons.Filled.Edit, contentDescription = "enter edit mode")
            } else {
                Icon(Icons.Filled.EditOff, contentDescription = "exit edit mode")
            }
        })
    }, floatingActionButton = {
        val lastExerciseEmpty = uiState.exercises.lastOrNull()?.exerciseSets?.isEmpty() ?: false

        if (uiState.editMode && (!lastExerciseEmpty || !uiState.scrollEnd)) {
            FloatingActionButton(onClick = {
                val event = ViewWorkoutEvent.NewExercisePressed
                viewModel.handleEvent(event)
            }, content = {
                Icon(Icons.Filled.Add, contentDescription = "Add Exercise")
            })
        }
    }, content = { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues)
        ) {
            uiState.workoutName?.let { value ->
                val text = remember { mutableStateOf(value) }
                OutlinedTextField(
                    value = text.value, label = {
                        Text("workout name")
                    }, onValueChange = {
                        val event = ViewWorkoutEvent.WorkoutRenamed(it)
                        viewModel.handleEvent(event)
                        text.value = it
                    }, modifier = Modifier.fillMaxWidth().padding(4.dp), readOnly = !uiState.editMode
                )
            }


            //TODO fix callback hell
            Row(modifier = Modifier.weight(1f)) {
                ExerciseList(
                    uiState.exercises,
                    onSetAdded = {
                        val event = ViewWorkoutEvent.Set.Added(it)
                        viewModel.handleEvent(event)
                    },
                    editMode = uiState.editMode,
                    onWeightChanged = { setId, exerciseId, value ->
                        val event = ViewWorkoutEvent.Set.WeightChanged(setId, exerciseId, value)
                        viewModel.handleEvent(event)
                    },
                    onRepsChanged = { setId, exerciseId, value ->
                        val event = ViewWorkoutEvent.Set.RepsChanged(setId, exerciseId, value)
                        viewModel.handleEvent(event)
                    },
                    onDistanceChanged = { setId, exerciseId, value ->
                        val event = ViewWorkoutEvent.Set.DistanceChanged(setId, exerciseId, value)
                        viewModel.handleEvent(event)
                    },
                    onTimeChanged = { setId, exerciseId, value ->
                        val event = ViewWorkoutEvent.Set.TimeChanged(setId, exerciseId, value)
                        viewModel.handleEvent(event)
                    },
                    onDeleteExercisePressed = {
                        val event = ViewWorkoutEvent.Exercise.Deleted(it)
                        viewModel.handleEvent(event)
                    },
                    onDeleteSetPressed = {
                        val event = ViewWorkoutEvent.Set.Deleted(it)
                        viewModel.handleEvent(event)
                    },
                    onEditAction = { setId, exerciseId ->
                        val event = ViewWorkoutEvent.Set.EditAction(setId, exerciseId)
                        viewModel.handleEvent(event)
                    },
                    onInfoPressed = {
                        val event = ViewWorkoutEvent.Exercise.InfoPressed(it)
                        viewModel.handleEvent(event)
                    },
                    onEditPressed = {
                        val event = ViewWorkoutEvent.Exercise.EditPressed(it)
                        viewModel.handleEvent(event)
                    },
                    scrollEnd = {
                        val event = ViewWorkoutEvent.ReachedBottom(it)
                        viewModel.handleEvent(event)
                    },
                    isScrollAtEnd = uiState.scrollEnd,
                )
            }

            AnimatedVisibility(visible = uiState.editMode) {
                if (uiState.recommendedExercises.isNotEmpty()) {
                    Column(modifier = Modifier.padding(0.dp)) {
                        FlowRow {
                            for (exerciseTemplate in uiState.recommendedExercises) {
                                TextButton(onClick = {
                                    val event = ViewWorkoutEvent.Exercise.Added(exerciseTemplate)
                                    viewModel.handleEvent(event)
                                }) {
                                    Text(text = exerciseTemplate.name)
                                }
                            }
                        }
                    }
                }
            }


            if (uiState.displaySelectExerciseDialog) {
                val dismiss = {
                    val event = ViewWorkoutEvent.SelectExerciseDismissed
                    viewModel.handleEvent(event)
                }

                val updateSearch: (String) -> Unit = {
                    val event = ViewWorkoutEvent.SearchChanged(it)
                    viewModel.handleEvent(event)
                }

                val selectExercise: (ExerciseTemplate) -> Unit = {
                    val event = ViewWorkoutEvent.Exercise.Added(it)
                    viewModel.handleEvent(event)
                }

                val query = uiState.exerciseSearchQuery
                val availableExercises = uiState.availableExerciseTemplates

                SelectExerciseDialog(dismiss, query, updateSearch, selectExercise, availableExercises)
            }


        }
    })

    LaunchedEffect(uiState.undoSnackBarMessage) {
        uiState.undoSnackBarMessage?.let {
            try {
                val result = snackbarHostState.showSnackbar(
                    message = uiState.undoSnackBarMessage.toString(),
                    actionLabel = "Undo",
                    duration = SnackbarDuration.Short
                )
                when (result) {
                    SnackbarResult.ActionPerformed -> {
                        val event = ViewWorkoutEvent.UndoPressed
                        viewModel.handleEvent(event)
                    }

                    SnackbarResult.Dismissed -> {/* Handle snackbar dismissed */
                    }
                }
            } finally {
                val event = ViewWorkoutEvent.SnackBarDismissed
                viewModel.handleEvent(event)
            }
        }
    }
}

@Composable
private fun SelectExerciseDialog(
    dismiss: () -> Unit,
    query: String,
    updateSearch: (String) -> Unit,
    selectExercise: (ExerciseTemplate) -> Unit,
    availableExercises: List<ExerciseTemplate>
) {
    DialogBase(
        onDismissRequest = dismiss
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            val focusRequester = remember { FocusRequester() }
            OutlinedTextField(
                value = query,
                onValueChange = updateSearch,
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                label = {
                    Text("Filter")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        selectExercise(availableExercises.first())
                    })
            )

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }

            ListAvailableItems(items = availableExercises, onItemClick = {
                selectExercise(it)
            }, getName = {
                it.name
            }, modifier = Modifier.weight(1f))
            Row {
                Spacer(Modifier.weight(1f))
                TextButton(onClick = dismiss) {
                    Text("Cancel")
                }
            }
        }

    }
}

@Composable
fun ExerciseList(
    items: List<ExerciseWithSets>,
    onSetAdded: (Long) -> Unit,
    onWeightChanged: (Long, Long, Double) -> Unit,
    onRepsChanged: (Long, Long, Int) -> Unit,
    onDistanceChanged: (Long, Long, Double) -> Unit,
    onTimeChanged: (Long, Long, Int) -> Unit,
    editMode: Boolean,
    onDeleteExercisePressed: (Long) -> Unit,
    onDeleteSetPressed: (Long) -> Unit,
    onEditAction: (Long, Long) -> Unit,
    onInfoPressed: (Long) -> Unit,
    onEditPressed: (Long) -> Unit,
    scrollEnd: (Boolean) -> Unit,
    isScrollAtEnd: Boolean
) {
    if (items.isEmpty()) {
        if (editMode) {
            Text("add an exercise to get started")
        } else {
            Text("this workout is empty")
        }

    } else {
        val listState = rememberLazyListState()
        var newItemIndex by remember { mutableStateOf<Int?>(null) }

        val focusManager = LocalFocusManager.current

        scrollEnd(!listState.canScrollForward)

        LaunchedEffect(newItemIndex) {
            newItemIndex?.let {
                listState.animateScrollToItem(index = it)
                newItemIndex = null
            }
        }

        LaunchedEffect(isScrollAtEnd) {
            listState.scrollToItem(items.size - 1)
        }

        LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            itemsIndexed(
                items = items, key = { _, item -> item.exercise.exerciseId }) { index, exerciseWithSets ->
                SwipeToDeleteContainer(onDelete = {
                    onDeleteExercisePressed(exerciseWithSets.exercise.exerciseId)
                }, enabled = editMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)
                    ) {
                        Column(modifier = Modifier.padding(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = exerciseWithSets.exerciseTemplate.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 0.dp).weight(1f)
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
                                        Icon(
                                            Icons.Default.Edit, contentDescription = "edit exercise"
                                        )

                                    }
                                }

                                AnimatedVisibility(visible = editMode) {
                                    IconButton(onClick = {
                                        newItemIndex = index
                                        onSetAdded(exerciseWithSets.exercise.exerciseId)
                                        focusManager.clearFocus()
                                    }) {
                                        Icon(
                                            Icons.Default.Add, contentDescription = "add a new set"
                                        )
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
                                                onWeightChanged(
                                                    set.id, exerciseWithSets.exercise.exerciseId, it
                                                )
                                            },
                                            onRepsChanged = {
                                                onRepsChanged(
                                                    set.id, exerciseWithSets.exercise.exerciseId, it
                                                )
                                            },
                                            onDistanceChanged = {
                                                onDistanceChanged(
                                                    set.id, exerciseWithSets.exercise.exerciseId, it
                                                )
                                            },
                                            onTimeChanged = {
                                                onTimeChanged(
                                                    set.id, exerciseWithSets.exercise.exerciseId, it
                                                )
                                            },
                                            onDeleteSetPressed = {
                                                onDeleteSetPressed(set.id)
                                            },
                                            editMode = editMode,
                                            onEditAction = {
                                                onEditAction(
                                                    set.id, exerciseWithSets.exercise.exerciseId
                                                )
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
    onTimeChanged: (Int) -> Unit,
    onDeleteSetPressed: () -> Unit,
    onEditAction: () -> Unit,
    editMode: Boolean
) {
    key(exerciseSet.id) {
        SwipeToDeleteContainer(
            onDelete = onDeleteSetPressed, onEditAction = onEditAction, enabled = editMode
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.background(MaterialTheme.colorScheme.background)
                    .alpha(if (exerciseSet.setType != SetType.Default) 0.6f else 1f)
            ) {
                val trackedComponents: List<@Composable () -> Unit> = buildList {
                    if (exerciseTemplate.reps) {
                        add({ RepsField(exerciseSet, onRepsChanged, Modifier.weight(1f), editMode) })
                    }

                    if (exerciseTemplate.weight) {
                        add({
                            WeightField(exerciseSet, onWeightChanged, exerciseTemplate, Modifier.weight(1f), editMode)
                        })
                    }

                    if (exerciseTemplate.distance) {
                        add({
                            DistanceField(exerciseSet, onDistanceChanged, Modifier.weight(1f), editMode)
                        })
                    }

                    if (exerciseTemplate.time) {
                        add({
                            TimeField(exerciseSet, onTimeChanged, Modifier.weight(1f), editMode)
                        })
                    }
                }

                trackedComponents.forEachIndexed { index, component ->
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (index == 0) {
                            AnimatedVisibility(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                visible = exerciseSet.setType == SetType.WarmUp,
                                enter = expandHorizontally(),
                                exit = ExitTransition.None
                            ) {
                                Icon(
                                    Icons.Default.ArrowDropUp, "this item will not be showed in statistics"
                                )
                            }
                            AnimatedVisibility(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                visible = exerciseSet.setType == SetType.Drop,
                                enter = expandHorizontally(),
                                exit = ExitTransition.None
                            ) {
                                Icon(
                                    Icons.Default.ArrowDropDown, "this item will not be showed in statistics"
                                )
                            }
                        }
                        component()
                    }
                }
            }
        }
    }
}

