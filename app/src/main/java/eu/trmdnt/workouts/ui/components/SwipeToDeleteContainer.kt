package eu.trmdnt.workouts.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SwipeToDeleteContainer(
    modifier: Modifier = Modifier,
    onDelete: () -> Unit,
    onIgnoreAction: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(confirmValueChange = {
        when (it) {
            SwipeToDismissBoxValue.StartToEnd -> {
                if (onIgnoreAction != null) {
                    onIgnoreAction()
                    return@rememberSwipeToDismissBoxState false
                } else {
                    onDelete()
                }

            }

            SwipeToDismissBoxValue.EndToStart -> {
                onDelete()
            }

            SwipeToDismissBoxValue.Settled -> return@rememberSwipeToDismissBoxState false
        }
        return@rememberSwipeToDismissBoxState true
    }, positionalThreshold = { it * .3f })
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            if (enabled) DismissBackground(
                dismissState = dismissState,
                ignoreAction = (onIgnoreAction != null)
            )
        },
        content = {
            content()
        },
        modifier = modifier,
        enableDismissFromEndToStart = enabled,
        enableDismissFromStartToEnd = enabled && onIgnoreAction != null,
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DismissBackground(dismissState: SwipeToDismissBoxState, ignoreAction: Boolean) {
    val color = when (dismissState.dismissDirection) {
        SwipeToDismissBoxValue.StartToEnd -> {
            if (ignoreAction) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                Color.Transparent
            }

        }

        SwipeToDismissBoxValue.EndToStart -> {
            MaterialTheme.colorScheme.onError
        }

        SwipeToDismissBoxValue.Settled -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(12.dp, 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            Icons.Default.VisibilityOff, contentDescription = "delete"
        )
        Spacer(modifier = Modifier)
        Icon(
            Icons.Default.Delete, contentDescription = "delete"
        )
    }
}