package eu.trmdnt.workouts.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import eu.trmdnt.workouts.R
import kotlinx.coroutines.launch

@Composable
fun SwipeToDeleteContainer(
    modifier: Modifier = Modifier,
    onDelete: () -> Unit,
    onEditAction: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { it * .3f },
    )

    LaunchedEffect(dismissState.settledValue) {
        when (dismissState.settledValue) {
            SwipeToDismissBoxValue.StartToEnd -> {
                if (onEditAction != null) {
                    coroutineScope.launch {
                        // otherwise it may be triggered again
                        dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                    }
                    onEditAction()
                }
            }

            SwipeToDismissBoxValue.EndToStart -> {
                coroutineScope.launch {
                    // otherwise it may be triggered again
                    dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                }
                onDelete()
            }

            SwipeToDismissBoxValue.Settled -> {}
        }
    }
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            if (enabled) DismissBackground(
                dismissState = dismissState, editAction = (onEditAction != null)
            )
        },
        content = {
            content()
        },
        modifier = modifier,
        enableDismissFromEndToStart = enabled,
        enableDismissFromStartToEnd = enabled && onEditAction != null,
    )
}


@Composable
fun DismissBackground(dismissState: SwipeToDismissBoxState, editAction: Boolean) {
    val color = when (dismissState.dismissDirection) {
        SwipeToDismissBoxValue.StartToEnd -> {
            if (editAction) {
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
        if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
            Icon(
                Icons.Default.Edit, contentDescription = stringResource(R.string.edit)
            )
        }
        Spacer(modifier = Modifier)
        if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
            Icon(
                Icons.Default.Delete, contentDescription = stringResource(R.string.delete)
            )
        }
    }
}