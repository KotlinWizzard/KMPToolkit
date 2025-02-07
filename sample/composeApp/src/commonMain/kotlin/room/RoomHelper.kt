package room

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

expect class RoomHelper {
    fun getDatabasePath(): String
}

fun <T : RoomDatabase> RoomHelper.additionalBuilderProperties(builder: RoomDatabase.Builder<T>) =
    builder
        // .addMigrations(MIGRATIONS)
        .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = false)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
