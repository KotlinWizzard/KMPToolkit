package io.github.kotlinwizzard.kmptoolkit.pagingxcaching.ui.lazylayouts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.paging.infrastructure.BasicApiPagingViewModelService
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.state.error.ErrorState
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.ui.LocalErrorDefaults
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.ui.ToolkitErrorDefaults
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.ui.ToolkitPagingLayout
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.ui.ToolkitPullToRefreshMode

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <Actual : Any> ToolkitLazyPagingHorizontalGrid(
    rows: GridCells,
    modifier: Modifier = Modifier,
    viewModel: BasicApiPagingViewModelService<*, Actual>,
    pagingItems: LazyPagingItems<Actual> = viewModel.lazyPagingItems,
    errorState: ErrorState = viewModel.errorState,
    pullToRefreshMode: ToolkitPullToRefreshMode = ToolkitPullToRefreshMode.None,
    errorDefaults: ToolkitErrorDefaults = LocalErrorDefaults.current,
    state: LazyGridState = rememberLazyGridState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    horizontalArrangement: Arrangement.Horizontal =
        if (!reverseLayout) Arrangement.Start else Arrangement.End,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    itemContent: @Composable LazyGridItemScope.(index: Int, data: Actual) -> Unit,
) {
    ToolkitPagingLayout(
        modifier = modifier,
        viewModel = viewModel,
        pagingItems = pagingItems,
        errorState = errorState,
        errorDefaults = errorDefaults,
        pullToRefreshMode = pullToRefreshMode,
    ) {
        LazyHorizontalGrid(
            modifier = Modifier.fillMaxSize(),
            state = state,
            contentPadding = contentPadding,
            reverseLayout = reverseLayout,
            verticalArrangement = verticalArrangement,
            horizontalArrangement = horizontalArrangement,
            flingBehavior = flingBehavior,
            userScrollEnabled = userScrollEnabled,
            rows = rows
        ) {
            items(pagingItems.itemCount, key = { index ->
                pagingItems[index]?.let { viewModel.getKey(it) } ?: index
            }) { index ->
                val data = pagingItems[index] ?: return@items
                itemContent.invoke(this, index, data)
            }
        }
    }
}