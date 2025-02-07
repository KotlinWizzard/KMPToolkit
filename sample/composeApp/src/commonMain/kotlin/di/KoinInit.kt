package di

import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

object KoinInit {
    fun init(appDeclaration: KoinAppDeclaration = {}): Koin =
        startKoin {
            modules(
                MODULES,
            )
            appDeclaration()
        }.koin


    private val MODULES =
        listOf(
            commonModule(),
        )

}
