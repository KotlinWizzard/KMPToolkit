package util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers


expect val Dispatchers.io: CoroutineDispatcher
