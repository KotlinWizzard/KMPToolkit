package com.kmptoolkit.pagingxcaching.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import com.kmptoolkit.pagingxcaching.extensions.handleObserver
import com.kmptoolkit.pagingxcaching.service.paging.infrastructure.BasicApiPagingViewModelService
import com.kmptoolkit.pagingxcaching.state.error.ErrorState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <Actual : Any> ToolkitPagingLayout(
    modifier: Modifier = Modifier,
    viewModel: BasicApiPagingViewModelService<*, Actual>,
    pagingItems: LazyPagingItems<Actual> = viewModel.lazyPagingItems,
    errorState: ErrorState = viewModel.errorState,
    pullToRefreshMode: ToolkitPullToRefreshMode = ToolkitPullToRefreshMode.None,
    errorDefaults: ToolkitErrorDefaults = LocalErrorDefaults.current,
    content: @Composable BoxScope.(LazyPagingItems<Actual>) -> Unit,
) {
    viewModel.ListenRefresh {
        viewModel.launchPaging()
        pagingItems.refresh()
    }
    viewModel.ListenRetry {
        viewModel.launchPaging()
        pagingItems.retry()
    }
    pagingItems.handleObserver(errorState)
    ToolkitErrorOverlayLayout(
        modifier = Modifier,
        errorState = errorState,
        errorDefaults = errorDefaults
    ) {
        if (pagingItems.itemCount > 0) {
            when (pullToRefreshMode) {
                ToolkitPullToRefreshMode.None -> {
                    Box(modifier) {
                        content(this, pagingItems)
                    }
                }

                is ToolkitPullToRefreshMode.PullToRefresh -> {
                    ToolkitPullToRefresh(
                        modifier = modifier,
                        viewModel = viewModel,
                        errorState = errorState,
                        pullToRefreshMode = pullToRefreshMode,
                        content = {
                            content(this, pagingItems)
                        }
                    )
                }
            }
        }
    }
}

