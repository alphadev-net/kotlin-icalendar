package net.alphadev.icalendar.model

import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char

data class VEvent(
    override val properties: List<ICalProperty>,
    override val components: List<ICalComponent> = emptyList()
) : ICalComponent {
    override val componentName: String = NAME
    companion object { const val NAME = "VEVENT" }
}

val VEvent.uid: String?
    get() = properties.firstOrNull { it.name == "UID" }?.value

val VEvent.summary: String?
    get() = properties.firstOrNull { it.name == "SUMMARY" }?.value

val VEvent.description: String?
    get() = properties.firstOrNull { it.name == "DESCRIPTION" }?.value

val VEvent.location: String?
    get() = properties.firstOrNull { it.name == "LOCATION" }?.value

val VEvent.sequence: Int?
    get() = properties.firstOrNull { it.name == "SEQUENCE" }?.value?.toIntOrNull()

val VEvent.status: String?
    get() = properties.firstOrNull { it.name == "STATUS" }?.value

val VEvent.transp: String?
    get() = properties.firstOrNull { it.name == "TRANSP" }?.value

val VEvent.organizer: String?
    get() = properties.firstOrNull { it.name == "ORGANIZER" }?.value

val VEvent.dtStartProperty: ICalProperty?
    get() = properties.firstOrNull { it.name == "DTSTART" }

val VEvent.dtEndProperty: ICalProperty?
    get() = properties.firstOrNull { it.name == "DTEND" }

val VEvent.dtStart: Instant?
    get() = dtStartProperty?.toInstant()

val VEvent.dtEnd: Instant?
    get() = dtEndProperty?.toInstant()

val VEvent.dtStamp: Instant?
    get() = properties.firstOrNull { it.name == "DTSTAMP" }?.toInstant()

val VEvent.created: Instant?
    get() = properties.firstOrNull { it.name == "CREATED" }?.toInstant()

val VEvent.lastModified: Instant?
    get() = properties.firstOrNull { it.name == "LAST-MODIFIED" }?.toInstant()

val VEvent.isAllDay: Boolean
    get() = dtStartProperty?.valueType.equals("DATE", ignoreCase = true)
            || (dtStartProperty?.value?.contains("T") == false)

val VEvent.alarms: List<VAlarm>
    get() = components.filterIsInstance<VAlarm>()

val VEvent.hasAlarms: Boolean
    get() = components.any { it is VAlarm }

fun ICalProperty.toInstant(): Instant? {
    val v = value.trim()
    if (v.isEmpty()) return null

    val zone = tzid?.let { TimeZone.of(it) } ?: TimeZone.UTC

    return when {
        valueType.equals("DATE", ignoreCase = true) || !v.contains("T") -> {
            val date = parseICalDate(v)
            Instant.fromEpochSeconds(date.atStartOfDayIn(zone).epochSeconds)
        }
        v.endsWith("Z") -> {
            val local = parseICalDateTime(v.dropLast(1))
            Instant.fromEpochSeconds(local.toInstant(TimeZone.UTC).epochSeconds)
        }
        else -> {
            val local = parseICalDateTime(v)
            Instant.fromEpochSeconds(local.toInstant(zone).epochSeconds)
        }
    }
}

private val iCalDateFormat = LocalDate.Format {
    year(Padding.ZERO)
    monthNumber(Padding.ZERO)
    day(Padding.ZERO)
}

private val iCalDateTimeFormat = LocalDateTime.Format {
    year(Padding.ZERO)
    monthNumber(Padding.ZERO)
    day(Padding.ZERO)
    char('T')
    hour(Padding.ZERO)
    minute(Padding.ZERO)
    second(Padding.ZERO)
}

private fun parseICalDate(value: String): LocalDate = LocalDate.parse(value, iCalDateFormat)

private fun parseICalDateTime(value: String): LocalDateTime = LocalDateTime.parse(value, iCalDateTimeFormat)
