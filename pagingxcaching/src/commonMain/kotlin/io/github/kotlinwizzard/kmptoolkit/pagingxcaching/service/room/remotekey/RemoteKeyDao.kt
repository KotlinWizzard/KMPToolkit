package io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.room.remotekey

import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.mediator.PagingPrimaryKeyProvider
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.room.key.PagingPrimaryKey
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.room.key.PagingQueryKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

class RemoteKeyDao(
    val remoteKeyRoomDao: RemoteKeyRoomDao,
    val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) {
    fun mapToRemoteKey(
        keyProvider: PagingPrimaryKeyProvider,
        pagingQueryKey: PagingQueryKey,
        details: RemoteKeyDetails,
    ): RemoteKey {
        val key = constructKey(keyProvider, pagingQueryKey)
        return RemoteKey(
            remoteKeyData = key,
            type = pagingQueryKey.getQueryType(),
            queryHash = pagingQueryKey.getQueryHash(),
            currentPage = details.currentPage,
            nextPage = details.nextPage,
            previousPage = details.previousPage,
        )
    }

    private fun constructKey(
        keyProvider: PagingPrimaryKeyProvider,
        pagingQueryKey: PagingQueryKey,
    ): String =
        pagingQueryKey.getQueryType() + ":" + pagingQueryKey.getQueryHash() + ":" +
                keyProvider
                    .getPagingPrimaryKey().asString()

    fun insert(
        keyProvider: PagingPrimaryKeyProvider,
        pagingQueryKey: PagingQueryKey,
        details: RemoteKeyDetails,
    ) {
        val remoteKey =
            mapToRemoteKey(
                keyProvider = keyProvider,
                pagingQueryKey = pagingQueryKey,
                details = details,
            )
        scope.launch {
            remoteKeyRoomDao.insertOrUpdate(data = remoteKey)
        }
    }

    suspend fun getCreationTime(pagingQueryKey: PagingQueryKey) =
        remoteKeyRoomDao.selectCreationTime(
            type = pagingQueryKey.getQueryType(),
            queryHash = pagingQueryKey.getQueryHash(),
        )

    suspend fun getAllMatchingRemoteKeys(pagingQueryKey: PagingQueryKey): List<RemoteKey> =
        remoteKeyRoomDao.selectAllByTypeAndQueryHash(
            pagingQueryKey.getQueryType(),
            pagingQueryKey.getQueryHash(),
        )

    suspend fun delete(remoteKey: RemoteKey) = remoteKeyRoomDao.delete(remoteKey)

    suspend fun deleteById(
        keyProvider: PagingPrimaryKeyProvider,
        pagingQueryKey: PagingQueryKey,
    ) = remoteKeyRoomDao.deleteById(constructKey(keyProvider, pagingQueryKey))

    private fun getKeyStringByRemoteKey(remoteKey: RemoteKey): String? =
        remoteKey.remoteKeyData.split(":").lastOrNull()

    fun getKeyByRemoteKey(remoteKey: RemoteKey) = getKeyStringByRemoteKey(remoteKey)?.let {
        PagingPrimaryKey.fromString(it)
    }


    suspend fun getRemoteKeyById(
        keyProvider: PagingPrimaryKeyProvider,
        pagingQueryKey: PagingQueryKey,
    ): RemoteKey? = remoteKeyRoomDao.selectById(constructKey(keyProvider, pagingQueryKey))
}

