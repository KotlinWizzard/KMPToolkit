package io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.infrastructure

import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.dao.CacheDao
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.room.key.QueryKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull

abstract class SingleCachedViewModelService<Key : QueryKey, Network : Any, Local : Any, Output : Any>(
    dao: CacheDao<Key, Network, Local, Output>,
    fetcher: CacheFetcher<Key, Network>,
    refreshAction: io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.infrastructure.CachedRefreshAction = io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.infrastructure.CachedRefreshAction.RefreshAndUpdate,
) : CachedViewModelService<Key, Network, Local, Output, Output?>(
        dao = dao,
        fetcher = fetcher,
        refreshAction = refreshAction,
    ) {
    init {
        require(fetcher is CacheFetcher.LocalOnly || fetcher is CacheFetcher.NetworkSingle)
    }

    override fun getInitialFlowType(): Output? = null

    override fun getDatabaseFlow(): Flow<Output?> = lastKey?.let { dao.selectAsFlowSingle(it) } ?: emptyFlow()

    override suspend fun localDataNotEmpty(): Boolean = flow.firstOrNull() != null

    override fun validateNotEmpty(data: Output?): Boolean = data != null
}
