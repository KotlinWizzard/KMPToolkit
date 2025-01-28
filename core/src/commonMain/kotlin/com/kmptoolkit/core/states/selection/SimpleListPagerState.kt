package com.kmptoolkit.core.states.selection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class SimpleListPagerState<Data>(
    initialData: List<Data>,
    mode: Mode = Mode.Cyclic,
) {
    var currentPage by mutableStateOf(0)
        private set

    var items by mutableStateOf(initialData)
        private set
    var mode by mutableStateOf(mode)

    val totalPages: Int by derivedStateOf { items.count() }

    val hasNextPage: Boolean by derivedStateOf {
        if (mode == Mode.Cyclic) {
            totalPages > 1
        } else {
            currentPage < items.lastIndex
        }
    }

    val currentItem by derivedStateOf {
        items[currentPage]
    }

    val hasPreviousPage: Boolean by derivedStateOf {
        if (mode == Mode.Cyclic) {
            totalPages > 1
        } else {
            currentPage > 0
        }
    }

    fun nextPage() {
        if (hasNextPage) {
            if (currentPage < items.lastIndex) {
                currentPage++
            } else if (currentPage >= items.lastIndex && mode == Mode.Cyclic) {
                currentPage = 0
            }
        }
    }

    fun previousPage() {
        if (hasPreviousPage) {
            if (currentPage > 0) {
                currentPage--
            } else if (currentPage == 0 && mode == Mode.Cyclic) {
                currentPage = items.lastIndex
            }
        }
    }

    fun updateItems(data: List<Data>) {
        items = data
    }

    fun updatePageManually(page: Int) {
        if (page in items.indices) {
            currentPage = page
        }
    }

    enum class Mode {
        Cyclic,
        Strict,
    }
}

@Composable
fun <Data> rememberSimpleListPagerState(
    items: List<Data>,
    mode: SimpleListPagerState.Mode = SimpleListPagerState.Mode.Cyclic,
) = remember {
    SimpleListPagerState(items, mode)
}