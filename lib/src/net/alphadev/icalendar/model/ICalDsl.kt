package net.alphadev.icalendar.dsl

import net.alphadev.icalendar.model.*
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.number
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

fun vCalendar(block: VCalendarBuilder.() -> Unit): VCalendar {
    return VCalendarBuilder().apply(block).build()
}

@DslMarker
annotation class ICalDsl

@ICalDsl
class VCalendarBuilder {
    private val properties = mutableListOf<ICalProperty>()
    private val components = mutableListOf<ICalComponent>()

    init { property("VERSION", "2.0") }

    fun prodId(value: String) = property("PRODID", value)
    fun calScale(value: String) = property("CALSCALE", value)
    fun method(value: String) = property("METHOD", value)

    fun property(name: String, value: String, parameters: Map<String, List<String>> = emptyMap()) {
        properties.removeAll { it.name.equals(name, ignoreCase = true) }
        properties.add(ICalProperty(name, parameters, value))
    }

    fun event(block: VEventBuilder.() -> Unit) {
        components.add(VEventBuilder().apply(block).build())
    }

    fun build(): VCalendar = VCalendar(properties.toList(), components.toList())
}

@ICalDsl
class VEventBuilder @OptIn(ExperimentalUuidApi::class) constructor() {
    private val properties = mutableListOf<ICalProperty>()
    private val components = mutableListOf<ICalComponent>()

    init {
        property("DTSTAMP", Clock.System.now().formatUtc())
        @OptIn(ExperimentalUuidApi::class)
        property("UID", Uuid.random().toString())
    }

    fun uid(value: String) = property("UID", value)
    fun summary(value: String) = property("SUMMARY", value)
    fun description(value: String) = property("DESCRIPTION", value)
    fun location(value: String) = property("LOCATION", value)
    fun status(value: EventStatus) = property("STATUS", value.name)
    fun transp(value: Transparency) = property("TRANSP", value.name)
    fun sequence(value: Int) = property("SEQUENCE", value.toString())

    fun organizer(email: String, name: String? = null) {
        val params = if (name != null) mapOf("CN" to listOf(name)) else emptyMap()
        property("ORGANIZER", "mailto:$email", params)
    }

    fun dtStart(value: LocalDateTime) = property("DTSTART", value.formatIcal())
    fun dtStart(value: LocalDateTime, tzid: String) {
        property("DTSTART", value.formatIcal(), mapOf("TZID" to listOf(tzid)))
    }
    fun dtStart(value: Instant) = property("DTSTART", value.formatUtc())
    fun dtStartDate(value: LocalDate) {
        property("DTSTART", value.formatIcal(), mapOf("VALUE" to listOf("DATE")))
    }

    fun dtEnd(value: LocalDateTime) = property("DTEND", value.formatIcal())
    fun dtEnd(value: LocalDateTime, tzid: String) {
        property("DTEND", value.formatIcal(), mapOf("TZID" to listOf(tzid)))
    }
    fun dtEnd(value: Instant) = property("DTEND", value.formatUtc())
    fun dtEndDate(value: LocalDate) {
        property("DTEND", value.formatIcal(), mapOf("VALUE" to listOf("DATE")))
    }

    fun duration(value: Duration) = property("DURATION", value.toIcalDuration())

    fun alarm(block: VAlarmBuilder.() -> Unit) {
        components.add(VAlarmBuilder().apply(block).build())
    }

    fun xProperty(name: String, value: String, parameters: Map<String, List<String>> = emptyMap()) {
        require(name.startsWith("X-", ignoreCase = true)) { "Extension properties must start with X-" }
        property(name, value, parameters)
    }

    fun property(name: String, value: String, parameters: Map<String, List<String>> = emptyMap()) {
        properties.removeAll { it.name.equals(name, ignoreCase = true) }
        properties.add(ICalProperty(name.uppercase(), parameters, value))
    }

    fun build(): VEvent = VEvent(properties.toList(), components.toList())
}

