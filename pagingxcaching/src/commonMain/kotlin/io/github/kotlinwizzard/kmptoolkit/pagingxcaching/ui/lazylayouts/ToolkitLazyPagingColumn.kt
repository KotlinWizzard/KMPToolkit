package io.github.kotlinwizzard.kmptoolkit.pagingxcaching.ui.lazylayouts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
fun <Actual : Any> ToolkitLazyPagingColumn(
    modifier: Modifier = Modifier,
    viewModel: BasicApiPagingViewModelService<*, Actual>,
    pagingItems: LazyPagingItems<Actual> = viewModel.lazyPagingItems,
    errorState: ErrorState = viewModel.errorState,
    pullToRefreshMode: ToolkitPullToRefreshMode = ToolkitPullToRefreshMode.None,
    errorDefaults: ToolkitErrorDefaults = LocalErrorDefaults.current,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    header: @Composable (() -> Unit)? = null,
    divider: @Composable () -> Unit = {},
    itemContent: @Composable LazyItemScope.(index: Int, data: Actual) -> Unit,
) {
    ToolkitPagingLayout(
        modifier = modifier,
        viewModel = viewModel,
        pagingItems = pagingItems,
        errorState = errorState,
        errorDefaults = errorDefaults,
        pullToRefreshMode = pullToRefreshMode,
    ) {
        LazyColumn(
            modifier = modifier,
            state = state,
            contentPadding = contentPadding,
            reverseLayout = reverseLayout,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            flingBehavior = flingBehavior,
            userScrollEnabled = userScrollEnabled,
        ) {
            header?.let {
                stickyHeader {
                    it.invoke()
                }
            }
            items(pagingItems.itemCount, key = { index ->
                pagingItems[index]?.let { viewModel.getKey(it) } ?: index
            }) { index ->
                val data = pagingItems[index] ?: return@items
                if (index > 0 && pagingItems.itemCount > 1) {
                    divider.invoke()
                }
                itemContent.invoke(this, index, data)
            }
        }
    }
}