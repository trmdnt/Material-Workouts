package eu.trmdnt.workouts.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DialogBase(onDismissRequest: () -> Unit, content: @Composable () -> Unit) {
    BasicAlertDialog(onDismissRequest = onDismissRequest) {
        Card {
            Column(Modifier.padding(bottom = 8.dp, top = 16.dp, end = 16.dp, start = 16.dp)) {
                content()
            }
        }
    }
}