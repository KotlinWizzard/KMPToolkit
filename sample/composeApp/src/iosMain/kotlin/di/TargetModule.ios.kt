package di

import org.koin.core.module.Module
import org.koin.dsl.module
import room.RoomInitializer

actual fun targetModule(): Module = module {
    single<RoomInitializer> {
        RoomInitializer()
    }
}