package net.alphadev.icalendar.model.dsl

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
class VTodoBuilder: IComponentBuilder() {

    init {
        val now = Clock.System.now()
        propertyWithInstant("DTSTAMP", now.formatUtc(), instant = now)
        property("UID", Uuid.random().toString())
    }

    fun uid(value: String) = property("UID", value)
    fun summary(value: String) = property("SUMMARY", value)
    fun description(value: String) = property("DESCRIPTION", value)
    fun status(value: TodoStatus) = property("STATUS", value.name)
    fun classType(value: TodoClass) = property("CLASS", value.name)
    fun priority(value: Int) = property("PRIORITY", value.toString())
    fun percentComplete(value: Int) = property("PERCENT-COMPLETE", value.toString())
    fun categories(vararg values: String) = property("CATEGORIES", values.joinToString(","))

    fun dtStart(value: LocalDateTime, timeZone: TimeZone = TimeZone.UTC) {
        propertyWithInstant("DTSTART", value.formatICalDateTime(), instant = value.toInstant(timeZone))
    }

    fun dtStart(value: LocalDateTime, tzid: String) {
        val tz = try { TimeZone.of(tzid) } catch (_: Exception) { TimeZone.UTC }
        val instant = value.toInstant(tz)
        propertyWithInstant("DTSTART", value.formatICalDateTime(), mapOf("TZID" to listOf(tzid)), instant)
    }

    fun dtStart(value: Instant) = propertyWithInstant("DTSTART", value.formatUtc(), instant = value)

    fun dtDue(value: LocalDateTime, timeZone: TimeZone = TimeZone.UTC) {
        propertyWithInstant("DUE", value.formatICalDateTime(), instant = value.toInstant(timeZone))
    }

    fun dtDue(value: LocalDateTime, tzid: String) {
        val tz = try { TimeZone.of(tzid) } catch (_: Exception) { TimeZone.UTC }
        val instant = value.toInstant(tz)
        propertyWithInstant("DUE", value.formatICalDateTime(), mapOf("TZID" to listOf(tzid)), instant)
    }

    fun dtDue(value: Instant) = propertyWithInstant("DUE", value.formatUtc(), instant = value)

    fun dtCompleted(value: LocalDateTime, timeZone: TimeZone = TimeZone.UTC) {
        propertyWithInstant("COMPLETED", value.formatICalDateTime(), instant = value.toInstant(timeZone))
    }

    fun dtCompleted(value: Instant) = propertyWithInstant("COMPLETED", value.formatUtc(), instant = value)

    fun attendee(email: String, name: String? = null, params: Map<String, List<String>> = emptyMap()) {
        val combinedParams = if (name != null) params + ("CN" to listOf(name)) else params
        property("ATTENDEE", "mailto:$email", combinedParams)
    }

    fun attach(uri: String) = property("ATTACH", uri)

    fun alarm(block: VAlarmBuilder.() -> Unit) {
        components.add(VAlarmBuilder().apply(block).build())
    }

    fun build(): VTodo = VTodo(properties.toList(), components.toList())
}
