package io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.paging.infrastructure

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class ViewModelServiceWithRefresh: ViewModelService {

    private val refreshStatusFlow = MutableSharedFlow<RefreshStatus>(replay = 1)
    private val retryStatusFlow = MutableSharedFlow<RetryStatus>(replay = 1)
    val refreshStatus
        @Composable
        get() = refreshStatusFlow.collectAsStateWithLifecycle(RefreshStatus.Idle)
    val retryStatus
        @Composable
        get() = retryStatusFlow.collectAsStateWithLifecycle(RetryStatus.Idle)

    fun triggerRefresh() {
        refreshStatusFlow.tryEmit(RefreshStatus.Refresh)
    }

    private fun onRefresh(callback: () -> Unit) =
        viewModelServiceScope.launch(Dispatchers.IO) {
            refreshStatusFlow.collectLatest {
                if (it == RefreshStatus.Refresh) {
                    withContext(Dispatchers.Main) {
                        refreshStatusFlow.tryEmit(RefreshStatus.Idle)
                        callback.invoke()
                        doOnRefresh()
                    }
                }
            }
        }

    protected open fun doOnRefresh(): Unit = Unit

    protected open fun doOnRetry(): Unit = Unit

    @Composable
    fun ListenRefresh(onRefresh: () -> Unit) {
        DisposableEffect(this) {
            val job = onRefresh(onRefresh)
            onDispose {
                job.cancel()
            }
        }
    }

    @Composable
    fun ListenRetry(onRetry: () -> Unit) {
        DisposableEffect(this) {
            val job = onRetry(onRetry)
            onDispose {
                job.cancel()
            }
        }
    }

    fun triggerRetry() {
        retryStatusFlow.tryEmit(RetryStatus.Retry)
    }

    private fun onRetry(callback: () -> Unit) =
        viewModelServiceScope.launch(Dispatchers.IO) {
            retryStatusFlow.collectLatest {
                if (it == RetryStatus.Retry) {
                    withContext(Dispatchers.Main) {
                        retryStatusFlow.tryEmit(RetryStatus.Idle)
                        callback.invoke()
                        doOnRetry()
                    }
                }
            }
        }

    enum class RefreshStatus {
        Idle,
        Refresh,
    }

    enum class RetryStatus {
        Idle,
        Retry,
    }
}