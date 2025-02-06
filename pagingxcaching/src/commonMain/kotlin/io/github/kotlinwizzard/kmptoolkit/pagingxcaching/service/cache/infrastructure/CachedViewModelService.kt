package io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.infrastructure

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.kotlinwizzard.kmptoolkit.core.util.LifecycleEffect
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.api.ApiResult
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.dao.CacheDao
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.paging.infrastructure.ViewModelService
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.room.key.QueryKey
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.state.error.ErrorState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock


abstract class CachedViewModelService<Key : QueryKey, Network : Any, Local : Any, Output : Any, FlowType>(
    val dao: CacheDao<Key, Network, Local, Output>,
    protected val fetcher: CacheFetcher<Key, Network>,
    val refreshAction: io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.infrastructure.CachedRefreshAction = io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.infrastructure.CachedRefreshAction.RefreshAndUpdate,
) : ViewModelService {
    private val retryTrigger = RetryTrigger()

    abstract fun getInitialKey(): Key

    protected var lastKey: Key? = null

    protected open fun areKeysTheSame(
        currentKey: Key,
        lastKey: Key,
    ): Boolean = currentKey == lastKey

    protected open fun shouldLoadWithKey(key: Key): Boolean = true

    private var isNetworkLoading by mutableStateOf(true)

    fun loadWithKey(
        key: Key,
        checkIfKeyChanged: Boolean = false,
    ) {
        val lastKey = this.lastKey
        if (checkIfKeyChanged &&
            lastKey != null &&
            areKeysTheSame(
                currentKey = key,
                lastKey = lastKey,
            )
        ) {
            return
        }
        if (!shouldLoadWithKey(key)) return
        if (lastKey == null) {
            errorState.onApiCallStarted()
        }
        this.lastKey = key
        retryOrRefresh()
    }

    val errorState = ErrorState()

    protected val flow =
        retryFlow(retryTrigger) { getDatabaseFlow() }.onEach {
            if (validateNotEmpty(data = it)) {
                errorState.onSuccess()
            } else {
                if (fetcher is CacheFetcher.LocalOnly || !isNetworkLoading) {
                    errorState.onApiResult(ApiResult.Empty)
                }
            }
        }

    @Composable
    fun RefreshWithLifecycle() {
        LifecycleEffect(
            onResume = {
                retryOrRefresh()
            },
        )
    }

    protected abstract fun validateNotEmpty(data: FlowType): Boolean

    protected abstract fun getInitialFlowType(): FlowType

    protected abstract fun getDatabaseFlow(): Flow<FlowType>

    val state
        @Composable get() =
            flow
                .collectAsState(getInitialFlowType())

    private fun handleFetcher(ignoreRefreshCheck: Boolean = false) =
        viewModelServiceScope.launch(Dispatchers.IO) {
            if (!ignoreRefreshCheck) {
                if (!shouldRefresh()) {
                    isNetworkLoading = true
                    return@launch
                }
            }
            when (fetcher) {
                is CacheFetcher.NetworkList -> {
                    updateFlowFromNetwork(fetcher)
                }

                is CacheFetcher.NetworkSingle -> {
                    updateFlowFromNetwork(fetcher)
                }

                else -> Unit
            }
            isNetworkLoading = true
        }

    protected abstract suspend fun localDataNotEmpty(): Boolean

    private suspend fun updateFlowFromNetwork(fetcher: CacheFetcher.NetworkList<Key, Network>) {
        val key = lastKey ?: getInitialKey()
        val response = fetcher.fetchData.invoke(key)
        val result = response.successResultOrNull
        if (!result.isNullOrEmpty()) {
            dao.withTransaction {
                if (shouldDelete()) {
                    dao.deleteAllSuspended()
                }
                result.let { list ->
                    val mappedList = list.map { dao.converter.mapNetworkToOutput(it) }
                    dao.insertOrUpdateAllSuspended(mappedList)
                }
            }
        }
        val localDataNotEmpty = localDataNotEmpty()
        if (!localDataNotEmpty()) {
            errorState.onApiResult(response)
        }
    }

    private suspend fun updateFlowFromNetwork(fetcher: CacheFetcher.NetworkSingle<Key, Network>) {
        val key = lastKey ?: getInitialKey()
        val response = fetcher.fetchData.invoke(key)
        val result = response.successResultOrNull
        if (result != null) {
            dao.withTransaction {
                if (shouldDelete()) {
                    dao.deleteByKey(key)
                }
                result.let { item ->
                    val mappedItem = dao.converter.mapNetworkToOutput(item)
                    dao.insertOrUpdateSuspended(mappedItem)
                }
            }
        }
        if (!localDataNotEmpty()) {
            errorState.onApiResult(response)
        }
    }

    fun retryOrRefresh() {
        if (retryTrigger.retryEvent.value != RetryTrigger.State.RETRYING) {
            isNetworkLoading = true
            errorState.onApiCallStarted()
            retryTrigger.retry()
            handleFetcher(ignoreRefreshCheck = true)
        }
    }

    private fun shouldDelete(): Boolean =
        refreshAction is io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.infrastructure.CachedRefreshAction.RefreshAndDelete || refreshAction is io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.infrastructure.CachedRefreshAction.RefreshAndDeleteOnTime

    private suspend fun shouldRefresh(): Boolean =
        when (refreshAction) {
            io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.infrastructure.CachedRefreshAction.RefreshAndDelete -> true
            is io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.infrastructure.CachedRefreshAction.RefreshAndDeleteOnTime -> shouldRefreshOnTime(refreshAction.cacheTimeoutMillis)
            io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.infrastructure.CachedRefreshAction.RefreshAndUpdate -> true
            is io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.infrastructure.CachedRefreshAction.RefreshAndUpdateOnTime -> shouldRefreshOnTime(refreshAction.cacheTimeoutMillis)
            io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.infrastructure.CachedRefreshAction.RefreshNever -> false
        }

    private suspend fun shouldRefreshOnTime(timeout: Long): Boolean {
        val creationTime = dao.getCreationTimeMillis() ?: 0L
        val now = Clock.System.now().toEpochMilliseconds()
        return now - creationTime >= timeout
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun retryFlow(
        retryTrigger: RetryTrigger,
        flowProvider: () -> Flow<FlowType>,
    ) = retryTrigger.retryEvent
        .flatMapLatest { state ->
            if (state == RetryTrigger.State.RETRYING) {
                flowProvider().onEach {
                    retryTrigger.setIdle()
                }
            } else {
                flowProvider()
            }
        }


    private val coroutineScope
        get() = viewModelServiceScope

    init {
        coroutineScope.launch {
            loadWithKey(getInitialKey(), true)
        }
    }
}