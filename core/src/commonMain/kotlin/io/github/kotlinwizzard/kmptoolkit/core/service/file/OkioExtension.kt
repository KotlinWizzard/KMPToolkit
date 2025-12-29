package io.github.kotlinwizzard.kmptoolkit.core.service.file

import okio.FileSystem
import kotlin.time.DurationUnit
import kotlin.time.toDuration

expect val FileSystem.Companion.SYSTEM: FileSystem


fun FileSystem.updateMaxCacheDurationMillisForMemoryCacheFilesystem(durationMillis: Long) {
    if (this is OkioMemoryFileSystem) {
        this.maxCacheDurationMillis = durationMillis
    }
}

fun FileSystem.updateMaxCacheBytesForMemoryCacheFilesystem(maxBytes: Long) {
    if (this is OkioMemoryFileSystem) {
        this.maxBytes = maxBytes
    }
}

fun FileSystem.updateMaxCacheDurationMinutesForMemoryCacheFilesystem(durationMinutes: Int) {
    updateMaxCacheDurationMillisForMemoryCacheFilesystem(
        durationMinutes.toDuration(DurationUnit.MINUTES).inWholeMilliseconds
    )
}

fun FileSystem.updateMaxCacheBytesMBForMemoryCacheFilesystem(maxBytesMB: Int) {
    updateMaxCacheBytesForMemoryCacheFilesystem(
        maxBytesMB.toLong() * 1024L * 1024L
    )
}