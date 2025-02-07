package room

import android.content.Context

actual class RoomHelper(
    val appContext: Context,
    val databaseName: String,
) {
    actual fun getDatabasePath(): String = appContext.getDatabasePath(databaseName).absolutePath
}