@ICalDsl
class VAlarmBuilder {
    private val properties = mutableListOf<ICalProperty>()

    fun displayAction() = property("ACTION", "DISPLAY")
    fun audioAction() = property("ACTION", "AUDIO")
    fun emailAction() = property("ACTION", "EMAIL")
    fun action(value: AlarmAction) = property("ACTION", value.name)

    fun triggerBefore(duration: Duration) {
        property("TRIGGER", (-duration).toIcalDuration())
    }
    fun triggerAfter(duration: Duration) {
        property("TRIGGER", duration.toIcalDuration())
    }
    fun triggerBeforeEnd(duration: Duration) {
        property("TRIGGER", (-duration).toIcalDuration(), mapOf("RELATED" to listOf("END")))
    }
    fun triggerAfterEnd(duration: Duration) {
        property("TRIGGER", duration.toIcalDuration(), mapOf("RELATED" to listOf("END")))
    }
    fun triggerAt(instant: Instant) {
        property("TRIGGER", instant.formatUtc(), mapOf("VALUE" to listOf("DATE-TIME")))
    }

    fun description(value: String) = property("DESCRIPTION", value)
    fun summary(value: String) = property("SUMMARY", value)

    fun attendee(email: String) {
        properties.add(ICalProperty("ATTENDEE", emptyMap(), "mailto:$email"))
    }

    fun attach(uri: String) = property("ATTACH", uri)

    fun repeat(count: Int, interval: Duration) {
        property("REPEAT", count.toString())
        property("DURATION", interval.toIcalDuration())
    }

    fun property(name: String, value: String, parameters: Map<String, List<String>> = emptyMap()) {
        properties.removeAll { it.name.equals(name, ignoreCase = true) }
        properties.add(ICalProperty(name.uppercase(), parameters, value))
    }

    fun build(): VAlarm = VAlarm(properties.toList())
}

enum class EventStatus { TENTATIVE, CONFIRMED, CANCELLED }
enum class Transparency { OPAQUE, TRANSPARENT }

private fun LocalDateTime.formatIcal(): String = buildString {
    append(year.toString().padStart(4, '0'))
    append(month.number.toString().padStart(2, '0'))
    append(dayOfMonth.toString().padStart(2, '0'))
    append('T')
    append(hour.toString().padStart(2, '0'))
    append(minute.toString().padStart(2, '0'))
    append(second.toString().padStart(2, '0'))
}

private fun LocalDate.formatIcal(): String = buildString {
    append(year.toString().padStart(4, '0'))
    append(month.number.toString().padStart(2, '0'))
    append(dayOfMonth.toString().padStart(2, '0'))
}

internal fun Instant.formatUtc(): String {
    val dt = toLocalDateTime(TimeZone.UTC)
    return buildString {
        append(dt.year.toString().padStart(4, '0'))
        append(dt.month.number.toString().padStart(2, '0'))
        append(dt.dayOfMonth.toString().padStart(2, '0'))
        append('T')
        append(dt.hour.toString().padStart(2, '0'))
        append(dt.minute.toString().padStart(2, '0'))
        append(dt.second.toString().padStart(2, '0'))
        append('Z')
    }
}

internal fun Duration.toIcalDuration(): String = buildString {
    val totalSeconds = inWholeSeconds
    val isNegative = totalSeconds < 0
    val abs = if (isNegative) -totalSeconds else totalSeconds

    if (isNegative) append('-')
    append('P')

    val days = abs / 86400
    val remainder = abs % 86400
    val hours = remainder / 3600
    val minutes = (remainder % 3600) / 60
    val seconds = remainder % 60

    if (days > 0) append("${days}D")

    if (hours > 0 || minutes > 0 || seconds > 0 || days == 0L) {
        append('T')
        if (hours > 0) append("${hours}H")
        if (minutes > 0) append("${minutes}M")
        if (seconds > 0 || (hours == 0L && minutes == 0L)) append("${seconds}S")
    }
}
