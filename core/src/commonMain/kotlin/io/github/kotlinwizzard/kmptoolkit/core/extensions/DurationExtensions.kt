package io.github.kotlinwizzard.kmptoolkit.core.extensions

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toDateTimePeriod
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun DurationUnit.millis(value: Int) = Duration.convert(
    value.toDouble(),
    this,
    DurationUnit.MILLISECONDS
).toLong()

fun Duration.formattedMinuteSecondsString(): String {
    val dateTimePeriod = this.toDateTimePeriod()
    return LocalDate.today().toLocalDateTime(
        minute = dateTimePeriod.minutes,
        second = dateTimePeriod.seconds
    ).formattedMinuteSecondsString()
}