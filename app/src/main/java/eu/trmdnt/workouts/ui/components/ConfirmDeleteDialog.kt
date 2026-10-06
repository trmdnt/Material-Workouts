package eu.trmdnt.workouts.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import eu.trmdnt.workouts.R

@Composable
fun ConfirmDeleteDialog(text: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    DialogBase(onDismiss) {
        Text(text)
        Row {
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors()
                    .copy(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.delete))
            }
        }
    }
}