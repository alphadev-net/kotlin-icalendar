package net.alphadev.icalendar.ical

import kotlinx.io.files.Path
import net.alphadev.icalendar.cli.removeEventAlarms
import net.alphadev.icalendar.cli.sources.readICalFromFile
import net.alphadev.icalendar.model.events
import net.alphadev.icalendar.model.hasAlarms
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class RemoveAlarmsTest {
    @Test
    fun alarmsAreRemoved() {
        val calendars = readICalFromFile(Path("testResources/sample-events.ics"))

        assertTrue(calendars.isNotEmpty())

        val calendar = calendars.first()
        assertTrue(calendar.events.first().hasAlarms)

        val transformed = calendars.removeEventAlarms()
        val transformedCalendar = transformed.first()

        assertTrue(transformedCalendar.events.none { it.hasAlarms })
        assertEquals(2, transformedCalendar.events.size)
    }
}
