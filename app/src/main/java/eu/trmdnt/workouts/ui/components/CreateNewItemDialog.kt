package eu.trmdnt.workouts.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction

@Composable
fun CreateNewItemDialog(text: String, onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    DialogBase(onDismissRequest = onDismiss) {
        Text(text)
        var input by rememberSaveable { mutableStateOf("") }
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    onCreate(input)
                }
            ),
            modifier = Modifier
                .fillMaxWidth(),

            )
        Row {
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
            TextButton(onClick = {
                onCreate(input)
            }) {
                Text("Create")
            }
        }
    }
}