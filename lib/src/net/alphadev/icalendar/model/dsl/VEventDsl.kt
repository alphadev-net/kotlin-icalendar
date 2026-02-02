package net.alphadev.icalendar.model.dsl

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import net.alphadev.icalendar.model.*
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant
import kotlin.uuid.Uuid

@ICalDsl
class VEventBuilder: IComponentBuilder() {

    init {
        val now = Clock.System.now()
        propertyWithInstant("DTSTAMP", now.formatUtc(), instant = now)
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

    fun dtStart(value: LocalDateTime, timeZone: TimeZone = TimeZone.UTC) {
        propertyWithInstant("DTSTART", iCalDateTimeFormat.format(value), instant = value.toInstant(timeZone))
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

    fun dtEnd(value: LocalDateTime, timeZone: TimeZone = TimeZone.UTC) {
        propertyWithInstant("DTEND", iCalDateTimeFormat.format(value), instant = value.toInstant(timeZone))
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

    fun build(): VEvent = VEvent(properties.toList(), components.toList())
}
