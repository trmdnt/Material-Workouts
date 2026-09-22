package eu.trmdnt.workouts.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

sealed class SelectionEvent<out T> {
    data class ToggleSelection<T>(val item: T) : SelectionEvent<T>()
    object ClearSelection : SelectionEvent<Nothing>()
    object DeleteSelected : SelectionEvent<Nothing>()
    object ConfirmDeletion : SelectionEvent<Nothing>()
    object DismissDeletionDialog : SelectionEvent<Nothing>()
}


class SelectionManager<T>(val deleteItems: (Set<T>) -> Unit) {
    private val _selectedItems = MutableStateFlow<Set<T>>(emptySet())
    private val _confirmDeleteDialogShown = MutableStateFlow(false)
    val confirmDeleteDialogShown = _confirmDeleteDialogShown.asStateFlow()
    val selectedItems = _selectedItems.asStateFlow()
    val isEditMode = _selectedItems.map { it.isNotEmpty() }

    private fun toggleSelection(item: T) {
        _selectedItems.update { currentSelection ->
            if (item in currentSelection) currentSelection - item else currentSelection + item
        }
    }

    fun clearSelection() {
        _selectedItems.value = emptySet()
    }

    fun handleCommonEvents(event: SelectionEvent<T>) {
        when (event) {
            is SelectionEvent.ToggleSelection -> toggleSelection(event.item)
            is SelectionEvent.ClearSelection -> clearSelection()
            is SelectionEvent.DeleteSelected -> _confirmDeleteDialogShown.value = true
            is SelectionEvent.DismissDeletionDialog -> _confirmDeleteDialogShown.value = false
            is SelectionEvent.ConfirmDeletion -> {
                val itemsToDelete = _selectedItems.value
                if (itemsToDelete.isNotEmpty()) {
                    deleteItems(itemsToDelete)
                    clearSelection()
                }

                _confirmDeleteDialogShown.value = false
            }
        }
    }
}