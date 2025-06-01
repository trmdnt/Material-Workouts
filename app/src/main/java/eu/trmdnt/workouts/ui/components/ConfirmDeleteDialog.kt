package eu.trmdnt.workouts.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ConfirmDeleteDialog(text: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    DialogBase(onDismiss) {
        Text(text)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
            TextButton(onClick = onConfirm) {
                Text("Delete")
            }
        }
    }
}