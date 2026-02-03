package net.alphadev.icalendar.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlin.time.Instant

val iCalDateFormat = LocalDate.Format {
    year(Padding.ZERO)
    monthNumber(Padding.ZERO)
    day(Padding.ZERO)
}

val iCalDateTimeFormat = LocalDateTime.Format {
    year(Padding.ZERO)
    monthNumber(Padding.ZERO)
    day(Padding.ZERO)
    char('T')
    hour(Padding.ZERO)
    minute(Padding.ZERO)
    second(Padding.ZERO)
}

internal fun parseICalDate(value: String): LocalDate? {
    return try {
        iCalDateFormat.parse(value.trim())
    } catch (_: Exception) {
        null
    }
}

internal fun parseICalDateTime(value: String): LocalDateTime? {
    return try {
        val v = value.trim().removeSuffix("Z")
        iCalDateTimeFormat.parse(v)
    } catch (_: Exception) {
        null
    }
}

fun Instant.formatUtc(): String =
    iCalDateTimeFormat.format(toLocalDateTime(TimeZone.UTC)) + "Z"
