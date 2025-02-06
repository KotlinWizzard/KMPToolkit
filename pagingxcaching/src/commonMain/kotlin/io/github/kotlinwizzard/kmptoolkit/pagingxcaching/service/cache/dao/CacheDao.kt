package io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.dao

import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.converter.CacheConverter
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.room.key.QueryKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

abstract class CacheDao<Key : QueryKey, Network : Any, Local : Any, Output : Any>(
    val converter: CacheConverter<Network, Local, Output>,
    private val defaultCoroutineContext: CoroutineContext = Dispatchers.IO,
) : LocalCrudDao<Key, Local>() {
    fun insert(
        data: Output,
        coroutineContext: CoroutineContext = defaultCoroutineContext,
    ) = CoroutineScope(coroutineContext).launch {
        insertSuspended(data)
    }

    suspend fun insertSuspended(data: Output) = insertLocal(converter.mapOutputToLocal(data))

    suspend fun insertAllSuspended(data: List<Output>) =
        withTransaction {
            data.forEach {
                insertSuspended(it)
            }
        }

    fun insertAll(
        data: List<Output>,
        coroutineContext: CoroutineContext = defaultCoroutineContext,
    ) = CoroutineScope(coroutineContext).launch {
        insertAllSuspended(data)
    }

    suspend fun insertOrUpdateSuspended(data: Output) {
        insertOrUpdateLocal(converter.mapOutputToLocal(data))
    }

    fun insertOrUpdate(
        data: Output,
        coroutineContext: CoroutineContext = defaultCoroutineContext,
    ) = CoroutineScope(coroutineContext).launch {
        insertOrUpdateSuspended(data)
    }

    suspend fun insertOrUpdateAllSuspended(data: List<Output>) {
        withTransaction {
            data.forEach {
                insertOrUpdateSuspended(it)
            }
        }
    }

    fun insertOrUpdateAll(
        data: List<Output>,
        coroutineContext: CoroutineContext = defaultCoroutineContext,
    ) = CoroutineScope(coroutineContext).launch {
        insertOrUpdateAllSuspended(data)
    }

    fun selectAsFlowList(key: Key) = selectFlowListLocal(key).map { list -> list.map { converter.mapLocalToOutput(it) } }

    fun selectAsFlowSingle(key: Key) =
        selectFlowSingleLocal(key).map { local ->
            local?.let { it ->
                converter.mapLocalToOutput(
                    it,
                )
            }
        }

    suspend fun selectList(key: Key) = selectListLocal(key).map { converter.mapLocalToOutput(it) }

    suspend fun selectSingle(key: Key) = selectSingleLocal(key)?.let { converter.mapLocalToOutput(it) }

    fun deleteByKey(
        key: Key,
        coroutineContext: CoroutineContext = defaultCoroutineContext,
    ) = CoroutineScope(coroutineContext).launch {
        deleteByKeySuspended(key = key)
    }

    suspend fun deleteByKeySuspended(key: Key) {
        deleteByKeyLocal(key)
    }

    suspend fun deleteSuspended(data: Output) = deleteLocal(converter.mapOutputToLocal(data))

    fun delete(
        data: Output,
        coroutineContext: CoroutineContext = defaultCoroutineContext,
    ) = CoroutineScope(coroutineContext).launch {
        deleteSuspended(data)
    }

    suspend fun deleteAllSuspended() = deleteAllLocal()

    fun deleteAll(coroutineContext: CoroutineContext = defaultCoroutineContext) =
        CoroutineScope(coroutineContext).launch {
            deleteAllSuspended()
        }

    abstract suspend fun getCreationTimeMillis(): Long?
}
