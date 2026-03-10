package net.alphadev.icalendar.transform

import net.alphadev.icalendar.model.dsl.builder
import net.alphadev.icalendar.model.dsl.vCalendar
import net.alphadev.icalendar.model.events
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

class AnonymizeTest {

    private fun basicEvent() = vCalendar {
        event {
            uid("test-uid")
            dtStart(Instant.parse("2024-01-01T10:00:00Z"))
            dtEnd(Instant.parse("2024-01-01T11:00:00Z"))
        }
    }.events.first()

    @Test
    fun stripsSummary() {
        val event = basicEvent().builder { it.summary("Team Meeting") }
        val result = event.anonymize()
        assertFalse(result.properties.any { it.name == "SUMMARY" })
    }

    @Test
    fun stripsDescription() {
        val event = basicEvent().builder { it.description("Secret details") }
        val result = event.anonymize()
        assertFalse(result.properties.any { it.name == "DESCRIPTION" })
    }

    @Test
    fun stripsLocation() {
        val event = basicEvent().builder { it.location("123 Main St") }
        val result = event.anonymize()
        assertFalse(result.properties.any { it.name == "LOCATION" })
    }

    @Test
    fun stripsOrganizer() {
        val event = basicEvent().builder { it.organizer("boss@example.com", "The Boss") }
        val result = event.anonymize()
        assertFalse(result.properties.any { it.name == "ORGANIZER" })
    }

    @Test
    fun stripsAllAttendees() {
        val event = basicEvent().builder {
            it.attendee("alice@example.com", "Alice")
            it.attendee("bob@example.com", "Bob")
        }
        val result = event.anonymize()
        assertFalse(result.properties.any { it.name == "ATTENDEE" })
    }

    @Test
    fun retainsSafeProperties() {
        val event = basicEvent().builder { it.summary("Should be removed") }
        val result = event.anonymize()
        assertFalse(result.properties.any { it.name == "SUMMARY" })
        assertTrue(result.properties.any { it.name == "UID" })
        assertTrue(result.properties.any { it.name == "DTSTAMP" })
        assertTrue(result.properties.any { it.name == "DTSTART" })
        assertTrue(result.properties.any { it.name == "DTEND" })
    }

    @Test
    fun eventWithoutSensitiveDataIsUnchanged() {
        val event = basicEvent()
        val result = event.anonymize()
        assertEquals(event.properties, result.properties)
    }

    @Test
    fun preservesComponents() {
        val event = basicEvent().builder { it.alarm { } }
        val result = event.anonymize()
        assertEquals(event.components, result.components)
    }

    @Test
    fun customFilterOverridesDefault() {
        val event = basicEvent().builder {
            it.summary("Keep me")
            it.location("Remove me")
        }
        val result = event.anonymize(filter = setOf("LOCATION"))
        assertFalse(result.properties.any { it.name == "LOCATION" })
        assertTrue(result.properties.any { it.name == "SUMMARY" })
    }

    @Test
    fun emptyFilterRetainsAllProperties() {
        val event = basicEvent().builder { it.summary("Keep me") }
        val result = event.anonymize(filter = emptySet())
        assertEquals(event.properties, result.properties)
    }

    @Test
    fun builderFilterSelectivelyStrips() {
        val event = basicEvent().builder {
            it.summary("Remove me")
            it.location("Keep me")
        }
        val result = event.anonymize { summary() }
        assertFalse(result.properties.any { it.name == "SUMMARY" })
        assertTrue(result.properties.any { it.name == "LOCATION" })
    }

    @Test
    fun builderFilterEmpty() {
        val event = basicEvent().builder { it.summary("Keep me") }
        val result = event.anonymize { }
        assertTrue(result.properties.any { it.name == "SUMMARY" })
    }

    @Test
    fun builderPrivacyRedactedMatchesDefault() {
        val event = basicEvent().builder {
            it.summary("Remove me")
            it.description("Remove me")
            it.location("Remove me")
            it.organizer("boss@example.com")
            it.attendee("alice@example.com")
        }
        val result = event.anonymize { privacyRedacted() }
        assertEquals(event.anonymize(), result)
    }
}
