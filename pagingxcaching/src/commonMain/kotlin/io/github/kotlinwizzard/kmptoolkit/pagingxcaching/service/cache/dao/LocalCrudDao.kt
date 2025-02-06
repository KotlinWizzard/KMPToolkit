package io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.dao

import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.room.key.QueryKey
import kotlinx.coroutines.flow.Flow

abstract class LocalCrudDao<Key : QueryKey, Local : Any> {
    protected abstract suspend fun insertLocal(data: Local)

    protected abstract suspend fun insertOrUpdateLocal(data: Local)

    protected abstract suspend fun deleteLocal(data: Local)

    protected abstract suspend fun deleteByKeyLocal(key: Key)

    protected abstract suspend fun deleteAllLocal()

    protected abstract fun selectFlowListLocal(key: Key): Flow<List<Local>>

    protected abstract fun selectFlowSingleLocal(key: Key): Flow<Local?>

    protected abstract suspend fun selectListLocal(key: Key): List<Local>

    protected abstract suspend fun selectSingleLocal(key: Key): Local?

    abstract suspend fun withTransaction(transaction: suspend () -> Unit)
}