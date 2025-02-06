package io.github.kotlinwizzard.kmptoolkit.core.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual val Dispatchers.io: CoroutineDispatcher
    get() = Dispatchers.IO