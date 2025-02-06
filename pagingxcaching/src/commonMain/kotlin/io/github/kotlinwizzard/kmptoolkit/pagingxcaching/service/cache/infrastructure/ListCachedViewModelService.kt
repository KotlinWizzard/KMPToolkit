package io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.infrastructure

import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.dao.CacheDao
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.room.key.QueryKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull

abstract class ListCachedViewModelService<Key : QueryKey, Network : Any, Local : Any, Output : Any>(
    dao: CacheDao<Key, Network, Local, Output>,
    fetcher: CacheFetcher<Key, Network>,
    refreshAction: io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.infrastructure.CachedRefreshAction = io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.infrastructure.CachedRefreshAction.RefreshAndUpdate,
) : CachedViewModelService<Key, Network, Local, Output, List<Output>>(
        dao = dao,
        fetcher = fetcher,
        refreshAction = refreshAction,
    ) {
    init {
        require(fetcher is CacheFetcher.LocalOnly || fetcher is CacheFetcher.NetworkList)
    }

    override fun getInitialFlowType(): List<Output> = emptyList()

    override fun getDatabaseFlow(): Flow<List<Output>> = lastKey?.let { dao.selectAsFlowList(it) } ?: emptyFlow()

    override suspend fun localDataNotEmpty(): Boolean = flow.firstOrNull()?.isNotEmpty() ?: false

    override fun validateNotEmpty(data: List<Output>): Boolean = data.isNotEmpty()
}