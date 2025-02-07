package room

import androidx.room.Room

actual class RoomInitializer {
    actual fun getAppDataBase(): AppDatabase {
        val roomHelper = RoomHelper(AppDatabase.DATABASE_NAME)
        return roomHelper
            .additionalBuilderProperties(
                Room
                    .databaseBuilder(
                        name = roomHelper.getDatabasePath(),
                        factory = AppDatabaseConstructor::initialize,
                    ),
            ).build()
    }
}