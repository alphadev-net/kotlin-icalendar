package net.alphadev.icalendar.model

import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant

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

val VEvent.dtStart: ICalTemporal?
    get() = dtStartProperty?.toICalTemporal()

val VEvent.dtEnd: ICalTemporal?
    get() = dtEndProperty?.toICalTemporal()

val VEvent.dtStamp: Instant?
    get() = properties.firstOrNull { it.name == "DTSTAMP" }?.toICalTemporal()?.toInstant()

val VEvent.created: Instant?
    get() = properties.firstOrNull { it.name == "CREATED" }?.toICalTemporal()?.toInstant()

val VEvent.lastModified: Instant?
    get() = properties.firstOrNull { it.name == "LAST-MODIFIED" }?.toICalTemporal()?.toInstant()

val VEvent.isAllDay: Boolean
    get() = dtStart is ICalTemporal.Date

val VEvent.alarms: List<VAlarm>
    get() = components.filterIsInstance<VAlarm>()

val VEvent.hasAlarms: Boolean
    get() = components.any { it is VAlarm }

sealed interface ICalTemporal {
    data class Date(val date: LocalDate) : ICalTemporal
    data class DateTime(val dateTime: LocalDateTime, val tzid: String?) : ICalTemporal
    data class DateTimeUtc(val instant: Instant) : ICalTemporal

    fun toInstant(defaultZone: TimeZone = TimeZone.currentSystemDefault()): Instant = when (this) {
        is Date -> Instant.fromEpochSeconds(date.atStartOfDayIn(defaultZone).epochSeconds)
        is DateTime -> {
            val zone = tzid?.let { TimeZone.of(it) } ?: defaultZone
            Instant.fromEpochSeconds(dateTime.toInstant(zone).epochSeconds)
        }
        is DateTimeUtc -> instant
    }
}

fun ICalProperty.toICalTemporal(): ICalTemporal? {
    val v = value.trim()
    if (v.isEmpty()) return null

    return when {
        valueType.equals("DATE", ignoreCase = true) -> {
            ICalTemporal.Date(parseICalDate(v))
        }
        v.endsWith("Z") -> {
            val local = parseICalDateTime(v.dropLast(1))
            val epochSeconds = local.toInstant(TimeZone.UTC).epochSeconds
            ICalTemporal.DateTimeUtc(Instant.fromEpochSeconds(epochSeconds))
        }
        v.contains("T") -> {
            val local = parseICalDateTime(v)
            ICalTemporal.DateTime(local, tzid)
        }
        else -> {
            ICalTemporal.Date(parseICalDate(v))
        }
    }
}

private fun parseICalDate(value: String): LocalDate {
    require(value.length == 8) { "Invalid iCal date: $value" }
    val year = value.substring(0, 4).toInt()
    val month = value.substring(4, 6).toInt()
    val day = value.substring(6, 8).toInt()
    return LocalDate(year, month, day)
}

private fun parseICalDateTime(value: String): LocalDateTime {
    require(value.length == 15 && value[8] == 'T') { "Invalid iCal datetime: $value" }
    val year = value.substring(0, 4).toInt()
    val month = value.substring(4, 6).toInt()
    val day = value.substring(6, 8).toInt()
    val hour = value.substring(9, 11).toInt()
    val minute = value.substring(11, 13).toInt()
    val second = value.substring(13, 15).toInt()
    return LocalDateTime(year, month, day, hour, minute, second)
}
