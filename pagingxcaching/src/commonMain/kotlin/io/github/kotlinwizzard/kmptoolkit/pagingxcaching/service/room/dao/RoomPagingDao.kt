package io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.room.dao

import androidx.paging.PagingSource
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.room.key.PagingQueryKey
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.room.key.PagingPrimaryKey


interface RoomPagingDao<
        Key : PagingQueryKey,
        Local : Any,
        > : RoomCacheDao<Key, Local> {
    fun selectPagingSource(key: Key): PagingSource<Int, Local> =
        throw CacheDaoMethodNotImplementedException("selectPagingSource")

    suspend fun deleteByPrimaryKey(primaryKey: PagingPrimaryKey): Unit =
        throw CacheDaoMethodNotImplementedException("selectPagingSource")

}

