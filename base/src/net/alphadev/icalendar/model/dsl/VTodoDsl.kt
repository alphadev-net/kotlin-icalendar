package net.alphadev.icalendar.model.dsl

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import net.alphadev.icalendar.model.*
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

public enum class TodoStatus { NEEDS_ACTION, COMPLETED, IN_PROCESS, CANCELLED }
public enum class TodoClass { PUBLIC, PRIVATE, CONFIDENTIAL }

@ICalDsl
public class VTodoBuilder: IComponentBuilder() {

    init {
        val now = Clock.System.now()
        propertyWithInstant("DTSTAMP", now.formatUtc(), instant = now)
        property("UID", Uuid.random().toString())
    }

    public fun uid(value: String) {
        property("UID", value)
    }

    public fun summary(value: String) {
        property("SUMMARY", value)
    }

    public fun description(value: String) {
        property("DESCRIPTION", value)
    }

    public fun status(value: TodoStatus) {
        property("STATUS", value.name)
    }

    public fun classType(value: TodoClass) {
        property("CLASS", value.name)
    }

    public fun priority(value: Int) {
        property("PRIORITY", value.toString())
    }

    public fun percentComplete(value: Int) {
        property("PERCENT-COMPLETE", value.toString())
    }

    public fun categories(vararg values: String) {
        property("CATEGORIES", values.joinToString(","))
    }

    public fun dtStart(value: LocalDateTime, timeZone: TimeZone = TimeZone.UTC) {
        propertyWithInstant("DTSTART", value.formatICalDateTime(), instant = value.toInstant(timeZone))
    }

    public fun dtStart(value: LocalDateTime, tzid: String) {
        val tz = try { TimeZone.of(tzid) } catch (_: Exception) { TimeZone.UTC }
        val instant = value.toInstant(tz)
        propertyWithInstant("DTSTART", value.formatICalDateTime(), mapOf("TZID" to listOf(tzid)), instant)
    }

    public fun dtStart(value: Instant) {
        propertyWithInstant("DTSTART", value.formatUtc(), instant = value)
    }

    public fun dtDue(value: LocalDateTime, timeZone: TimeZone = TimeZone.UTC) {
        propertyWithInstant("DUE", value.formatICalDateTime(), instant = value.toInstant(timeZone))
    }

    public fun dtDue(value: LocalDateTime, tzid: String) {
        val tz = try { TimeZone.of(tzid) } catch (_: Exception) { TimeZone.UTC }
        val instant = value.toInstant(tz)
        propertyWithInstant("DUE", value.formatICalDateTime(), mapOf("TZID" to listOf(tzid)), instant)
    }

    public fun dtDue(value: Instant) {
        propertyWithInstant("DUE", value.formatUtc(), instant = value)
    }

    public fun dtCompleted(value: LocalDateTime, timeZone: TimeZone = TimeZone.UTC) {
        propertyWithInstant("COMPLETED", value.formatICalDateTime(), instant = value.toInstant(timeZone))
    }

    public fun dtCompleted(value: Instant) {
        propertyWithInstant("COMPLETED", value.formatUtc(), instant = value)
    }

    public fun attendee(email: String, name: String? = null, params: Map<String, List<String>> = emptyMap()) {
        val combinedParams = if (name != null) params + ("CN" to listOf(name)) else params
        property("ATTENDEE", "mailto:$email", combinedParams)
    }

    public fun attach(uri: String) {
        property("ATTACH", uri)
    }

    public fun alarm(block: VAlarmBuilder.() -> Unit) {
        components.add(VAlarmBuilder().apply(block).build())
    }

    public fun build(): VTodo {
        return VTodo(properties.toList(), components.toList())
    }
}
