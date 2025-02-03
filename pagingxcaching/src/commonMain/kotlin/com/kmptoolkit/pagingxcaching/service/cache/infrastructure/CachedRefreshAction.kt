package com.kmptoolkit.pagingxcaching.service.cache.infrastructure

import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
sealed class CachedRefreshAction {
    data object RefreshNever : CachedRefreshAction()

    data object RefreshAndDelete : CachedRefreshAction()

    data object RefreshAndUpdate : CachedRefreshAction()

    data class RefreshAndUpdateOnTime(val cacheTimeoutMillis: Long) : CachedRefreshAction()

    data class RefreshAndDeleteOnTime(val cacheTimeoutMillis: Long) : CachedRefreshAction()

    companion object {
        fun getTimeInMillisFromUnit(
            value: Long,
            unit: DurationUnit,
        ): Long = Duration.convert(value.toDouble(), unit, DurationUnit.MILLISECONDS).toLong()
    }
}
