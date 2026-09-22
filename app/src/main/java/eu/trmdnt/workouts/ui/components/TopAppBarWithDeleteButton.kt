package eu.trmdnt.workouts.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable

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