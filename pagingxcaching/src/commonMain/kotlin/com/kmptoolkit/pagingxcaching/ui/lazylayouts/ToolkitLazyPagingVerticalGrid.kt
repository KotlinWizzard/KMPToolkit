package com.kmptoolkit.pagingxcaching.ui.lazylayouts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.kmptoolkit.pagingxcaching.service.paging.infrastructure.BasicApiPagingViewModelService
import com.kmptoolkit.pagingxcaching.state.error.ErrorState
import com.kmptoolkit.pagingxcaching.ui.LocalErrorDefaults
import com.kmptoolkit.pagingxcaching.ui.ToolkitErrorDefaults
import com.kmptoolkit.pagingxcaching.ui.ToolkitPagingLayout
import com.kmptoolkit.pagingxcaching.ui.ToolkitPullToRefreshMode

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <Actual : Any> ToolkitLazyPagingVerticalGrid(
    columns: GridCells,
    modifier: Modifier = Modifier,
    viewModel: BasicApiPagingViewModelService<*, Actual>,
    pagingItems: LazyPagingItems<Actual> = viewModel.lazyPagingItems,
    errorState: ErrorState = viewModel.errorState,
    pullToRefreshMode: ToolkitPullToRefreshMode = ToolkitPullToRefreshMode.None,
    errorDefaults: ToolkitErrorDefaults = LocalErrorDefaults.current,
    state: LazyGridState = rememberLazyGridState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical = if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
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
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            state = state,
            contentPadding = contentPadding,
            reverseLayout = reverseLayout,
            verticalArrangement = verticalArrangement,
            horizontalArrangement = horizontalArrangement,
            flingBehavior = flingBehavior,
            userScrollEnabled = userScrollEnabled,
            columns = columns,
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