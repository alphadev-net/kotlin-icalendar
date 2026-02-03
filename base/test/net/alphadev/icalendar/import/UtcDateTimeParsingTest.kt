package net.alphadev.icalendar.import

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import net.alphadev.icalendar.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class UtcDateTimeParsingTest {
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
}
