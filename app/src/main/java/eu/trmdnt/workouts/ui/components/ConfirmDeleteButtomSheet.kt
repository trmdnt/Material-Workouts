package eu.trmdnt.workouts.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmDeleteBottomSheet(text: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        // Sheet content
        Text(text = text)
        Button(onClick = onDismiss) {
            Text("Cancel")
        }
        Button(onClick = onConfirm) {
            Text("Delete")
        }
    }
}