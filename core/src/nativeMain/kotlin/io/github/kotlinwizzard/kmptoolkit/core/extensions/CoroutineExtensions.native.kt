package io.github.kotlinwizzard.kmptoolkit.core.extensions

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

actual val kotlinx.coroutines.Dispatchers.IO: kotlinx.coroutines.CoroutineDispatcher
    get() = Dispatchers.IO