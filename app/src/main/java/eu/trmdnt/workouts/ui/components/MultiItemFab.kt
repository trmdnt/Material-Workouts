package eu.trmdnt.workouts.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class FabAction(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

@Composable
fun MultiItemFab(actions: List<FabAction>) {
    var expanded by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Bottom) {
        AnimatedVisibility(visible = expanded) {
            Column(horizontalAlignment = Alignment.End) {
                actions.forEach { action ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .combinedClickable(onClick = {
                                expanded = false
                                action.onClick()
                            })
                    ) {
                        ExtendedFloatingActionButton(modifier = Modifier.padding(start = 4.dp), onClick = {
                            expanded = false
                            action.onClick()
                        }, icon = {
                            Icon(action.icon, contentDescription = action.label)
                        }, text = { Text(text = action.label) })
                    }
                }
            }
        }

        val rotation by animateFloatAsState(targetValue = if (expanded) 45f else 0f)
        FloatingActionButton(onClick = { expanded = !expanded }) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (expanded) "Close" else "Open",
                modifier = Modifier.rotate(rotation)
            )
        }
    }
}