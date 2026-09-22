package eu.trmdnt.workouts.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ConfirmDeleteDialog(text: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    DialogBase(onDismiss) {
        Text(text)
        Row {
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors()
                    .copy(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete")
            }
        }
    }
}