package io.github.kotlinwizzard.kmptoolkit.core.extensions

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Runnable
import kotlin.coroutines.CoroutineContext

actual val kotlinx.coroutines.Dispatchers.IO: kotlinx.coroutines.CoroutineDispatcher
    get() = IoCoroutineDispatcher


internal object IoCoroutineDispatcher : CoroutineDispatcher() {
    private val unlimitedPool = Dispatchers.Default
    private val io = unlimitedPool.limitedParallelism(128)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun limitedParallelism(
        parallelism: Int,
        name: String?,
    ): CoroutineDispatcher {
        return unlimitedPool.limitedParallelism(parallelism, name)
    }

    override fun dispatch(
        context: CoroutineContext,
        block: Runnable,
    ) {
        io.dispatch(context, block)
    }

    @InternalCoroutinesApi
    override fun dispatchYield(
        context: CoroutineContext,
        block: Runnable,
    ) {
        io.dispatchYield(context, block)
    }

    override fun toString(): String = "Dispatchers.IO"
}