package eu.trmdnt.workouts.ui.components.selection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun <T> ItemsListWithHeaders(
    itemsList: List<Pair<String, List<T>>>,
    selectedItemsList: List<T>,
    onItemPress: (T) -> Unit,
    onLongItemPress: (T) -> Unit,
    paddingValues: PaddingValues,
    editMode: Boolean,
    textContent: @Composable (T, Boolean) -> Unit,
    getId: ((T) -> Long)
) {
    LazyColumn(
        contentPadding = paddingValues,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(10.dp, 0.dp)
    ) {
        itemsList.forEach { section ->
            if (section.second.isEmpty()) {
                return@forEach
            }

            stickyHeader {
                Text(
                    section.first,
                    Modifier
                        .fillMaxWidth()
                        //.background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(10.dp),
                )
            }

            itemsIndexed(
                items = section.second, key = { _, it ->
                    getId(it)
                }) { index, item ->
                MyListItem(
                    onItemPress = {
                        onItemPress(item)
                    },
                    onLongItemPress = {
                        onLongItemPress(item)
                    },
                    editMode = editMode,
                    checked = selectedItemsList.contains(item),
                ) { checked ->
                    textContent(item, checked)
                }
            }
        }
    }
}