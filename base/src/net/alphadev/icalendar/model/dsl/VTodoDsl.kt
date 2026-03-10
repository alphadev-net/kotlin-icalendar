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
public class VTodoBuilder {

    private val builderState = IComponentBuilder()

    init {
        val now = Clock.System.now()
        builderState.propertyWithInstant("DTSTAMP", now.formatUtc(), instant = now)
        builderState.property("UID", Uuid.random().toString())
    }

    public fun uid(value: String) {
        builderState.property("UID", value)
    }

    public fun summary(value: String) {
        builderState.property("SUMMARY", value)
    }

    public fun description(value: String) {
        builderState.property("DESCRIPTION", value)
    }

    public fun status(value: TodoStatus) {
        builderState.property("STATUS", value.name)
    }

    public fun classType(value: TodoClass) {
        builderState.property("CLASS", value.name)
    }

    public fun priority(value: Int) {
        builderState.property("PRIORITY", value.toString())
    }

    public fun percentComplete(value: Int) {
        builderState.property("PERCENT-COMPLETE", value.toString())
    }

    public fun categories(vararg values: String) {
        builderState.property("CATEGORIES", values.joinToString(","))
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

    public fun dtDue(value: LocalDateTime, timeZone: TimeZone = TimeZone.UTC) {
        builderState.propertyWithInstant("DUE", value.formatICalDateTime(), instant = value.toInstant(timeZone))
    }

    public fun dtDue(value: LocalDateTime, tzid: String) {
        val tz = try { TimeZone.of(tzid) } catch (_: Exception) { TimeZone.UTC }
        val instant = value.toInstant(tz)
        builderState.propertyWithInstant("DUE", value.formatICalDateTime(), mapOf("TZID" to listOf(tzid)), instant)
    }

    public fun dtDue(value: Instant) {
        builderState.propertyWithInstant("DUE", value.formatUtc(), instant = value)
    }

    public fun dtCompleted(value: LocalDateTime, timeZone: TimeZone = TimeZone.UTC) {
        builderState.propertyWithInstant("COMPLETED", value.formatICalDateTime(), instant = value.toInstant(timeZone))
    }

    public fun dtCompleted(value: Instant) {
        builderState.propertyWithInstant("COMPLETED", value.formatUtc(), instant = value)
    }

    public fun attendee(email: String, name: String? = null, params: Map<String, List<String>> = emptyMap()) {
        val combinedParams = if (name != null) params + ("CN" to listOf(name)) else params
        builderState.property("ATTENDEE", "mailto:$email", combinedParams)
    }

    public fun attach(uri: String) {
        builderState.property("ATTACH", uri)
    }

    public fun alarm(block: VAlarmBuilder.() -> Unit) {
        builderState.components.add(VAlarmBuilder().apply(block).build())
    }

    public fun xProperty(name: String, value: String, parameters: Map<String, List<String>> = emptyMap()) {
        builderState.xProperty(name, value, parameters)
    }

    internal fun build(): VTodo {
        return VTodo(builderState.properties.toList(), builderState.components.toList())
    }
}
