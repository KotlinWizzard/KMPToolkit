package io.github.kotlinwizzard.kmptoolkit.core.extensions

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char


fun LocalDate.simpleFormattedDateString(): String {
    val dateFormat =
        LocalDate.Format {
            dayOfMonth(padding = Padding.ZERO)
            char('.')
            monthNumber(padding = Padding.ZERO)
            char('.')
            year()
        }
    return this.format(dateFormat)
}


fun LocalDateTime.formattedHourMinuteSecondsString(): String {
    val format =
        LocalDateTime.Format {
            hour(padding = Padding.ZERO)
            char(':')
            minute()
            char(':')
            second()
        }
    return this.format(format)
}

fun LocalDateTime.formattedHourMinuteString(): String {
    val format =
        LocalDateTime.Format {
            hour(padding = Padding.ZERO)
            char(':')
            minute()
        }
    return this.format(format)
}

fun LocalDateTime.formattedMinuteSecondsString(): String {
    val format =
        LocalDateTime.Format {
            minute(padding = Padding.ZERO)
            char(':')
            second()
        }
    return this.format(format)
}