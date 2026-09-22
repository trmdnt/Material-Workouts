package eu.trmdnt.workouts.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun <T> ListAvailableItems(
    items: List<T>,
    onItemClick: (T) -> Unit,
    getName: (T) -> String,
    modifier: Modifier = Modifier
) {
    //TODO add searchbox to filter elements quickly
    LazyColumn(modifier = modifier) {
        itemsIndexed(
            items = items
        ) { index, item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { onItemClick(item) }),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = getName(item),
                )
                if (index < items.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}