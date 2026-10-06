package eu.trmdnt.workouts.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import eu.trmdnt.workouts.R

@Composable
fun TopAppBarWithDeleteButton(onClosePressed: () -> Unit, onDeletePressed: () -> Unit) {
    TopAppBar(
        title = {
            Text(stringResource(R.string.select_items_to_delete))
        },
        navigationIcon = {
            IconButton(onClick = onClosePressed) {
                Icon(
                    imageVector = Icons.Filled.Close, contentDescription = stringResource(R.string.end_selection)
                )
            }
        },
        actions = {
            IconButton(onClick = onDeletePressed) {
                Icon(
                    imageVector = Icons.Filled.Delete, contentDescription = stringResource(R.string.delete_selected_items)
                )
            }
        },
    )
}