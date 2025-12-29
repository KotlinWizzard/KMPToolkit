@file:OptIn(ExperimentalTime::class)
package io.github.kotlinwizzard.kmptoolkit.core.extensions
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
import kotlinx.datetime.toStdlibInstant
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun kotlin.time.Instant.toLocalDateTime(): LocalDateTime =
   toLocalDateTime(TimeZone.currentSystemDefault())

@OptIn(ExperimentalTime::class)
fun LocalDateTime.toInstant(): kotlin.time.Instant = this.toInstant(TimeZone.currentSystemDefault())

fun kotlin.time.Instant.differenceOfDays(
    to: kotlin.time.Instant,
    inclusive: Boolean = false,
): Int {
    val fromDate = this.toLocalDateTime().date
    val toDate = to.toLocalDateTime().date
    return fromDate.daysUntil(toDate, inclusive)
}

@OptIn(ExperimentalTime::class)
fun LocalDateTime.periodUntil(other: LocalDateTime): DateTimePeriod {
    val timeZone = TimeZone.currentSystemDefault()
    return this.toInstant(timeZone).periodUntil(
        other.toInstant(timeZone),
        timeZone,
    )
}

fun LocalDateTime.Companion.now() = kotlin.time.Clock.System.now().toLocalDateTime()


@OptIn(ExperimentalTime::class)
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