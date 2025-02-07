package room

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.room.remotekey.RemoteKey

@Database(
    entities = [
      RemoteKey::class,
    ],
    version = 1,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase:RoomDatabase(), RoomDbClearAllTablesFix{
    override fun clearAllTables() {
    }
    companion object {
        const val DATABASE_NAME = "test-app-database.db"
    }
}

interface RoomDbClearAllTablesFix {
    fun clearAllTables()
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
