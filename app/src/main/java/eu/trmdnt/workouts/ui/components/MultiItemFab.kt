package eu.trmdnt.workouts.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class FabAction(
    val label: String,
    val icon: ImageVector? = null,
    val onClick: () -> Unit,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MultiItemFab(actions: List<FabAction>, expanded: Boolean, onButtonPressed: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(horizontalAlignment = Alignment.End) {
                actions.forEach { action ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .combinedClickable(onClick = {
                                action.onClick()
                            })
                    ) {
                        ExtendedFloatingActionButton(modifier = Modifier.padding(start = 4.dp), onClick = {
                            action.onClick()
                        }, icon = {
                            action.icon?.let {
                                Icon(it, contentDescription = action.label)
                            }
                        }, text = { Text(text = action.label) }, shape = RoundedCornerShape(CornerSize(50)))
                    }
                }
            }
        }

        val rotation by animateFloatAsState(targetValue = if (expanded) 45f else 0f)

        val shapePercent by animateIntAsState(
            targetValue = if (expanded) 50 else 25,
        )

        val containerColor by animateColorAsState(
            targetValue = if (expanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
        )
        val contentColor by animateColorAsState(
            targetValue = if (!expanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
        )

        FloatingActionButton(
            onClick = { onButtonPressed() },
            shape = RoundedCornerShape(CornerSize(shapePercent)),
            containerColor = containerColor,
            contentColor = contentColor
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (expanded) "Close" else "Open",
                modifier = Modifier.rotate(rotation),
            )
        }
    }
}