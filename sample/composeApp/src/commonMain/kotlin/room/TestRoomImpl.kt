package room

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.room.dao.DaoType
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.room.dao.RoomPagingDao
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.room.key.PagingPrimaryKey
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.room.key.PagingQueryKey
import kotlinx.coroutines.flow.Flow

@Entity
data class TestRoomEntity(@PrimaryKey val id:Long, val data:String, val creationTime: Long )

sealed class  TestRoomKey:PagingQueryKey(){
        data class ById(val id:Long):TestRoomKey()
        data object Default:TestRoomKey()
}

@Dao
interface TestRoomDao:RoomPagingDao<TestRoomKey,TestRoomEntity>{
    override val daoType: DaoType
        get() = DaoType.UserData

    override suspend fun deleteByPrimaryKey(primaryKey: PagingPrimaryKey) {
        when (primaryKey) {
            is PagingPrimaryKey.LongKey -> {
                deleteByKey(TestRoomKey.ById(primaryKey.value))
            }

            is PagingPrimaryKey.StringKey -> Unit
        }
    }

    @Insert(onConflict = OnConflictStrategy.ABORT)
    override suspend fun insert(data: TestRoomEntity)

    @Query("SELECT * FROM TestRoomEntity")
    suspend fun selectAll(): List<TestRoomEntity>

    @Query("SELECT * FROM TestRoomEntity WHERE id= :id")
    suspend fun selectById(id: Long): TestRoomEntity?

    override suspend fun selectSingle(key: TestRoomKey): TestRoomEntity? {
        if (key is TestRoomKey.ById) {
            return selectById(key.id)
        }
        return super.selectSingle(key)
    }

    override suspend fun selectList(key: TestRoomKey): List<TestRoomEntity> = selectAll()

    @Query("SELECT * FROM TestRoomEntity")
    fun selectAllFlow(): Flow<List<TestRoomEntity>>

    override fun selectFlowList(key: TestRoomKey): Flow<List<TestRoomEntity>> = selectAllFlow()

    @Query("SELECT * FROM TestRoomEntity WHERE id= :id")
    fun selectFlowById(id: Long): Flow<TestRoomEntity?>

    override fun selectFlowSingle(key: TestRoomKey): Flow<TestRoomEntity?> {
        if (key is TestRoomKey.ById) {
            return selectFlowById(key.id)
        }
        return super.selectFlowSingle(key)
    }

    @Upsert
    override suspend fun insertOrUpdate(data: TestRoomEntity)

    @Query("DELETE FROM TestRoomEntity WHERE id= :id")
    suspend fun deleteById(id: Long)

    override suspend fun deleteByKey(key: TestRoomKey) {
        if (key is TestRoomKey.ById) {
            deleteById(key.id)
        }
    }

    @Query("DELETE FROM TestRoomEntity")
    override suspend fun deleteAll()

    @Query("SELECT creationTime FROM TestRoomEntity ORDER BY creationTime LIMIT 1")
    override suspend fun selectCreationTime(): Long?

    @Delete
    override suspend fun delete(data: TestRoomEntity)

    @Query(
        """
    SELECT * FROM TestRoomEntity ORDER BY id DESC
""",
    )
    fun selectPagingSourceByFilter(
    ): PagingSource<Int, TestRoomEntity>

    override fun selectPagingSource(key: TestRoomKey): PagingSource<Int, TestRoomEntity> =
        when (key) {
            is TestRoomKey.Default-> {
                selectPagingSourceByFilter(
                )
            }

            else -> super.selectPagingSource(key)
        }
}