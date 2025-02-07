package room

import android.content.Context
import androidx.room.Room

actual class RoomInitializer(
    val context: Context,
) {
    actual fun getAppDataBase(): AppDatabase {
        val roomHelper = RoomHelper(context, AppDatabase.DATABASE_NAME)
        return roomHelper
            .additionalBuilderProperties(
                Room
                    .databaseBuilder(
                        name = roomHelper.getDatabasePath(),
                        factory = AppDatabaseConstructor::initialize,
                        context = context,
                    ),
            ).build()
    }
}