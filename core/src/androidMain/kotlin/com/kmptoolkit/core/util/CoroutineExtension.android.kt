package com.kmptoolkit.core.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newFixedThreadPoolContext

actual val Dispatchers.io: CoroutineDispatcher
    get() = Dispatchers.IO

