package eu.trmdnt.workouts.ui.components.selection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun <T> ItemsList(
    itemsList: List<T>,
    selectedItemsList: List<T>,
    onItemPress: (T) -> Unit,
    onLongItemPress: (T) -> Unit,
    paddingValues: PaddingValues,
    editMode: Boolean,
    textContent: @Composable (T, Boolean) -> Unit,
    getId: (T) -> Long
) {
    LazyColumn(
        contentPadding = paddingValues,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(10.dp)
    ) {

        items(
            items = itemsList, key = {
                getId(it)
            }) { item ->
            MyListItem(
                onItemPress = {
                    onItemPress(item)
                },
                onLongItemPress = {
                    onLongItemPress(item)
                },
                editMode = editMode,
                checked = selectedItemsList.contains(item),
            ) { bool ->
                textContent(item, bool)
            }
        }
    }
}