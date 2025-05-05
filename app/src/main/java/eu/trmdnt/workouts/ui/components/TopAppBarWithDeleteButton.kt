package eu.trmdnt.workouts.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarWithDeleteButton(onClosePressed: () -> Unit, onDeletePressed: () -> Unit) {
    TopAppBar(
        title = {
            Text("Select items to delete")
        },
        navigationIcon = {
            IconButton(onClick = onClosePressed) {
                Icon(
                    imageVector = Icons.Filled.Close, contentDescription = "end selection"
                )
            }
        },
        actions = {
            IconButton(onClick = onDeletePressed) {
                Icon(
                    imageVector = Icons.Filled.Delete, contentDescription = "delete selected items"
                )
            }
        },
    )
}