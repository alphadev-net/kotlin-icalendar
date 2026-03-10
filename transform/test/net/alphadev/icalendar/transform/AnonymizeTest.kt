package net.alphadev.icalendar.transform

import net.alphadev.icalendar.model.dsl.VEventBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

class AnonymizeTest {

    private fun basicEvent() = VEventBuilder().apply {
        uid("test-uid")
        dtStart(Instant.parse("2024-01-01T10:00:00Z"))
        dtEnd(Instant.parse("2024-01-01T11:00:00Z"))
    }

    @Test
    fun stripsSummary() {
        val event = basicEvent().apply { summary("Team Meeting") }.build()
        val result = event.anonymize()
        assertFalse(result.properties.any { it.name == "SUMMARY" })
    }

    @Test
    fun stripsDescription() {
        val event = basicEvent().apply { description("Secret details") }.build()
        val result = event.anonymize()
        assertFalse(result.properties.any { it.name == "DESCRIPTION" })
    }

    @Test
    fun stripsLocation() {
        val event = basicEvent().apply { location("123 Main St") }.build()
        val result = event.anonymize()
        assertFalse(result.properties.any { it.name == "LOCATION" })
    }

    @Test
    fun stripsOrganizer() {
        val event = basicEvent().apply { organizer("boss@example.com", "The Boss") }.build()
        val result = event.anonymize()
        assertFalse(result.properties.any { it.name == "ORGANIZER" })
    }

    @Test
    fun stripsAllAttendees() {
        val event = basicEvent().apply {
            attendee("alice@example.com", "Alice")
            attendee("bob@example.com", "Bob")
        }.build()
        val result = event.anonymize()
        assertFalse(result.properties.any { it.name == "ATTENDEE" })
    }

    @Test
    fun retainsSafeProperties() {
        val event = basicEvent().apply { summary("Should be removed") }.build()
        val result = event.anonymize()
        assertFalse(result.properties.any { it.name == "SUMMARY" })
        assertTrue(result.properties.any { it.name == "UID" })
        assertTrue(result.properties.any { it.name == "DTSTAMP" })
        assertTrue(result.properties.any { it.name == "DTSTART" })
        assertTrue(result.properties.any { it.name == "DTEND" })
    }

    @Test
    fun eventWithoutSensitiveDataIsUnchanged() {
        val event = basicEvent().build()
        val result = event.anonymize()
        assertEquals(event.properties, result.properties)
    }

    @Test
    fun preservesComponents() {
        val event = basicEvent().apply { alarm { } }.build()
        val result = event.anonymize()
        assertEquals(event.components, result.components)
    }

    @Test
    fun customFilterOverridesDefault() {
        val event = basicEvent().apply {
            summary("Keep me")
            location("Remove me")
        }.build()
        val result = event.anonymize(filter = setOf("LOCATION"))
        assertFalse(result.properties.any { it.name == "LOCATION" })
        assertTrue(result.properties.any { it.name == "SUMMARY" })
    }

    @Test
    fun emptyFilterRetainsAllProperties() {
        val event = basicEvent().apply { summary("Keep me") }.build()
        val result = event.anonymize(filter = emptySet())
        assertEquals(event.properties, result.properties)
    }

    @Test
    fun builderFilterSelectivelyStrips() {
        val event = basicEvent().apply {
            summary("Remove me")
            location("Keep me")
        }.build()
        val result = event.anonymize { summary() }
        assertFalse(result.properties.any { it.name == "SUMMARY" })
        assertTrue(result.properties.any { it.name == "LOCATION" })
    }

    @Test
    fun builderFilterEmpty() {
        val event = basicEvent().apply { summary("Keep me") }.build()
        val result = event.anonymize { }
        assertTrue(result.properties.any { it.name == "SUMMARY" })
    }

    @Test
    fun builderPrivacyRedactedMatchesDefault() {
        val event = basicEvent().apply {
            summary("Remove me")
            description("Remove me")
            location("Remove me")
            organizer("boss@example.com")
            attendee("alice@example.com")
        }.build()
        val result = event.anonymize { privacyRedacted() }
        assertEquals(event.anonymize(), result)
    }
}
