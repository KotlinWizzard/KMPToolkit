package di

import org.koin.dsl.module
import room.AppDatabase
import room.RoomInitializer
import kotlin.math.sin

fun commonModule() =
    module {
        single<AppDatabase> {
            val roomInitializer: RoomInitializer = get()
            roomInitializer.getAppDataBase()
        }
    }