package net.alphadev.icalendar.import

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import net.alphadev.icalendar.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CustomTimeZoneDateTimeParsingTest {
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
}
