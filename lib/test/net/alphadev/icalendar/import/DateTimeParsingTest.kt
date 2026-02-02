package net.alphadev.icalendar.import

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import net.alphadev.icalendar.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DateTimeParsingTest {

    // ===================
    // UTC suffix (Z)
    // ===================

    @Test
    fun parsesUtcSuffixDateTime() {
        val ics = """
            BEGIN:VCALENDAR
            VERSION:2.0
            BEGIN:VEVENT
            UID:test-001
            DTSTART:20240120T090000Z
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val calendar = parseICalendar(ics).first()
        val event = calendar.events.first()
        val dtStart = event.dtStart

        assertNotNull(dtStart)
        val expected = LocalDateTime(2024, 1, 20, 9, 0, 0).toInstant(TimeZone.UTC)
        assertEquals(expected, dtStart)
    }

    // ===================
    // DateTime with TZID
    // ===================

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

    // ===================
    // Floating time (no TZID, no Z)
    // ===================

    @Test
    fun parsesFloatingTimeAsUtc() {
        val ics = """
            BEGIN:VCALENDAR
            VERSION:2.0
            BEGIN:VEVENT
            UID:test-001
            DTSTART:20240120T090000
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val calendar = parseICalendar(ics).first()
        val event = calendar.events.first()
        val dtStart = event.dtStart

        assertNotNull(dtStart)
        // Floating time treated as UTC
        val expected = LocalDateTime(2024, 1, 20, 9, 0, 0).toInstant(TimeZone.UTC)
        assertEquals(expected, dtStart)
    }

    // ===================
    // DATE only (VALUE=DATE)
    // ===================

    @Test
    fun parsesDateOnlyAsMidnightUtc() {
        val ics = """
            BEGIN:VCALENDAR
            VERSION:2.0
            BEGIN:VEVENT
            UID:test-001
            DTSTART;VALUE=DATE:20240120
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val calendar = parseICalendar(ics).first()
        val event = calendar.events.first()
        val dtStart = event.dtStart

        assertNotNull(dtStart)
        val expected = LocalDateTime(2024, 1, 20, 0, 0, 0).toInstant(TimeZone.UTC)
        assertEquals(expected, dtStart)
        assertTrue(event.isAllDay)
    }

    @Test
    fun parsesDateOnlyWithoutExplicitValueParam() {
        // Some producers don't include VALUE=DATE, just use 8-digit date
        val ics = """
            BEGIN:VCALENDAR
            VERSION:2.0
            BEGIN:VEVENT
            UID:test-001
            DTSTART:20240120
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val calendar = parseICalendar(ics).first()
        val event = calendar.events.first()
        val dtStart = event.dtStart

        assertNotNull(dtStart)
        val expected = LocalDateTime(2024, 1, 20, 0, 0, 0).toInstant(TimeZone.UTC)
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

    // ===================
    // Leading slash TZID
    // ===================

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

    // ===================
    // TZID referencing VTIMEZONE
    // ===================

    @Test
    fun parsesCustomVTimezoneReference() {
        val ics = """
            BEGIN:VCALENDAR
            VERSION:2.0
            BEGIN:VTIMEZONE
            TZID:Custom/Eastern
            BEGIN:STANDARD
            DTSTART:19701101T020000
            TZOFFSETTO:-0500
            TZOFFSETFROM:-0400
            RRULE:FREQ=YEARLY;BYMONTH=11;BYDAY=1SU
            END:STANDARD
            BEGIN:DAYLIGHT
            DTSTART:19700308T020000
            TZOFFSETTO:-0400
            TZOFFSETFROM:-0500
            RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=2SU
            END:DAYLIGHT
            END:VTIMEZONE
            BEGIN:VEVENT
            UID:test-001
            DTSTART;TZID=Custom/Eastern:20240615T090000
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val calendar = parseICalendar(ics).first()
        val event = calendar.events.first()
        val dtStart = event.dtStart

        assertNotNull(dtStart)
        // June = daylight = -0400, so 09:00 = 13:00 UTC
        val expected = LocalDateTime(2024, 6, 15, 13, 0, 0).toInstant(TimeZone.UTC)
        assertEquals(expected, dtStart)
    }

    @Test
    fun parsesCustomVTimezoneWinterTime() {
        val ics = """
            BEGIN:VCALENDAR
            VERSION:2.0
            BEGIN:VTIMEZONE
            TZID:Custom/Eastern
            BEGIN:STANDARD
            DTSTART:19701101T020000
            TZOFFSETTO:-0500
            TZOFFSETFROM:-0400
            RRULE:FREQ=YEARLY;BYMONTH=11;BYDAY=1SU
            END:STANDARD
            BEGIN:DAYLIGHT
            DTSTART:19700308T020000
            TZOFFSETTO:-0400
            TZOFFSETFROM:-0500
            RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=2SU
            END:DAYLIGHT
            END:VTIMEZONE
            BEGIN:VEVENT
            UID:test-001
            DTSTART;TZID=Custom/Eastern:20240115T090000
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val calendar = parseICalendar(ics).first()
        val event = calendar.events.first()
        val dtStart = event.dtStart

        assertNotNull(dtStart)
        // January = standard = -0500, so 09:00 = 14:00 UTC
        val expected = LocalDateTime(2024, 1, 15, 14, 0, 0).toInstant(TimeZone.UTC)
        assertEquals(expected, dtStart)
    }

    // ===================
    // Undefined VTIMEZONE fallback
    // ===================

    @Test
    fun undefinedVTimezoneWithIanaNameFallsBack() {
        val ics = """
            BEGIN:VCALENDAR
            VERSION:2.0
            BEGIN:VEVENT
            UID:test-001
            DTSTART;TZID=Europe/London:20240615T090000
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val calendar = parseICalendar(ics).first()
        val event = calendar.events.first()
        val dtStart = event.dtStart

        assertNotNull(dtStart)
        // June in London = BST = UTC+1, so 09:00 = 08:00 UTC
        val expected = LocalDateTime(2024, 6, 15, 8, 0, 0).toInstant(TimeZone.UTC)
        assertEquals(expected, dtStart)
    }

    @Test
    fun undefinedVTimezoneUnknownNameFallsToUtc() {
        val ics = """
            BEGIN:VCALENDAR
            VERSION:2.0
            BEGIN:VEVENT
            UID:test-001
            DTSTART;TZID=Nonexistent/Zone:20240615T090000
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val calendar = parseICalendar(ics).first()
        val event = calendar.events.first()
        val dtStart = event.dtStart

        assertNotNull(dtStart)
        // Unknown falls back to UTC
        val expected = LocalDateTime(2024, 6, 15, 9, 0, 0).toInstant(TimeZone.UTC)
        assertEquals(expected, dtStart)
    }

    // ===================
    // Malformed values
    // ===================

    @Test
    fun malformedDateTimeReturnsNullInstant() {
        val ics = """
            BEGIN:VCALENDAR
            VERSION:2.0
            BEGIN:VEVENT
            UID:test-001
            DTSTART:not-a-date
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val calendar = parseICalendar(ics).first()
        val event = calendar.events.first()

        assertNull(event.dtStart)
    }

    @Test
    fun emptyDateTimeReturnsNullInstant() {
        val ics = """
            BEGIN:VCALENDAR
            VERSION:2.0
            BEGIN:VEVENT
            UID:test-001
            DTSTART:
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val calendar = parseICalendar(ics).first()
        val event = calendar.events.first()

        assertNull(event.dtStart)
    }
}
