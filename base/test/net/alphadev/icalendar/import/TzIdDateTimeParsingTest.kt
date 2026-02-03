package net.alphadev.icalendar.import

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import net.alphadev.icalendar.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TzIdDateTimeParsingTest {
    @Test
    fun parsesDateTimeWithTzid() {
        val ics = """
            BEGIN:VCALENDAR
            VERSION:2.0
            BEGIN:VEVENT
            UID:test-001
            DTSTART;TZID=America/New_York:20240615T090000
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val calendar = parseICalendar(ics).first()
        val event = calendar.events.first()
        val dtStart = event.dtStart

        assertNotNull(dtStart)
        // June 15 = EDT = UTC-4, so 09:00 EDT = 13:00 UTC
        val expected = LocalDateTime(2024, 6, 15, 13, 0, 0).toInstant(TimeZone.UTC)
        assertEquals(expected, dtStart)
    }

    @Test
    fun parsesDateTimeWithTzidWinter() {
        val ics = """
            BEGIN:VCALENDAR
            VERSION:2.0
            BEGIN:VEVENT
            UID:test-001
            DTSTART;TZID=America/New_York:20240115T090000
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val calendar = parseICalendar(ics).first()
        val event = calendar.events.first()
        val dtStart = event.dtStart

        assertNotNull(dtStart)
        // January 15 = EST = UTC-5, so 09:00 EST = 14:00 UTC
        val expected = LocalDateTime(2024, 1, 15, 14, 0, 0).toInstant(TimeZone.UTC)
        assertEquals(expected, dtStart)
    }

    @Test
    fun parsesDateOnlyWithTzidUsesTzidForMidnight() {
        // Edge case: DATE with TZID - should midnight be in that timezone?
        val ics = """
            BEGIN:VCALENDAR
            VERSION:2.0
            BEGIN:VEVENT
            UID:test-001
            DTSTART;VALUE=DATE;TZID=America/New_York:20240615
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val calendar = parseICalendar(ics).first()
        val event = calendar.events.first()
        val dtStart = event.dtStart

        assertNotNull(dtStart)
        // June 15 midnight in New York (EDT = UTC-4) = 04:00 UTC
        val expected = LocalDateTime(2024, 6, 15, 4, 0, 0).toInstant(TimeZone.UTC)
        assertEquals(expected, dtStart)
    }

    @Test
    fun parsesLeadingSlashTzid() {
        val ics = """
            BEGIN:VCALENDAR
            VERSION:2.0
            BEGIN:VEVENT
            UID:test-001
            DTSTART;TZID=/America/New_York:20240615T090000
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val calendar = parseICalendar(ics).first()
        val event = calendar.events.first()
        val dtStart = event.dtStart

        assertNotNull(dtStart)
        val expected = LocalDateTime(2024, 6, 15, 13, 0, 0).toInstant(TimeZone.UTC)
        assertEquals(expected, dtStart)
    }

    @Test
    fun parsesMozillaStyleTzid() {
        val ics = """
            BEGIN:VCALENDAR
            VERSION:2.0
            BEGIN:VEVENT
            UID:test-001
            DTSTART;TZID=/mozilla.org/20050126_1/America/New_York:20240615T090000
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val calendar = parseICalendar(ics).first()
        val event = calendar.events.first()
        val dtStart = event.dtStart

        assertNotNull(dtStart)
        val expected = LocalDateTime(2024, 6, 15, 13, 0, 0).toInstant(TimeZone.UTC)
        assertEquals(expected, dtStart)
    }
}
