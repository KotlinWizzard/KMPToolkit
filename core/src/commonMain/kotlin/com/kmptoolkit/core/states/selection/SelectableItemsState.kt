package com.kmptoolkit.core.states.selection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import com.kmptoolkit.core.classes.LazyLayoutKeyProvider


class SelectableItemsState<Item : LazyLayoutKeyProvider>(
    val mode: SelectableItemMode = SelectableItemMode.Multiple(),
) {
    private val keySelection = mutableStateMapOf<Any, Boolean>()
    private val selectedItems = mutableStateMapOf<Any, Item>()
    val selectedItemsList: List<Item> by derivedStateOf {
        selectedItems.values.toList()
    }

    val atLeastOneItemSelected =
        derivedStateOf {
            keySelection.values.any { it }
        }

    val countSelectedItems =
        derivedStateOf {
            keySelection.values.count { it }
        }

    private val limitReached by derivedStateOf { selectedItems.count() >= mode.maxItems }

    fun onItemClick(item: Item) {
        if (itemSelected(item)) {
            unselectItem(item)
        } else {
            selectItem(item)
        }
    }

    private fun setItem(
        item: Item,
        value: Boolean,
    ) {
        if (!limitReached || !value) {
            keySelection[item.getKey()] = value
        }
        if (value) {
            if (!limitReached) {
                selectedItems[item.getKey()] = item
            }
        } else {
            selectedItems.remove(item.getKey())
        }
    }

    private fun selectItem(item: Item) {
        if (mode is SelectableItemMode.Single) {
            keySelection.keys.forEach {
                keySelection[it] = false
            }
            selectedItems.clear()
        }
        setItem(item, true)
    }

    fun clearAll() {
        selectedItems.clear()
        keySelection.clear()
    }

    private fun unselectItem(item: Item) {
        setItem(item, false)
    }

    private fun itemSelected(item: Item) = keySelection[item.getKey()] ?: false

    @Composable
    fun isItemSelected(item: Item): Boolean =
        remember(keySelection) {
            derivedStateOf { itemSelected(item) }
        }.value
}