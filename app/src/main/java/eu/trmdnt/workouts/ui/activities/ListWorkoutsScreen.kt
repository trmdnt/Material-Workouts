package eu.trmdnt.workouts.ui.activities

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import eu.trmdnt.workouts.R
import eu.trmdnt.workouts.database.entities.statistics.RecentStatistics
import eu.trmdnt.workouts.database.entities.statistics.WorkoutWithDuration
import eu.trmdnt.workouts.ui.components.*
import eu.trmdnt.workouts.ui.components.selection.ItemsListWithHeaders
import eu.trmdnt.workouts.ui.navigation.NavEventHandler
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


@Composable
fun ListWorkoutsScreen(navEventHandler: NavEventHandler) {
    val viewModel: ListWorkoutsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()


    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.navigationEvents.collect { event ->
                navEventHandler.handle(event)
            }
        }
    }

    val handleEvent = { event: ListWorkoutsEvent ->
        viewModel.handleEvent(event)
    }

    ListWorkoutsScreen(uiState, handleEvent)
}

@Composable
private fun ListWorkoutsScreen(
    uiState: ListWorkoutsViewModel.UiState,
    handleEvent: (ListWorkoutsEvent) -> Unit
) {
    val items = uiState.workouts.toMutableList()

    val itemsToday = items.filter {
        LocalDate.ofInstant(Instant.ofEpochSecond(it.workout.dateStarted), ZoneId.systemDefault())
            .isEqual(
                LocalDate.now()
            )
    }
    val now = Instant.now()
    items.removeAll(itemsToday)
    val items7days = items.filter {
        val sevenDaysAgo = now.minus(7, ChronoUnit.DAYS)

        val instant = Instant.ofEpochSecond(it.workout.dateStarted)
        instant.isAfter(sevenDaysAgo)
    }
    items.removeAll(items7days)
    val items30days = items.filter {
        val thirtyDaysAgo = now.minus(30, ChronoUnit.DAYS)

        val instant = Instant.ofEpochSecond(it.workout.dateStarted)
        instant.isAfter(thirtyDaysAgo)
    }
    items.removeAll(items30days)

    val items1year = items.filter {
        val oneYearAgo = now.minus(365, ChronoUnit.DAYS)

        val instant = Instant.ofEpochSecond(it.workout.dateStarted)
        instant.isAfter(oneYearAgo)
    }
    items.removeAll(items1year)


    val sections: List<Pair<String, List<WorkoutWithDuration>>> = listOf(
        Pair(stringResource(R.string.today), itemsToday),
        Pair(stringResource(R.string.last_week), items7days),
        Pair(stringResource(R.string.last_month), items30days),
        Pair(stringResource(R.string.last_year), items1year),
        Pair(stringResource(R.string.a_long_time_ago), items)
    )

    if (uiState.editMode) {
        BackHandler {
            handleEvent(ListWorkoutsEvent.ClearSelection)
        }
    }

    Scaffold(containerColor = Color.Transparent, topBar = {
        AnimatedContent(targetState = uiState.editMode) { targetState ->
            if (targetState) {
                TopAppBarWithDeleteButton(onClosePressed = {
                    handleEvent(ListWorkoutsEvent.ClearSelection)
                }, onDeletePressed = {
                    handleEvent(ListWorkoutsEvent.DeleteSelected)
                })
            } else {
                RecentStatisticsComponent(recentStatistics = uiState.recentStatistics)
            }
        }
    }, floatingActionButton = {
        if (!uiState.editMode) {
            MultiItemFab(expanded = uiState.newWorkoutSelectionFabOpen, onButtonPressed = {
                handleEvent(ListWorkoutsEvent.OnFabExtended)
            }, actions = uiState.workoutTemplates.map {
                FabAction(
                    it.name,
                    onClick = {
                        handleEvent(ListWorkoutsEvent.OnWorkoutTemplateSelected(it))
                    },
                )
            })
        }
    }) { contentPadding ->
        WorkoutListWithSelection(sections, uiState, contentPadding, handleEvent)
    }

    if (uiState.confirmDialogShown) {
        ConfirmDeleteDialog(text = stringResource(
            R.string.do_you_really_want_to_delete_the_selected_item_s,
            uiState.selectedWorkouts.size
        ), onDismiss = {
            handleEvent(ListWorkoutsEvent.CancelDeletion)
        }, onConfirm = {
            handleEvent(ListWorkoutsEvent.ConfirmDeletion)
        })
    }
}

