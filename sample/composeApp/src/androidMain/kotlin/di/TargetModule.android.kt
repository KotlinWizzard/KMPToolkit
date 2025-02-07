package di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import room.AppDatabase
import room.RoomHelper
import room.RoomInitializer

actual fun targetModule(): Module = module {
    single<RoomInitializer> {
        RoomInitializer(androidContext())
    }
}