package io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.room.dao

import androidx.room.Transaction
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.room.key.QueryKey
import kotlinx.coroutines.flow.Flow

interface RoomCacheDao<Key : QueryKey, Local : Any>: DaoTypeProvider {
    suspend fun insert(data: Local)

    suspend fun insertOrUpdate(data: Local)

    suspend fun delete(data: Local)

    suspend fun deleteByKey(key: Key)

    suspend fun deleteAll()

    fun selectFlowList(key: Key): Flow<List<Local>> = throw CacheDaoMethodNotImplementedException("selectFlowList")

    fun selectFlowSingle(key: Key): Flow<Local?> = throw CacheDaoMethodNotImplementedException("selectFlowSingle")

    suspend fun selectList(key: Key): List<Local> = throw CacheDaoMethodNotImplementedException("selectList")

    suspend fun selectSingle(key: Key): Local? = throw CacheDaoMethodNotImplementedException("selectSingle")

    suspend fun selectCreationTime(): Long?

    suspend fun withTransaction(transaction: suspend () -> Unit) =

        performTransaction(
            object : TransactionCallback {
                override suspend fun transaction() {
                    transaction.invoke()
                }
            },
        )

    @Transaction
    suspend fun performTransaction(callback: TransactionCallback) {
        callback.transaction()
    }

    override suspend fun clearAll() {
        deleteAll()
    }
}