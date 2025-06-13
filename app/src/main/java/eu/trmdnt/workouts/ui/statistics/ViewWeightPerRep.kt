package eu.trmdnt.workouts.ui.statistics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import eu.trmdnt.workouts.ui.components.WeightRepHistoryPlot
import eu.trmdnt.workouts.ui.components.TopAppBarWithBackButton

@Composable
fun ViewWeightPerRep(exerciseTemplateId: Long, onBackPressed: () -> Unit) {
    val viewModel: ViewWeightPerRepViewModel =
        hiltViewModel<ViewWeightPerRepViewModel, ViewWeightPerRepViewModel.ViewWeightPerRepViewModelFactory> { factory ->
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
            WeightRepHistoryPlot(uiState.data)
        }
    }

}