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
public class VEventBuilder internal constructor(
    initial: IComponentBuilder
) {
    public constructor(): this(IComponentBuilder()) {
        val now = Clock.System.now()
        builderState.propertyWithInstant("DTSTAMP", now.formatUtc(), instant = now)
        builderState.property("UID", Uuid.random().toString())
    }

    private val builderState = initial

    public fun uid(value: String) {
        builderState.property("UID", value)
    }

    public fun summary(value: String) {
        builderState.property("SUMMARY", value)
    }

    public fun description(value: String) {
        builderState.property("DESCRIPTION", value)
    }

    public fun location(value: String) {
        builderState.property("LOCATION", value)
    }

    public fun status(value: EventStatus) {
        builderState.property("STATUS", value.name)
    }

    public fun transp(value: Transparency) {
        builderState.property("TRANSP", value.name)
    }

    public fun sequence(value: Int) {
        builderState.property("SEQUENCE", value.toString())
    }

    public fun organizer(email: String, name: String? = null) {
        val params = if (name != null) mapOf("CN" to listOf(name)) else emptyMap()
        builderState.property("ORGANIZER", "mailto:$email", params)
    }

    public fun dtStart(value: LocalDateTime, timeZone: TimeZone = TimeZone.UTC) {
        builderState.propertyWithInstant("DTSTART", value.formatICalDateTime(), instant = value.toInstant(timeZone))
    }

    public fun dtStart(value: LocalDateTime, tzid: String) {
        val tz = try { TimeZone.of(tzid) } catch (_: Exception) { TimeZone.UTC }
        val instant = value.toInstant(tz)
        builderState.propertyWithInstant("DTSTART", value.formatICalDateTime(), mapOf("TZID" to listOf(tzid)), instant)
    }

    public fun dtStart(value: Instant) {
        builderState.propertyWithInstant("DTSTART", value.formatUtc(), instant = value)
    }

    public fun dtStartDate(value: LocalDate) {
        val midnight = LocalDateTime(value.year, value.month, value.day, 0, 0, 0)
        val instant = midnight.toInstant(TimeZone.UTC)
        builderState.propertyWithInstant("DTSTART", value.formatICalDate(), mapOf("VALUE" to listOf("DATE")), instant)
    }

    public fun dtEnd(value: LocalDateTime, timeZone: TimeZone = TimeZone.UTC) {
        builderState.propertyWithInstant("DTEND", value.formatICalDateTime(), instant = value.toInstant(timeZone))
    }

    public fun dtEnd(value: LocalDateTime, tzid: String) {
        val tz = try { TimeZone.of(tzid) } catch (_: Exception) { TimeZone.UTC }
        val instant = value.toInstant(tz)
        builderState.propertyWithInstant("DTEND", value.formatICalDateTime(), mapOf("TZID" to listOf(tzid)), instant)
    }

    public fun dtEnd(value: Instant) {
        builderState.propertyWithInstant("DTEND", value.formatUtc(), instant = value)
    }

    public fun dtEndDate(value: LocalDate) {
        val midnight = LocalDateTime(value.year, value.month, value.day, 0, 0, 0)
        val instant = midnight.toInstant(TimeZone.UTC)
        builderState.propertyWithInstant("DTEND", value.formatICalDate(), mapOf("VALUE" to listOf("DATE")), instant)
    }

    public fun duration(value: Duration) {
        builderState.property("DURATION", value.toIsoString())
    }

    public fun alarm(alarm: VAlarm) {
        builderState.components.add(alarm)
    }

    public fun alarm(block: VAlarmBuilder.() -> Unit) {
        builderState.components.add(VAlarmBuilder().apply(block).build())
    }

    public fun attendee(email: String, name: String? = null, params: Map<String, List<String>> = emptyMap()) {
        val combinedParams = if (name != null) {
            params + ("CN" to listOf(name))
        } else {
            params
        }
        builderState.properties.add(ICalProperty("ATTENDEE", combinedParams, "mailto:$email"))
    }

    public fun categories(value: String, block: PropertyParamsBuilder.() -> Unit = {}) {
        val params = PropertyParamsBuilder().apply(block).build()
        builderState.properties.add(ICalProperty("CATEGORIES", params, value))
    }

    public fun xProperty(name: String, value: String, parameters: Map<String, List<String>> = emptyMap()) {
        builderState.xProperty(name, value, parameters)
    }

    internal fun build(): VEvent {
        return VEvent(builderState.properties.toList(), builderState.components.toList())
    }
}
