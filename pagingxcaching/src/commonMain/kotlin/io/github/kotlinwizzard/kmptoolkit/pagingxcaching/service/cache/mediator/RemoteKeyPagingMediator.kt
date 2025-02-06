package io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.mediator

import androidx.compose.ui.util.fastForEach
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import io.github.kotlinwizzard.kmptoolkit.core.classes.LazyLayoutKeyProvider
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.dao.CachedPagingDaoWithRoomDao
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.infrastructure.CacheFetcher
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.paging.exceptions.ResponseEmptyException
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.room.key.NotInitialized
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.room.key.PagingQueryKey
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.room.remotekey.RemoteKey
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.room.remotekey.RemoteKeyDao
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.room.remotekey.RemoteKeyDetails
import kotlinx.datetime.Clock
import kotlinx.io.IOException

@OptIn(ExperimentalPagingApi::class)
class RemoteKeyPagingMediator<Key : PagingQueryKey, Local : PagingPrimaryKeyProvider, Model : LazyLayoutKeyProvider>(
    val dao: CachedPagingDaoWithRoomDao<Key, *, Local, Model>,
    private val remoteKeyDao: RemoteKeyDao,
    private val pagingKeyProvider: PagingKeyProvider<Key>,
    private val refreshAction: io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.infrastructure.CachedRefreshAction,
    private val fetcher: CacheFetcher.NetworkPage<Key, Model>,
    var refreshMode: RefreshMode = RefreshMode.NORMAL
) : RemoteMediator<Int, Local>() {
    private val startPage = 0

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, Local>,
    ): MediatorResult {
        return try {
            val page =
                when (loadType) {
                    LoadType.REFRESH -> {
                        val remoteKey = getRemoteKeyClosestToCurrentPosition(state)
                        if (refreshMode == RefreshMode.IGNORE_REFRESH) {
                            refreshMode = RefreshMode.NORMAL
                            return MediatorResult.Success(endOfPaginationReached = remoteKey?.nextPage != null)
                        }
                        remoteKey?.currentPage ?: startPage
                    }

                    LoadType.PREPEND -> {
                        val remoteKey = getRemoteKeyForFirstItem(state)
                        val prevKey = remoteKey?.previousPage
                        prevKey
                            ?: return MediatorResult.Success(endOfPaginationReached = remoteKey != null)
                    }

                    LoadType.APPEND -> {
                        val remoteKey = getRemoteKeyForLastItem(state)
                        val nextKey = remoteKey?.nextPage
                        nextKey
                            ?: return MediatorResult.Success(endOfPaginationReached = remoteKey != null)
                    }
                }

            val pagingKey =
                pagingKey ?: return MediatorResult.Success(endOfPaginationReached = true)
            val dataPage =
                fetcher.fetchData(pagingKey, page)
            val content = dataPage.content
            val isEnd = !dataPage.hasNext()
            dao.withTransaction {
                if (loadType == LoadType.REFRESH &&
                    (
                            refreshAction is io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.infrastructure.CachedRefreshAction.RefreshAndDeleteOnTime ||
                                    refreshAction is io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.infrastructure.CachedRefreshAction.RefreshAndDelete
                            ) &&
                    refreshMode != RefreshMode.IGNORE_DELETE
                ) {
                    remoteKeyDao.getAllMatchingRemoteKeys(pagingKey).fastForEach { remoteKey ->
                        remoteKeyDao.delete(remoteKey)
                        val modelKey = remoteKeyDao.getKeyByRemoteKey(remoteKey)
                        if (modelKey != null) {
                            dao.deleteByPrimaryKey(modelKey)
                        }
                    }
                }
                content.forEach {
                    dao.insertOrUpdate(it)
                    remoteKeyDao.insert(
                        it.toLocal(),
                        pagingKey,
                        details =
                        RemoteKeyDetails(
                            currentPage = page,
                            previousPage = dataPage.previousPageNumber,
                            nextPage = dataPage.nextPageNumber,
                        ),
                    )
                }
            }
            if (loadType == LoadType.REFRESH && refreshMode != RefreshMode.NORMAL) {
                refreshMode = RefreshMode.NORMAL
            }
            if (loadType == LoadType.REFRESH && isEnd && content.isEmpty()) {
                throw ResponseEmptyException()
            }

            return MediatorResult.Success(
                endOfPaginationReached = isEnd,
            )
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: ResponseEmptyException) {
            return MediatorResult.Error(e)
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }

    override suspend fun initialize(): InitializeAction =
        when (refreshAction) {
            io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.infrastructure.CachedRefreshAction.RefreshAndDelete -> InitializeAction.LAUNCH_INITIAL_REFRESH
            is io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.infrastructure.CachedRefreshAction.RefreshAndDeleteOnTime -> getInitializeActionOnTime(refreshAction.cacheTimeoutMillis)
            io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.infrastructure.CachedRefreshAction.RefreshAndUpdate -> InitializeAction.LAUNCH_INITIAL_REFRESH
            is io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.infrastructure.CachedRefreshAction.RefreshAndUpdateOnTime -> getInitializeActionOnTime(refreshAction.cacheTimeoutMillis)
            io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.infrastructure.CachedRefreshAction.RefreshNever -> InitializeAction.SKIP_INITIAL_REFRESH
        }

    private val pagingKey: Key?
        get() = pagingKeyProvider.pagingKey.value

    private suspend fun getInitializeActionOnTime(timeout: Long): InitializeAction {
        val pagingKey = pagingKey ?: NotInitialized
        val creationTime =
            remoteKeyDao.getCreationTime(
                pagingQueryKey = pagingKey,
            ) ?: 0L
        return if (Clock.System.now().toEpochMilliseconds() - creationTime < timeout) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, Local>): RemoteKey? =
        state.anchorPosition?.let { position ->
            val pagingKey = pagingKey ?: return@let null
            state.closestItemToPosition(position)?.let { local ->
                val item = dao.converter.mapLocalToOutput(local)
                remoteKeyDao.getRemoteKeyById(item.toLocal(), pagingKey)
            }
        }

    private fun Model.toLocal() = dao.converter.mapOutputToLocal(this)

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, Local>): RemoteKey? =
        state.pages
            .firstOrNull {
                it.data.isNotEmpty()
            }?.data
            ?.firstOrNull()
            ?.let { local ->
                val pagingKey = pagingKey ?: return@let null
                val item = dao.converter.mapLocalToOutput(local)
                remoteKeyDao.getRemoteKeyById(item.toLocal(), pagingKey)
            }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, Local>): RemoteKey? =
        state.pages
            .lastOrNull {
                it.data.isNotEmpty()
            }?.data
            ?.lastOrNull()
            ?.let { local ->
                val pagingKey = pagingKey ?: return@let null
                val item = dao.converter.mapLocalToOutput(local)
                remoteKeyDao.getRemoteKeyById(item.toLocal(), pagingKey)
            }

    enum class RefreshMode {
        NORMAL,
        IGNORE_REFRESH,
        IGNORE_DELETE,
    }
}

