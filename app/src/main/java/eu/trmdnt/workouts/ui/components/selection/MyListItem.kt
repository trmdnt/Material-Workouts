package eu.trmdnt.workouts.ui.components.selection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.trmdnt.workouts.ui.components.MyCard

@Composable
fun MyListItem(
    onItemPress: () -> Unit,
    onLongItemPress: () -> Unit,
    editMode: Boolean,
    checked: Boolean,
    textContent: @Composable (Boolean) -> Unit,
) {
    MyCard(
        modifier = Modifier
            .combinedClickable(onClick = {
                onItemPress()
            }, onLongClick = {
                onLongItemPress()
            })
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(modifier = Modifier.weight(1f)) {
                textContent(checked)
            }

            Column {
                AnimatedVisibility(
                    editMode,
                    enter = expandHorizontally(),
                    exit = shrinkHorizontally()
                ) {
                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                        Checkbox(
                            checked = checked, onCheckedChange = { _ ->
                                onItemPress()
                            })
                    }
                }
            }
        }
    }
}