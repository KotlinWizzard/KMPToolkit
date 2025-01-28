package com.kmptoolkit.core.states.selection

sealed class SelectableItemMode(
    val maxItems: Int,
) {
    data object Single : SelectableItemMode(maxItems = 1)

    class Multiple(
        maxItems: Int = Int.MAX_VALUE,
    ) : SelectableItemMode(maxItems = maxItems)
}