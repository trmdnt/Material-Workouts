package eu.trmdnt.workouts.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import eu.trmdnt.workouts.ui.components.selection.ItemsList


@Composable
private fun SelectionContainerWithTopBar(
    itemsList: @Composable (PaddingValues) -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    editMode: Boolean,
    alternativeTopBar: (@Composable () -> Unit)?,
    fab: (@Composable () -> Unit)?,
) {
    if (editMode) {
        BackHandler {
            onCancel()
        }
    }

    Scaffold(containerColor = Color.Transparent, topBar = {
        if (alternativeTopBar == null) {
            AnimatedVisibility(
                visible = editMode, enter = expandVertically(
                    // Expand from the top.
                    expandFrom = Alignment.Top
                ), exit = shrinkVertically()
            ) {
                TopAppBarWithDeleteButton(onClosePressed = {
                    onCancel()
                }, onDeletePressed = {
                    onDelete()
                })
            }
        } else {
            if (editMode) {
                TopAppBarWithDeleteButton(onClosePressed = {
                    onCancel()
                }, onDeletePressed = {
                    onDelete()
                })
            } else {
                alternativeTopBar()
            }
        }
    }, floatingActionButton = {
        if (fab != null && !editMode) {
            fab()
        }

    }) { contentPadding ->
        // Screen content
        itemsList(contentPadding)
    }
}

@Composable
fun <T> SelectionContainerWithTopBar(
    itemsList: List<T>,
    selectedItemsList: List<T>,
    onItemClick: (T) -> Unit,
    onLongItemClick: (T) -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    editMode: Boolean,
    textContent: @Composable (T, Boolean) -> Unit,
    alternativeTopBar: (@Composable () -> Unit)?,
    fab: (@Composable () -> Unit)?,
    getId: ((T) -> Long),
) {
    // Screen content
    val itemsListComposable: @Composable (PaddingValues) -> Unit = { contentPadding ->
        ItemsList(
            itemsList = itemsList,
            selectedItemsList = selectedItemsList,
            onItemPress = onItemClick,
            onLongItemPress = onLongItemClick,
            paddingValues = contentPadding,
            editMode = editMode,
            textContent = textContent,
            getId = getId,
        )
    }

    SelectionContainerWithTopBar(
        itemsList = itemsListComposable,
        onCancel = onCancel,
        onDelete = onDelete,
        editMode = editMode,
        alternativeTopBar = alternativeTopBar,
        fab = fab
    )
}

