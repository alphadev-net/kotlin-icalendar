package net.alphadev.icalendar.model.dsl

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import net.alphadev.icalendar.model.*
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

enum class TodoStatus { NEEDS_ACTION, COMPLETED, IN_PROCESS, CANCELLED }
enum class TodoClass { PUBLIC, PRIVATE, CONFIDENTIAL }

@ICalDsl
class VTodoBuilder {
    private val properties = mutableListOf<ICalProperty>()
    private val components = mutableListOf<ICalComponent>()

    init {
        val now = Clock.System.now()
        propertyWithInstant("DTSTAMP", now.formatUtc(), instant = now)
        property("UID", Uuid.random().toString())
    }

    // Basic properties
    fun uid(value: String) = property("UID", value)
    fun summary(value: String) = property("SUMMARY", value)
    fun description(value: String) = property("DESCRIPTION", value)
    fun status(value: TodoStatus) = property("STATUS", value.name)
    fun classType(value: TodoClass) = property("CLASS", value.name)
    fun priority(value: Int) = property("PRIORITY", value.toString())
    fun percentComplete(value: Int) = property("PERCENT-COMPLETE", value.toString())
    fun categories(vararg values: String) = property("CATEGORIES", values.joinToString(","))

    // Date/time properties
    fun dtStart(value: LocalDateTime) {
        val instant = value.toInstant(TimeZone.UTC)
        propertyWithInstant("DTSTART", iCalDateTimeFormat.format(value), instant = instant)
    }

    fun dtStart(value: LocalDateTime, tzid: String) {
        val tz = try { TimeZone.of(tzid) } catch (_: Exception) { TimeZone.UTC }
        val instant = value.toInstant(tz)
        propertyWithInstant("DTSTART", iCalDateTimeFormat.format(value), mapOf("TZID" to listOf(tzid)), instant)
    }

    fun dtStart(value: Instant) = propertyWithInstant("DTSTART", value.formatUtc(), instant = value)

    fun dtDue(value: LocalDateTime) {
        val instant = value.toInstant(TimeZone.UTC)
        propertyWithInstant("DUE", iCalDateTimeFormat.format(value), instant = instant)
    }

    fun dtDue(value: LocalDateTime, tzid: String) {
        val tz = try { TimeZone.of(tzid) } catch (_: Exception) { TimeZone.UTC }
        val instant = value.toInstant(tz)
        propertyWithInstant("DUE", iCalDateTimeFormat.format(value), mapOf("TZID" to listOf(tzid)), instant)
    }

    fun dtDue(value: Instant) = propertyWithInstant("DUE", value.formatUtc(), instant = value)

    fun dtCompleted(value: LocalDateTime) {
        val instant = value.toInstant(TimeZone.UTC)
        propertyWithInstant("COMPLETED", iCalDateTimeFormat.format(value), instant = instant)
    }

    fun dtCompleted(value: Instant) = propertyWithInstant("COMPLETED", value.formatUtc(), instant = value)

    // Attendees
    fun attendee(email: String, name: String? = null, params: Map<String, List<String>> = emptyMap()) {
        val combinedParams = if (name != null) params + ("CN" to listOf(name)) else params
        property("ATTENDEE", "mailto:$email", combinedParams)
    }

    // Attachments
    fun attach(uri: String) = property("ATTACH", uri)

    // Alarms
    fun alarm(block: VAlarmBuilder.() -> Unit) {
        components.add(VAlarmBuilder().apply(block).build())
    }

    // Extension properties
    fun xProperty(name: String, value: String, parameters: Map<String, List<String>> = emptyMap()) {
        require(name.startsWith("X-", ignoreCase = true)) { "Extension properties must start with X-" }
        property(name, value, parameters)
    }

    // Property helpers
    private fun property(name: String, value: String, parameters: Map<String, List<String>> = emptyMap()) {
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

    fun build(): VTodo = VTodo(properties.toList(), components.toList())
}
