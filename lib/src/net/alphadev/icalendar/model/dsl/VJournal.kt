package net.alphadev.icalendar.model.dsl

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import net.alphadev.icalendar.model.*
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

enum class JournalStatus { DRAFT, FINAL, CANCELLED }
enum class JournalClass { PUBLIC, PRIVATE, CONFIDENTIAL }

@ICalDsl
class VJournalBuilder {
    private val properties = mutableListOf<ICalProperty>()
    private val components = mutableListOf<ICalComponent>()

    init {
        val now = Clock.System.now()
        propertyWithInstant("DTSTAMP", now.formatUtc(), instant = now)
        property("UID", Uuid.random().toString())
    }

    fun uid(value: String) = property("UID", value)
    fun summary(value: String) = property("SUMMARY", value)
    fun description(value: String) = property("DESCRIPTION", value)
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
        val midnight = LocalDateTime(value.year, value.month, value.day, 0, 0)
        val instant = midnight.toInstant(TimeZone.UTC)
        propertyWithInstant("DTSTART", iCalDateFormat.format(value), mapOf("VALUE" to listOf("DATE")), instant)
    }

    fun status(value: JournalStatus) = property("STATUS", value.name)

    fun classType(value: JournalClass) = property("CLASS", value.name)

    fun attendee(email: String, name: String? = null, params: Map<String, List<String>> = emptyMap()) {
        val combinedParams = if (name != null) params + ("CN" to listOf(name)) else params
        property("ATTENDEE", "mailto:$email", combinedParams)
    }

    fun attach(uri: String) = property("ATTACH", uri)

    fun xProperty(name: String, value: String, parameters: Map<String, List<String>> = emptyMap()) {
        require(name.startsWith("X-", ignoreCase = true)) { "Extension properties must start with X-" }
        property(name, value, parameters)
    }

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

    fun build(): VJournal = VJournal(properties.toList(), components.toList())
}
