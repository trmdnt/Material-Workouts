package eu.trmdnt.workouts.ui.statistics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import eu.trmdnt.workouts.ui.components.TableCell
import eu.trmdnt.workouts.ui.components.TableHeader
import eu.trmdnt.workouts.ui.components.TopAppBarWithBackButton
import eu.trmdnt.workouts.ui.components.WeightRepHistoryPlot
import java.text.SimpleDateFormat

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ViewWeightPerRep(exerciseTemplateId: Long, onBackPressed: () -> Unit) {
    val viewModel: ViewExerciseStatisticViewModel =
        hiltViewModel<ViewExerciseStatisticViewModel, ViewExerciseStatisticViewModel.ViewWeightPerRepViewModelFactory> { factory ->
            factory.create(exerciseTemplateId)
        }

    val uiState by viewModel.uiState.collectAsState()

    val title: String = uiState.exerciseTemplate?.name.toString()

    Scaffold(topBar = {
        TopAppBarWithBackButton(title, onBackPressed)
    }) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            Row(modifier = Modifier.weight(3f)) {
                WeightRepHistoryPlot(uiState.data, uiState.selectedPoint) {
                    viewModel.onPlotPointSelected(it)
                }
            }

            Column(
                modifier = Modifier
                    .weight(2f)
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        viewModel.onPreviousPressed()
                    }) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = "previous point")
                    }
                    val formatter = SimpleDateFormat("yyyy-MM-dd")
                    if (uiState.selectedPoint == null) {
                        Text("Select a point to view its data", Modifier.weight(1f))
                    } else {
                        uiState.selectedPoint?.let {
                            val dateString = formatter.format(uiState.data[it].dateTimeStamp * 1000)

                            Text(
                                "$dateString",
                                Modifier.weight(1f),
                                style = MaterialTheme.typography.titleLargeEmphasized
                            )
                        }
                    }
                    IconButton(onClick = {
                        viewModel.onNextPressed()
                    }) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = "next point")
                    }
                }

                uiState.selectedPoint?.let {
                    val weightPerRep = uiState.data[it].weightPerRep
                    val totalReps = uiState.data[it].totalReps
                    Text(
                        "Weight/Rep: $weightPerRep, total Reps: $totalReps",
                        style = MaterialTheme.typography.bodyLargeEmphasized
                    )
                }

                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    uiState.setsOnSelectedDate?.let {
                        Row {
                            TableHeader("weight", 1f)
                            TableHeader("reps", 1f)
                        }
                        it.forEach {
                            Row {
                                TableCell("${it.weight}", 1f)
                                TableCell("${it.reps}", 1f)
                            }
                        }


                    }
                }
            }
        }
    }
}