package io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.room.remotekey

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.room.dao.DaoType
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.room.dao.DaoTypeProvider
import kotlinx.datetime.Clock

@Dao
interface RemoteKeyRoomDao<T:RemoteKey> : DaoTypeProvider {
    override val daoType: DaoType
        get() = DaoType.UserData

    fun createRemoteKey(
       remoteKeyData: String,
       type: String,
       queryHash: String,
        currentPage: Int,
       previousPage: Int?,
       nextPage: Int?,
       creationTime: Long = Clock.System.now().toEpochMilliseconds(),
    ):T

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(data: T)

    @Query("SELECT * FROM RemoteKey WHERE remoteKeyData = :id")
    suspend fun selectById(id: String): T?

    @Query("SELECT * FROM RemoteKey WHERE type = :type AND queryHash = :queryHash  ")
    suspend fun selectAllByTypeAndQueryHash(
        type: String,
        queryHash: String,
    ): List<T>

    @Upsert
    suspend fun insertOrUpdate(data: T)

    @Query("DELETE FROM RemoteKey WHERE remoteKeyData = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM RemoteKey WHERE type = :type")
    suspend fun deleteAllByType(type: String)

    @Query("DELETE FROM RemoteKey")
    suspend fun deleteAll()

    @Query("SELECT creationTime FROM RemoteKey WHERE type = :type AND queryHash = :queryHash ORDER BY creationTime LIMIT 1")
    suspend fun selectCreationTime(
        type: String,
        queryHash: String,
    ): Long?

    @Delete
    suspend fun delete(data: T)

    override suspend fun clearAll() {
        deleteAll()
    }
}