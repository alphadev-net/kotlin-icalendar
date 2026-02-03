package net.alphadev.icalendar.transform

import kotlinx.datetime.LocalDateTime
import net.alphadev.icalendar.model.*
import net.alphadev.icalendar.model.dsl.vCalendar
import kotlin.test.*
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class VEventOffsetTest {

    @Test
    fun offsetTimesByAddsPositiveDuration() {
        val start = Instant.parse("2024-01-15T10:00:00Z")
        val end = Instant.parse("2024-01-15T11:00:00Z")

        val cal = vCalendar {
            event {
                uid("test-1")
                dtStart(start)
                dtEnd(end)
            }
        }

        val result = cal.events.first().offsetTimesBy(2.hours)

        assertEquals(Instant.parse("2024-01-15T12:00:00Z"), result.dtStart)
        assertEquals(Instant.parse("2024-01-15T13:00:00Z"), result.dtEnd)
    }

    @Test
    fun offsetTimesBySubtractsNegativeDuration() {
        val start = Instant.parse("2024-01-15T10:00:00Z")
        val end = Instant.parse("2024-01-15T11:00:00Z")

        val cal = vCalendar {
            event {
                uid("test-1")
                dtStart(start)
                dtEnd(end)
            }
        }

        val result = cal.events.first().offsetTimesBy(-2.hours)

        assertEquals(Instant.parse("2024-01-15T08:00:00Z"), result.dtStart)
        assertEquals(Instant.parse("2024-01-15T09:00:00Z"), result.dtEnd)
    }

    @Test
    fun offsetTimesByWorksWithDays() {
        val start = Instant.parse("2024-01-15T10:00:00Z")

        val cal = vCalendar {
            event {
                uid("test-1")
                dtStart(start)
            }
        }

        val result = cal.events.first().offsetTimesBy(3.days)

        assertEquals(Instant.parse("2024-01-18T10:00:00Z"), result.dtStart)
    }

    @Test
    fun offsetTimesByPreservesOtherProperties() {
        val start = Instant.parse("2024-01-15T10:00:00Z")

        val cal = vCalendar {
            event {
                uid("test-1")
                summary("Meeting")
                description("Important meeting")
                dtStart(start)
            }
        }

        val original = cal.events.first()
        val result = original.offsetTimesBy(1.hours)

        assertEquals(original.uid, result.uid)
        assertEquals(original.summary, result.summary)
        assertEquals(original.description, result.description)
    }

    @Test
    fun offsetTimesByHandlesEventWithOnlyStart() {
        val start = Instant.parse("2024-01-15T10:00:00Z")

        val cal = vCalendar {
            event {
                uid("test-1")
                dtStart(start)
            }
        }

        val result = cal.events.first().offsetTimesBy(1.hours)

        assertEquals(Instant.parse("2024-01-15T11:00:00Z"), result.dtStart)
        assertNull(result.dtEnd)
    }

    @Test
    fun offsetTimesByPreservesTimezoneInfo() {
        val localTime = LocalDateTime(2024, 1, 15, 10, 0)

        val cal = vCalendar {
            event {
                uid("test-1")
                dtStart(localTime, "America/New_York")
            }
        }

        val original = cal.events.first()
        val result = original.offsetTimesBy(1.hours)

        // Instant is always UTC-normalized, so offset applies in UTC
        assertNotNull(result.dtStart)
        assertEquals(original.dtStart!!.plus(1.hours), result.dtStart)

        // TZID parameter should be preserved
        assertEquals("America/New_York", result.dtStartProperty?.tzid)
    }
}