@Composable
private fun WorkoutListWithSelection(
    sections: List<Pair<String, List<WorkoutWithDuration>>>,
    uiState: ListWorkoutsViewModel.UiState,
    contentPadding: PaddingValues,
    handleEvent: (ListWorkoutsEvent) -> Unit
) {
    ItemsListWithHeaders(
        itemsList = sections,
        selectedItemsList = uiState.selectedWorkouts.toList(),
        onItemPress = {
            handleEvent(ListWorkoutsEvent.OnItemPressed(it))
        },
        onLongItemPress = {
            handleEvent(ListWorkoutsEvent.OnItemLongPressed(it))
        },
        paddingValues = contentPadding,
        editMode = uiState.editMode,
        textContent = { item, selected ->
            // display workout name

            Row {

                Row(modifier = Modifier.weight(1f)) {
                    Text(text = "${item.workout.name} ", fontWeight = FontWeight.Bold)
                    val workoutTemplate =
                        uiState.workoutTemplates.find({ it.workoutTemplateId == item.workout.workoutTemplateId })

                    // display workout template name if it is not the same as the workout name
                    workoutTemplate?.name?.let {
                        if (it != item.workout.name) {
                            Text("($it) ")
                        }
                    }

                    val instant = Instant.ofEpochSecond(item.workout.dateStarted)

                    // Convert the Instant to a LocalDateTime
                    val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())

                    // Define a formatter
                    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

                    Text(stringResource(R.string.on, dateTime.format(formatter)))
                }

                AnimatedVisibility(
                    !uiState.editMode,
                    enter = expandHorizontally(),
                    exit = shrinkHorizontally()
                ) {
                    Text(convertSecondsToTime(item.timeSpentSeconds))
                }
            }

        },
        getId = {
            it.workout.id
        },
    )
}


@Composable
private fun RecentStatisticsComponent(recentStatistics: RecentStatistics) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val options = listOf(stringResource(R.string.week), stringResource(R.string.month),
        stringResource(R.string.year), stringResource(R.string.total)
    )

    Column(
        modifier = Modifier
            .padding(10.dp, 0.dp)
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MyCard(
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.time_spent))
                val time = when (selectedIndex) {
                    0 -> recentStatistics.timeWorkingOutWeek
                    1 -> recentStatistics.timeWorkingOutMonth
                    2 -> recentStatistics.timeWorkingOutYear
                    3 -> recentStatistics.timeWorkingOutTotal
                    else -> 0
                }
                Text(convertSecondsToTime(time))

            }

            MyCard(
                modifier = Modifier.weight(1f),

                ) {
                Text(stringResource(R.string.workout_count))
                val count = when (selectedIndex) {
                    0 -> recentStatistics.workoutCountWeek
                    1 -> recentStatistics.workoutCountMonth
                    2 -> recentStatistics.workoutCountYear
                    3 -> recentStatistics.workoutCountTotal
                    else -> 0
                }
                Text(count.toString())
            }
        }
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, label ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index, count = options.size
                    ),
                    onClick = { selectedIndex = index },
                    selected = index == selectedIndex,
                    label = { Text(label) })
            }
        }
    }

}

fun convertSecondsToTime(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return if (hours == 0L) String.format("%02dm", minutes) else
        String.format("%02dh %02dm", hours, minutes)
}