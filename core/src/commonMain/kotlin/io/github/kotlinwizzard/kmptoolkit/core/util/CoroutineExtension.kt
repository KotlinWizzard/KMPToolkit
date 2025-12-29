package io.github.kotlinwizzard.kmptoolkit.core.util

import io.github.kotlinwizzard.kmptoolkit.core.extensions.IO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

val Dispatchers.io: CoroutineDispatcher
    get() = Dispatchers.IO
