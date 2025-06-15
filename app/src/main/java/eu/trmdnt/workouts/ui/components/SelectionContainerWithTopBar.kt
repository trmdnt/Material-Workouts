package eu.trmdnt.workouts.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun <T> SelectionContainerWithTopBar(
    itemsList: List<T>,
    selectedItemsList: List<T>,
    onItemClick: (T) -> Unit,
    onLongItemClick: (T) -> Unit,
    onItemEditClicked: ((T) -> Unit)? = null,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    editMode: Boolean,
    textContent: @Composable (T, Modifier) -> Unit,
    alternativeTopBar: (@Composable () -> Unit)?,
    fab: (@Composable () -> Unit)?,
    getId: ((T) -> Long),
) {
    if (editMode) {
        BackHandler {
            onCancel()
        }
    }

    Scaffold(topBar = {
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
        ItemsList(
            itemsList = itemsList,
            selectedItemsList = selectedItemsList,
            onItemPress = onItemClick,
            onLongItemPress = onLongItemClick,
            paddingValues = contentPadding,
            editMode = editMode,
            textContent = textContent,
            getId = getId,
            onItemEditClicked = onItemEditClicked,
        )
    }
}

@Composable
private fun <T> ItemsList(
    itemsList: List<T>,
    selectedItemsList: List<T>,
    onItemPress: (T) -> Unit,
    onLongItemPress: (T) -> Unit,
    onItemEditClicked: ((T) -> Unit)?,
    paddingValues: PaddingValues,
    editMode: Boolean,
    textContent: @Composable (T, Modifier) -> Unit,
    getId: ((T) -> Long)
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = paddingValues
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
                onItemEditClicked = onItemEditClicked?.let {
                    { it(item) }
                }
            ) {
                textContent(item, it)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MyListItem(
    onItemPress: () -> Unit,
    onItemEditClicked: (() -> Unit)?,
    onLongItemPress: () -> Unit,
    editMode: Boolean,
    checked: Boolean,
    textContent: @Composable (Modifier) -> Unit,
) {
    Card(
        modifier = Modifier
            .combinedClickable(onClick = {
                onItemPress()
            }, onLongClick = {
                onLongItemPress()
            })
            .fillMaxWidth()
    ) {

        Row {
            textContent(Modifier.weight(1f))
            Column(
                modifier = Modifier.padding(8.dp),
            ) {
                if (editMode) {
                    Checkbox(
                        checked = checked, onCheckedChange = { _ ->
                            onItemPress()
                        })
                } else {
                    if (onItemEditClicked != null) {
                        OutlinedButton(
                            onClick = {
                                onItemEditClicked()
                            },

                            content = {
                                Icon(
                                    imageVector = Icons.Filled.Edit, contentDescription = "edit"
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

