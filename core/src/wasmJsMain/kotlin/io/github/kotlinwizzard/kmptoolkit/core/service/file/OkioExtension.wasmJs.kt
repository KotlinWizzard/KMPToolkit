package io.github.kotlinwizzard.kmptoolkit.core.service.file

import okio.FileSystem

private val memoryFileSystem = OkioMemoryFileSystem.Create(maxCacheDurationMinutes = 15, maxBytesMB = 512)
actual val FileSystem.Companion.SYSTEM: FileSystem
    get() =  memoryFileSystem