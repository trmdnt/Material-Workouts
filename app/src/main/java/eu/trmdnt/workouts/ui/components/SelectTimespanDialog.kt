package eu.trmdnt.workouts.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectTimespanDialog(initialValue: Int, onConfirmValue: (Int) -> Unit, onDismiss: () -> Unit) {
    DialogBase(onDismissRequest = onDismiss) {
        val seconds = remember { mutableStateOf((initialValue % 60).toString()) }
        val secondsError = remember { mutableStateOf(false) }
        val minutes = remember { mutableStateOf((initialValue / 60).toString()) }
        val minutesError = remember { mutableStateOf(false) }
        Row {
            OutlinedTextField(modifier = Modifier.weight(1f), value = minutes.value.toString(), label = {
                Text("minutes")
            }, onValueChange = {
                minutes.value = it
                try {
                    if (it.isEmpty()) {
                        return@OutlinedTextField
                    }
                    it.toInt()
                    minutesError.value = false
                } catch (_: NumberFormatException) {
                    minutesError.value = true
                }
            }, isError = minutesError.value, placeholder = {
                Text("0")
            })
            OutlinedTextField(modifier = Modifier.weight(1f), value = seconds.value.toString(), label = {
                Text("seconds")
            }, onValueChange = {
                seconds.value = it
                try {
                    if (it.isEmpty()) {
                        return@OutlinedTextField
                    }
                    it.toInt()
                    secondsError.value = false
                } catch (_: NumberFormatException) {
                    secondsError.value = true
                }
            }, isError = secondsError.value, placeholder = {
                Text("0")
            })
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(
                onClick = { onDismiss() },
//                        modifier = Modifier.padding(8.dp),
            ) {
                Text("Dismiss")
            }
            TextButton(
                onClick = {
                    try {
                        var value = seconds.value.let {
                            if (it.isEmpty()) {
                                0
                            } else {
                                it.toInt()
                            }
                        }
                        value = value + minutes.value.let {
                            if (it.isEmpty()) {
                                0
                            } else {
                                it.toInt()
                            }
                        } * 60
                        onConfirmValue(value)
                    } catch (_: NumberFormatException) {

                    }
                },
            ) {
                Text("Confirm")
            }
        }
    }
}