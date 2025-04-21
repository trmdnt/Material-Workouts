package com.example.gymutil.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun <T> ListAvailableItems(items: List<T>, onItemClick: (T) -> Unit, getName: (T) -> String) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 8.dp)) {
        items(
            items = items
        ) {
            Card(
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { onItemClick(it) })
            ) {
                Text(modifier = Modifier.padding(8.dp), text = getName(it), style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}