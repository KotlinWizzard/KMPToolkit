package io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.infrastructure

import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
sealed class CachedRefreshAction {
    data object RefreshNever : io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.infrastructure.CachedRefreshAction()

    data object RefreshAndDelete : io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.infrastructure.CachedRefreshAction()

    data object RefreshAndUpdate : io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.infrastructure.CachedRefreshAction()

    data class RefreshAndUpdateOnTime(val cacheTimeoutMillis: Long) : io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.infrastructure.CachedRefreshAction()

    data class RefreshAndDeleteOnTime(val cacheTimeoutMillis: Long) : io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.cache.infrastructure.CachedRefreshAction()

    companion object {
        fun getTimeInMillisFromUnit(
            value: Long,
            unit: DurationUnit,
        ): Long = Duration.convert(value.toDouble(), unit, DurationUnit.MILLISECONDS).toLong()
    }
}
