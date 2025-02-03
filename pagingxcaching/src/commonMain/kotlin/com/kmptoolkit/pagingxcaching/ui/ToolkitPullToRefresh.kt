package com.kmptoolkit.pagingxcaching.ui

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.PositionalThreshold
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.kmptoolkit.core.presentation.theme.ToolkitTheme
import com.kmptoolkit.pagingxcaching.extensions.doOnRefresh
import com.kmptoolkit.pagingxcaching.service.paging.infrastructure.BasicApiPagingViewModelService
import com.kmptoolkit.pagingxcaching.state.error.ErrorState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <Actual : Any> ToolkitPullToRefresh(
    modifier: Modifier = Modifier,
    viewModel: BasicApiPagingViewModelService<*, Actual>,
    errorState: ErrorState = viewModel.errorState,
    pullToRefreshState: PullToRefreshState =
        rememberPullToRefreshState(),
    pullToRefreshMode: ToolkitPullToRefreshMode.PullToRefresh = ToolkitPullToRefreshMode.PullToRefresh(),
    content: @Composable BoxScope.() -> Unit
) {
    PullToRefreshBox(
        modifier =
        modifier,
        state = pullToRefreshState,
        content = {
            content(this)
        },
        contentAlignment = Alignment.TopStart,
        indicator = {
            Indicator(
                modifier =
                pullToRefreshMode.indicatorModifier,
                isRefreshing = false,
                state = pullToRefreshState,
                threshold =
                pullToRefreshMode.distanceThreshold,
                containerColor = pullToRefreshMode.indicatorColor(),
                color = pullToRefreshMode.indicatorBackground(),
            )
        },
        isRefreshing = errorState.isLoading,
        onRefresh = {
            viewModel.triggerRefresh()
        },
    )
}

sealed class ToolkitPullToRefreshMode {
    data object None : ToolkitPullToRefreshMode()
    data class PullToRefresh @OptIn(ExperimentalMaterial3Api::class) constructor(
        val distanceThreshold: Dp = PositionalThreshold,
        val indicatorColor: @Composable () -> Color = {ToolkitTheme.colorScheme.primary},
        val indicatorBackground: @Composable () -> Color = {ToolkitTheme.colorScheme.surface},
        val indicatorModifier: Modifier = Modifier
    ) :
        ToolkitPullToRefreshMode()
}