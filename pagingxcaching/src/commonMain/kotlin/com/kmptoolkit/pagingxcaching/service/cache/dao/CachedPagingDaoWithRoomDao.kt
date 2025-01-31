package com.kmptoolkit.pagingxcaching.service.cache.dao

import androidx.paging.PagingSource
import com.kmptoolkit.pagingxcaching.service.cache.converter.CacheConverter
import com.kmptoolkit.pagingxcaching.service.room.dao.RoomPagingDao
import com.kmptoolkit.pagingxcaching.service.room.key.PagingQueryKey
import kotlinx.coroutines.flow.Flow

abstract class CachedPagingDaoWithRoomDao<Key : PagingQueryKey, Network : Any, Local : Any, Output : Any>(
    converter: CacheConverter<Network, Local, Output>,
    val dao: RoomPagingDao<Key, Local>,
) : CacheDao<Key, Network, Local, Output>(converter) {
    override suspend fun getCreationTimeMillis(): Long? = dao.selectCreationTime()

    override suspend fun insertLocal(data: Local) {
        dao.insert(data)
    }

    override suspend fun insertOrUpdateLocal(data: Local) {
        dao.insertOrUpdate(data)
    }

    override suspend fun deleteLocal(data: Local) {
        dao.delete(data)
    }

    override suspend fun deleteByKeyLocal(key: Key) {
        dao.deleteByKey(key)
    }

    override suspend fun deleteAllLocal() {
        dao.deleteAll()
    }

    override fun selectFlowListLocal(key: Key): Flow<List<Local>> = dao.selectFlowList(key)

    override fun selectFlowSingleLocal(key: Key): Flow<Local?> = dao.selectFlowSingle(key)

    override suspend fun selectListLocal(key: Key): List<Local> = dao.selectList(key)

    override suspend fun selectSingleLocal(key: Key): Local? = dao.selectSingle(key)

    override suspend fun withTransaction(transaction: suspend () -> Unit) =
        dao.withTransaction(transaction = transaction)

    fun selectPagingSource(key: Key): PagingSource<Int, Local> = dao.selectPagingSource(key)

    abstract suspend fun deleteByPrimaryKey(key: String)
}