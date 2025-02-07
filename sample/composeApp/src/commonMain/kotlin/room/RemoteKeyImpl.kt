package room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.room.remotekey.RemoteKey
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.room.remotekey.RemoteKeyRoomDao

@Entity
data class RemoteKeyImpl(
    @PrimaryKey
    override val remoteKeyData: String,
    override val type: String,
    override val queryHash: String,
    override var currentPage: Int,
    override var previousPage: Int?,
    override val nextPage: Int?,
    override val creationTime: Long
) : RemoteKey(
    remoteKeyData, type, queryHash, currentPage, previousPage, nextPage, creationTime
)

@Dao
interface RemoteKeyRoomDaoImpl : RemoteKeyRoomDao<RemoteKeyImpl> {
    override fun createRemoteKey(
        remoteKeyData: String,
        type: String,
        queryHash: String,
        currentPage: Int,
        previousPage: Int?,
        nextPage: Int?,
        creationTime: Long,
    ): RemoteKeyImpl {
        return RemoteKeyImpl(
            remoteKeyData,
            type,
            queryHash,
            currentPage,
            previousPage,
            nextPage,
            creationTime
        )
    }

    @Insert(onConflict = OnConflictStrategy.ABORT)
    override suspend fun insert(data: RemoteKeyImpl)

    @Query("SELECT * FROM RemoteKeyImpl WHERE remoteKeyData = :id")
    override suspend fun selectById(id: String): RemoteKeyImpl?

    @Query("SELECT * FROM RemoteKeyImpl WHERE type = :type AND queryHash = :queryHash  ")
    override suspend fun selectAllByTypeAndQueryHash(
        type: String,
        queryHash: String,
    ): List<RemoteKeyImpl>

    @Upsert
    override suspend fun insertOrUpdate(data: RemoteKeyImpl)

    @Query("DELETE FROM RemoteKeyImpl WHERE remoteKeyData = :id")
    override suspend fun deleteById(id: String)

    @Query("DELETE FROM RemoteKeyImpl WHERE type = :type")
    override suspend fun deleteAllByType(type: String)

    @Query("DELETE FROM RemoteKeyImpl")
    override suspend fun deleteAll()

    @Query("SELECT creationTime FROM RemoteKeyImpl WHERE type = :type AND queryHash = :queryHash ORDER BY creationTime LIMIT 1")
    override suspend fun selectCreationTime(
        type: String,
        queryHash: String,
    ): Long?

    @Delete
    override suspend fun delete(data: RemoteKeyImpl)

    override suspend fun clearAll() {
        deleteAll()
    }
}