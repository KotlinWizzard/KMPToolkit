package io.github.kotlinwizzard.kmptoolkit.core.states.selection

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.kotlinwizzard.kmptoolkit.core.classes.LazyLayoutKeyProvider


class SingleSelectionState<Item : LazyLayoutKeyProvider>(
    initialItems: List<Item>,
    private val initialSelectedItem: Item? = initialItems.firstOrNull(),
) {
    val items by mutableStateOf(initialItems)
    var selectedItem: Item? by mutableStateOf(initialSelectedItem ?: initialItems.firstOrNull())
        private set

    fun selectItem(item: Item) {
        selectedItem = item
    }

    fun isItemSelected(item: Item) =
        derivedStateOf {
            selectedItem?.getKey() == item.getKey()
        }

    fun reset() {
        selectedItem = initialSelectedItem
    }
}