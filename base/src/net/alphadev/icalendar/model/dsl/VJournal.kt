package net.alphadev.icalendar.model.dsl

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import net.alphadev.icalendar.model.*
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

public enum class JournalStatus { DRAFT, FINAL, CANCELLED }
public enum class JournalClass { PUBLIC, PRIVATE, CONFIDENTIAL }

@ICalDsl
public class VJournalBuilder {

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

    fun dtStartDate(value: LocalDate) {
        val midnight = LocalDateTime(value.year, value.month, value.day, 0, 0)
        val instant = midnight.toInstant(TimeZone.UTC)
        builderState.propertyWithInstant("DTSTART", value.formatICalDate(), mapOf("VALUE" to listOf("DATE")), instant)
    }

    public fun status(value: JournalStatus) {
        builderState.property("STATUS", value.name)
    }

    public fun classType(value: JournalClass) {
        builderState.property("CLASS", value.name)
    }

    public fun attendee(email: String, name: String? = null, params: Map<String, List<String>> = emptyMap()) {
        val combinedParams = if (name != null) params + ("CN" to listOf(name)) else params
        builderState.property("ATTENDEE", "mailto:$email", combinedParams)
    }

    public fun attach(uri: String) {
        builderState.property("ATTACH", uri)
    }

    public fun xProperty(name: String, value: String, parameters: Map<String, List<String>> = emptyMap()) {
        builderState.xProperty(name, value, parameters)
    }

    internal fun build(): VJournal {
        return VJournal(builderState.properties.toList(), builderState.components.toList())
    }
}
