package com.kmptoolkit.core.extensions

import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.daysUntil
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

fun LocalDate.withDayOfMonth(day: Int) =
    LocalDate(
        year = this.year,
        month = this.month,
        dayOfMonth = day,
    )

fun LocalDate.withFirstDayOfMonth() = withDayOfMonth(1)

fun LocalDate.datesWithin(other: LocalDate, exclusive: Boolean = false): List<LocalDate> {
    val startDate = minOf(this, other)
    val endDate = maxOf(this, other)
    val dateList = mutableListOf<LocalDate>()
    var currentDate = startDate
    fun isValid(start: LocalDate, end: LocalDate): Boolean {
        return if (exclusive) {
            start < end
        } else {
            start <= end
        }
    }
    while (isValid(currentDate, endDate)) {
        dateList.add(currentDate)
        currentDate = currentDate.plus(DatePeriod(days = 1))
    }
    return dateList
}


fun LocalDate.plusDays(days: Int) = plus(days, DateTimeUnit.DAY)

fun LocalDate.minusDays(days: Int) = minus(days, DateTimeUnit.DAY)

fun LocalDate.plusMonths(months: Int) = plus(months, DateTimeUnit.MONTH)

fun LocalDate.minusMonths(months: Int) = minus(months, DateTimeUnit.MONTH)

fun LocalDate.plusYears(years: Int) = plus(years, DateTimeUnit.YEAR)

fun LocalDate.minusYears(years: Int) = minus(years, DateTimeUnit.YEAR)

fun LocalDate.daysUntil(
    other: LocalDate,
    inclusive: Boolean = false,
): Int {
    val daysBetween = this.daysUntil(other)
    return if (inclusive) {
        daysBetween + 1
    } else {
        daysBetween
    }
}

fun LocalDate.Companion.today(): LocalDate =
    Clock.System
        .now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date


fun LocalDate.toInstant(hour: Int = 0, minute: Int = 0, second: Int = 0): Instant {
    return this.toLocalDateTime(hour, minute, second).toInstant(TimeZone.currentSystemDefault())
}

fun LocalDate.toLocalDateTime(hour: Int = 0, minute: Int = 0, second: Int = 0): LocalDateTime {
    return this.atTime(hour.coerceIn(0, 23), minute.coerceIn(0, 60), second.coerceIn(0, 60))
}