package io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.room.remotekey

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.room.dao.DaoType
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.room.dao.DaoTypeProvider

@Dao
interface RemoteKeyRoomDao : DaoTypeProvider {
    override val daoType: DaoType
        get() = DaoType.UserData

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(data: RemoteKey)

    @Query("SELECT * FROM remote_keys WHERE remoteKeyData = :id")
    suspend fun selectById(id: String): RemoteKey?

    @Query("SELECT * FROM remote_keys WHERE type = :type AND queryHash = :queryHash  ")
    suspend fun selectAllByTypeAndQueryHash(
        type: String,
        queryHash: String,
    ): List<RemoteKey>

    @Upsert
    suspend fun insertOrUpdate(data: RemoteKey)

    @Query("DELETE FROM remote_keys WHERE remoteKeyData = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM remote_keys WHERE type = :type")
    suspend fun deleteAllByType(type: String)

    @Query("DELETE FROM remote_keys")
    suspend fun deleteAll()

    @Query("SELECT creationTime FROM remote_keys WHERE type = :type AND queryHash = :queryHash ORDER BY creationTime LIMIT 1")
    suspend fun selectCreationTime(
        type: String,
        queryHash: String,
    ): Long?

    @Delete
    suspend fun delete(data: RemoteKey)

    override suspend fun clearAll() {
        deleteAll()
    }
}