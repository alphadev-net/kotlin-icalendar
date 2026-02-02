package net.alphadev.icalendar.dsl

import net.alphadev.icalendar.model.*
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import net.alphadev.icalendar.model.iCalDateFormat
import net.alphadev.icalendar.model.iCalDateTimeFormat
import net.alphadev.icalendar.model.formatUtc
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
        val now = Clock.System.now()
        propertyWithInstant("DTSTAMP", now.formatUtc(), instant = now)
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

    fun dtStart(value: LocalDateTime) {
        val instant = value.toInstant(TimeZone.UTC)
        propertyWithInstant("DTSTART", iCalDateTimeFormat.format(value), instant = instant)
    }

    fun dtStart(value: LocalDateTime, tzid: String) {
        val tz = try { TimeZone.of(tzid) } catch (_: Exception) { TimeZone.UTC }
        val instant = value.toInstant(tz)
        propertyWithInstant("DTSTART", iCalDateTimeFormat.format(value), mapOf("TZID" to listOf(tzid)), instant)
    }

    fun dtStart(value: Instant) {
        propertyWithInstant("DTSTART", value.formatUtc(), instant = value)
    }

    fun dtStartDate(value: LocalDate) {
        val midnight = LocalDateTime(value.year, value.month, value.day, 0, 0, 0)
        val instant = midnight.toInstant(TimeZone.UTC)
        propertyWithInstant("DTSTART", iCalDateFormat.format(value), mapOf("VALUE" to listOf("DATE")), instant)
    }

    fun dtEnd(value: LocalDateTime) {
        val instant = value.toInstant(TimeZone.UTC)
        propertyWithInstant("DTEND", iCalDateTimeFormat.format(value), instant = instant)
    }

    fun dtEnd(value: LocalDateTime, tzid: String) {
        val tz = try { TimeZone.of(tzid) } catch (_: Exception) { TimeZone.UTC }
        val instant = value.toInstant(tz)
        propertyWithInstant("DTEND", iCalDateTimeFormat.format(value), mapOf("TZID" to listOf(tzid)), instant)
    }

    fun dtEnd(value: Instant) {
        propertyWithInstant("DTEND", value.formatUtc(), instant = value)
    }

    fun dtEndDate(value: LocalDate) {
        val midnight = LocalDateTime(value.year, value.month, value.day, 0, 0, 0)
        val instant = midnight.toInstant(TimeZone.UTC)
        propertyWithInstant("DTEND", iCalDateFormat.format(value), mapOf("VALUE" to listOf("DATE")), instant)
    }

    fun duration(value: Duration) = property("DURATION", value.toIsoString())

    fun alarm(block: VAlarmBuilder.() -> Unit) {
        components.add(VAlarmBuilder().apply(block).build())
    }

    fun attendee(email: String, name: String? = null, params: Map<String, List<String>> = emptyMap()) {
        val combinedParams = if (name != null) {
            params + ("CN" to listOf(name))
        } else {
            params
        }
        properties.add(ICalProperty("ATTENDEE", combinedParams, "mailto:$email"))
    }

    fun xProperty(name: String, value: String, parameters: Map<String, List<String>> = emptyMap()) {
        require(name.startsWith("X-", ignoreCase = true)) { "Extension properties must start with X-" }
        property(name, value, parameters)
    }

    fun property(name: String, value: String, parameters: Map<String, List<String>> = emptyMap()) {
        properties.removeAll { it.name.equals(name, ignoreCase = true) }
        properties.add(ICalProperty(name.uppercase(), parameters, value))
    }

    private fun propertyWithInstant(
        name: String,
        value: String,
        parameters: Map<String, List<String>> = emptyMap(),
        instant: Instant? = null
    ) {
        properties.removeAll { it.name.equals(name, ignoreCase = true) }
        properties.add(ICalProperty(name.uppercase(), parameters, value, instant))
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
        property("TRIGGER", (-duration).toIsoString())
    }

    fun triggerAfter(duration: Duration) {
        property("TRIGGER", duration.toIsoString())
    }

    fun triggerBeforeEnd(duration: Duration) {
        property("TRIGGER", (-duration).toIsoString(), mapOf("RELATED" to listOf("END")))
    }

    fun triggerAfterEnd(duration: Duration) {
        property("TRIGGER", duration.toIsoString(), mapOf("RELATED" to listOf("END")))
    }

    fun triggerAt(instant: Instant) {
        properties.removeAll { it.name.equals("TRIGGER", ignoreCase = true) }
        properties.add(ICalProperty("TRIGGER", mapOf("VALUE" to listOf("DATE-TIME")), instant.formatUtc(), instant))
    }

    fun description(value: String) = property("DESCRIPTION", value)
    fun summary(value: String) = property("SUMMARY", value)

    fun attendee(email: String) {
        properties.add(ICalProperty("ATTENDEE", emptyMap(), "mailto:$email"))
    }

    fun attach(uri: String) = property("ATTACH", uri)

    fun repeat(count: Int, interval: Duration) {
        property("REPEAT", count.toString())
        property("DURATION", interval.toIsoString())
    }

    fun property(name: String, value: String, parameters: Map<String, List<String>> = emptyMap()) {
        properties.removeAll { it.name.equals(name, ignoreCase = true) }
        properties.add(ICalProperty(name.uppercase(), parameters, value))
    }

    fun build(): VAlarm = VAlarm(properties.toList())
}

enum class EventStatus { TENTATIVE, CONFIRMED, CANCELLED }
enum class Transparency { OPAQUE, TRANSPARENT }
