package io.github.kotlinwizzard.kmptoolkit.core.extensions

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.periodUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

fun Instant.toLocalDateTime(): LocalDateTime = this.toLocalDateTime(TimeZone.currentSystemDefault())

fun LocalDateTime.toInstant(): Instant = this.toInstant(TimeZone.currentSystemDefault())

fun Instant.differenceOfDays(
    to: Instant,
    inclusive: Boolean = false,
): Int {
    val fromDate = this.toLocalDateTime().date
    val toDate = to.toLocalDateTime().date
    return fromDate.daysUntil(toDate, inclusive)
}

fun LocalDateTime.periodUntil(other: LocalDateTime): DateTimePeriod {
    val timeZone = TimeZone.currentSystemDefault()
    return this.toInstant(timeZone).periodUntil(
        other.toInstant(timeZone),
        timeZone,
    )
}

fun LocalDateTime.Companion.now() = Clock.System.now().toLocalDateTime()


fun LocalDateTime.plusMinutes(minutes: Int) =
    this
        .toInstant()
        .plus(minutes, DateTimeUnit.MINUTE)
        .toLocalDateTime()

fun LocalDateTime.minusMinutes(minutes: Int) =
    this
        .toInstant()
        .minus(minutes, DateTimeUnit.MINUTE)
        .toLocalDateTime()

fun LocalDateTime.plusDays(days: Int) =
    this
        .toInstant()
        .plus(days, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
        .toLocalDateTime()

fun LocalDateTime.minusDays(days: Int) =
    this
        .toInstant()
        .minus(days, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
        .toLocalDateTime()

fun LocalDateTime.plusMonths(month: Int) =
    this
        .toInstant()
        .plus(month, DateTimeUnit.MONTH, TimeZone.currentSystemDefault())
        .toLocalDateTime()

fun LocalDateTime.minusMonths(month: Int) =
    this
        .toInstant()
        .minus(month, DateTimeUnit.MONTH, TimeZone.currentSystemDefault())
        .toLocalDateTime()

fun LocalDateTime.plusYears(years: Int) =
    this
        .toInstant()
        .plus(years, DateTimeUnit.YEAR, TimeZone.currentSystemDefault())
        .toLocalDateTime()

fun LocalDateTime.minusYears(years: Int) =
    this
        .toInstant()
        .minus(years, DateTimeUnit.YEAR, TimeZone.currentSystemDefault())
        .toLocalDateTime()


fun LocalDateTime.secondsUntil(other: LocalDateTime) =
    other.toInstant().epochSeconds - this.toInstant().epochSeconds