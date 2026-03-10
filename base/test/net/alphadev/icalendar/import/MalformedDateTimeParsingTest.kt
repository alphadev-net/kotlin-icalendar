@file:Suppress("NO_EXPLICIT_VISIBILITY_IN_API_MODE_WARNING")
package net.alphadev.icalendar.import

import net.alphadev.icalendar.model.dtStart
import net.alphadev.icalendar.model.events
import kotlin.test.Test
import kotlin.test.assertNull

class MalformedDateTimeParsingTest {
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
