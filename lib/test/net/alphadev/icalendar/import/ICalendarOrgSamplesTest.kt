package net.alphadev.icalendar.import

import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import net.alphadev.icalendar.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ICalendarOrgSamplesTest {

    @Test
    fun decodeRfc5545SingleEventExample() {
        val rfc5545SingleEvent = """
            BEGIN:VCALENDAR
            PRODID:-//xyz Corp//NONSGML PDA Calendar Version 1.0//EN
            VERSION:2.0
            BEGIN:VEVENT
            UID:uid1@example.com
            DTSTAMP:19960704T120000Z
            DTSTART:19960918T143000Z
            DTEND:19960920T220000Z
            SUMMARY:Networld+Interop Conference
            DESCRIPTION:Networld+Interop Conference
              and Exhibit
            LOCATION:San Jose Convention Center
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val calendar = parseICalendar(rfc5545SingleEvent).first()

        assertEquals("2.0", calendar.version)
        assertEquals("-//xyz Corp//NONSGML PDA Calendar Version 1.0//EN", calendar.prodId)
        assertEquals(1, calendar.events.size)

        val event = calendar.events.first()
        assertEquals("uid1@example.com", event.uid)
        assertEquals("Networld+Interop Conference", event.summary)
        assertEquals("Networld+Interop Conference and Exhibit", event.description)
        assertEquals("San Jose Convention Center", event.location)

        val dtStart = event.dtStart!!
        val start = dtStart.toLocalDateTime(TimeZone.UTC)
        assertEquals(1996, start.year)
        assertEquals(9, start.month.number)
        assertEquals(18, start.day)
        assertEquals(14, start.hour)
        assertEquals(30, start.minute)

        val dtEnd = event.dtEnd!!
        val end = dtEnd.toLocalDateTime(TimeZone.UTC)
        assertEquals(1996, end.year)
        assertEquals(9, end.month.number)
        assertEquals(20, end.day)
        assertEquals(22, end.hour)
        assertEquals(0, end.minute)
    }

    @Test
    fun decodeRfc5545VtodoExample() {
        val rfcVtodo = """
            BEGIN:VCALENDAR
            VERSION:2.0
            PRODID:-//xyz Corp//NONSGML PDA Calendar Version 1.0//EN
            BEGIN:VTODO
            UID:uid2@example.com
            DTSTAMP:19960401T150000Z
            DUE:19960403T150000Z
            SUMMARY:Submit TPS Report
            PRIORITY:1
            STATUS:NEEDS-ACTION
            END:VTODO
            END:VCALENDAR
        """.trimIndent()

        val calendar = parseICalendar(rfcVtodo).first()
        assertEquals(1, calendar.todos.size)

        val todo = calendar.todos.first()
        assertEquals("uid2@example.com", todo.uid)
        assertEquals("Submit TPS Report", todo.summary)
        assertEquals("NEEDS-ACTION", todo.status)
        assertEquals(1, todo.priority)

        assertEquals(null, todo.completed)

        val due = todo.due!!
        val dueTime = due.toLocalDateTime(TimeZone.UTC)
        assertEquals(1996, dueTime.year)
        assertEquals(4, dueTime.month.number)
        assertEquals(3, dueTime.day)
        assertEquals(15, dueTime.hour)
        assertEquals(0, dueTime.minute)

        val dtStart = todo.dtStart!!
        val start = dtStart.toLocalDateTime(TimeZone.UTC)
        assertEquals(1996, start.year)
        assertEquals(4, start.month.number)
        assertEquals(1, start.day)
        assertEquals(15, start.hour)
        assertEquals(0, start.minute)
    }

    @Test
    fun decodeRfc5545VjournalExample() {
        val rfcVjournal = """
            BEGIN:VCALENDAR
            VERSION:2.0
            PRODID:-//xyz Corp//NONSGML PDA Calendar Version 1.0//EN
            BEGIN:VJOURNAL
            UID:uid3@example.com
            DTSTAMP:19960401T120000Z
            DTSTART:19960401T120000Z
            SUMMARY:Journal entry
            DESCRIPTION:Today I learned something cool.
            END:VJOURNAL
            END:VCALENDAR
        """.trimIndent()

        val calendar = parseICalendar(rfcVjournal).first()
        assertEquals(1, calendar.journals.size)
        val journal = calendar.journals.first()
        assertEquals("uid3@example.com", journal.uid)
        assertEquals("Journal entry", journal.summary)
        assertEquals("Today I learned something cool.", journal.description)

        val dtStart = journal.dtStart!!
        val start = dtStart.toLocalDateTime(TimeZone.UTC)
        assertEquals(1996, start.year)
        assertEquals(4, start.month.number)
        assertEquals(1, start.day)
        assertEquals(12, start.hour)
        assertEquals(0, start.minute)
    }

    @Test
    fun decodeRfc5545VfreebusyExample() {
        val rfcVfreebusy = """
            BEGIN:VCALENDAR
            VERSION:2.0
            PRODID:-//xyz Corp//NONSGML PDA Calendar Version 1.0//EN
            BEGIN:VFREEBUSY
            UID:uid4@example.com
            DTSTAMP:19960401T120000Z
            DTSTART:19960401T080000Z
            DTEND:19960401T170000Z
            FREEBUSY:19960401T100000Z/19960401T120000Z
            END:VFREEBUSY
            END:VCALENDAR
        """.trimIndent()

        val calendar = parseICalendar(rfcVfreebusy).first()
        assertEquals(1, calendar.freeBusyItems.size)
        val fb = calendar.freeBusyItems.first()
        assertEquals("uid4@example.com", fb.uid)

        val dtStart = fb.dtStart!!
        val start = dtStart.toLocalDateTime(TimeZone.UTC)
        assertEquals(1996, start.year)
        assertEquals(4, start.month.number)
        assertEquals(1, start.day)
        assertEquals(8, start.hour)
        assertEquals(0, start.minute)

        val dtEnd = fb.dtEnd!!
        val end = dtEnd.toLocalDateTime(TimeZone.UTC)
        assertEquals(1996, end.year)
        assertEquals(4, end.month.number)
        assertEquals(1, end.day)
        assertEquals(17, end.hour)
        assertEquals(0, end.minute)

        val period = fb.freeBusyPeriods.first()
        assertEquals("19960401T100000Z/19960401T120000Z", period.toString())
    }

    @Test
    fun decodeRfc5545VtimezoneEventExample() {
        val rfcVTimezoneEvent = """
            BEGIN:VCALENDAR
            PRODID:-//RDU Software//NONSGML HandCal//EN
            VERSION:2.0
            BEGIN:VTIMEZONE
            TZID:America/New_York
            BEGIN:STANDARD
            DTSTART:19981025T020000
            TZOFFSETFROM:-0400
            TZOFFSETTO:-0500
            TZNAME:EST
            END:STANDARD
            BEGIN:DAYLIGHT
            DTSTART:19990404T020000
            TZOFFSETFROM:-0500
            TZOFFSETTO:-0400
            TZNAME:EDT
            END:DAYLIGHT
            END:VTIMEZONE
            BEGIN:VEVENT
            DTSTAMP:19980309T231000Z
            UID:guid-1.example.com
            ORGANIZER:mailto:mrbig@example.com
            ATTENDEE;RSVP=TRUE;ROLE=REQ-PARTICIPANT;CUTYPE=GROUP:
             mailto:employee-A@example.com
            DESCRIPTION:Project XYZ Review Meeting
            CATEGORIES:MEETING
            CLASS:PUBLIC
            CREATED:19980309T130000Z
            SUMMARY:XYZ Project Review
            DTSTART;TZID=America/New_York:19980312T083000
            DTEND;TZID=America/New_York:19980312T093000
            LOCATION:1CP Conference Room 4350
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val calendar = parseICalendar(rfcVTimezoneEvent).first()
        assertEquals(1, calendar.events.size)
        val event = calendar.events.first()

        // Parse in the event's local timezone
        val tz = TimeZone.of("America/New_York")
        val dtStart = event.dtStart!!
        val start = dtStart.toLocalDateTime(tz)
        assertEquals(8, start.hour)
        assertEquals(30, start.minute)
        assertEquals(1998, start.year)
        assertEquals(3, start.month.number)
        assertEquals(12, start.day)

        val dtEnd = event.dtEnd!!
        val end = dtEnd.toLocalDateTime(tz)
        assertEquals(9, end.hour)
        assertEquals(30, end.minute)
        assertEquals(1998, end.year)
        assertEquals(3, end.month.number)
        assertEquals(12, end.day)

        val attendee = event.attendees.first()
        assertEquals("employee-A@example.com", attendee.email)
        assertTrue(attendee.rsvp)
        assertEquals("REQ-PARTICIPANT", attendee.role)
        assertEquals("GROUP", attendee.cutype)
    }
}
