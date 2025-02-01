package com.kmptoolkit.pagingxcaching.service.paging.infrastructure

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.paging.ExperimentalPagingApi
import com.kmptoolkit.core.classes.LazyLayoutKeyProvider
import com.kmptoolkit.pagingxcaching.service.cache.dao.CachedPagingDaoWithRoomDao
import com.kmptoolkit.pagingxcaching.service.cache.infrastructure.CacheFetcher
import com.kmptoolkit.pagingxcaching.service.cache.infrastructure.CachedRefreshAction
import com.kmptoolkit.pagingxcaching.service.cache.mediator.PagingKeyProvider
import com.kmptoolkit.pagingxcaching.service.cache.mediator.PagingPrimaryKeyProvider
import com.kmptoolkit.pagingxcaching.service.cache.mediator.RemoteKeyPagingMediator
import com.kmptoolkit.pagingxcaching.service.paging.PageImpl
import com.kmptoolkit.pagingxcaching.service.paging.PagingSourceProvider
import com.kmptoolkit.pagingxcaching.service.room.key.PagingQueryKey
import com.kmptoolkit.pagingxcaching.service.room.remotekey.RemoteKeyDao
import kotlin.reflect.KClass

abstract class ApiCachedViewModelService<PageDTO : Any, DTO : Any, Actual : LazyLayoutKeyProvider, Key : PagingQueryKey, Local : PagingPrimaryKeyProvider>(
    protected val dao: CachedPagingDaoWithRoomDao<Key, *, Local, Actual>,
    protected val remoteKeyDao: RemoteKeyDao,
    protected val cachedRefreshAction: CachedRefreshAction,
) : BasicApiPagingViewModelService<Local, Actual>(),
    PagingKeyProvider<Key> {
    abstract val pageImplClass: KClass<PageDTO>

    var key: Key? by mutableStateOf(null)
        private set

    private val pagingKeyProvider: PagingKeyProvider<Key>
        get() = this

    override val pagingKey: State<Key?>
        get() =
            derivedStateOf {
                key
            }
    override val pagingSourceProvider: PagingSourceProvider<Local, Actual>
        get() =
            PagingSourceProvider.LocalPagingSource(
                source = { dao.selectPagingSource(key ?: throw Exception("not initialized")) },
                map = { dao.converter.mapLocalToOutput(it) },
            )

    override fun getKey(data: Actual): Any = data.getKey()

    @ExperimentalPagingApi
    override val remoteMediator =
        RemoteKeyPagingMediator(
            dao = dao,
            remoteKeyDao = remoteKeyDao,
            pagingKeyProvider = pagingKeyProvider,
            refreshAction = cachedRefreshAction,
            fetcher =
            CacheFetcher.NetworkPage(
                fetchData = { key, page ->
                    getPaginatedData(page, key)
                },
            ),
        )

    private suspend fun getPaginatedData(
        page: Int,
        key: Key,
    ): PageImpl<Actual> =
        mapPage(
            fetchPagingData(page, key).toPage()
        )


    abstract fun PageDTO.toPage(): PageImpl<DTO>

    abstract fun DTO.toActual(): Actual

    private fun mapPage(pageImpl: PageImpl<DTO>): PageImpl<Actual> =
        pageImpl.mapNotNull { it.toActual() }

    abstract suspend fun fetchPagingData(page: Int, key: Key): PageDTO

    protected open fun shouldLoadWithKey(key: Key): Boolean = true

    protected open fun areKeysTheSame(
        currentKey: Key,
        lastKey: Key,
    ): Boolean = currentKey == lastKey

    fun loadWithKey(
        key: Key,
        checkIfKeyChanged: Boolean = false,
    ) {
        val lastKey = this.key
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
        this.key = key
        triggerRefresh()
    }

    fun updateKey(key: Key) {
        this.key = key
    }
}