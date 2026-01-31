package net.alphadev.icalendar.import

import net.alphadev.icalendar.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

class ICalParserTest {

    @Test
    fun parseSimpleEvent() {
        val ics = """
            BEGIN:VCALENDAR
            VERSION:2.0
            PRODID:-//Test//EN
            BEGIN:VEVENT
            UID:simple-001
            DTSTAMP:20240115T120000Z
            DTSTART:20240120T090000Z
            DTEND:20240120T100000Z
            SUMMARY:Simple Meeting
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val calendars = parseICalendar(ics)
        assertEquals(1, calendars.size)

        val event = calendars.first().events.first()
        assertEquals("simple-001", event.uid)
        assertEquals("Simple Meeting", event.summary)
    }

    @Test
    fun parseAllDayEvent() {
        val ics = """
            BEGIN:VCALENDAR
            VERSION:2.0
            PRODID:-//Test//EN
            BEGIN:VEVENT
            UID:allday-001
            DTSTAMP:20240115T120000Z
            DTSTART;VALUE=DATE:20240120
            DTEND;VALUE=DATE:20240121
            SUMMARY:All Day Event
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val calendars = parseICalendar(ics)
        val event = calendars.first().events.first()

        assertTrue(event.isAllDay)
        val dtStart = event.dtStart as ICalTemporal.Date
        assertEquals(2024, dtStart.date.year)
        assertEquals(1, dtStart.date.monthNumber)
        assertEquals(20, dtStart.date.day)
    }

    @Test
    fun parseEventWithTimezone() {
        val ics = """
            BEGIN:VCALENDAR
            VERSION:2.0
            PRODID:-//Test//EN
            BEGIN:VEVENT
            UID:tz-001
            DTSTAMP:20240115T120000Z
            DTSTART;TZID=America/New_York:20240120T090000
            DTEND;TZID=America/New_York:20240120T100000
            SUMMARY:Meeting in New York
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val calendars = parseICalendar(ics)
        val event = calendars.first().events.first()

        val dtStart = event.dtStart as ICalTemporal.DateTime
        assertEquals("America/New_York", dtStart.tzid)
        assertEquals(9, dtStart.dateTime.hour)
    }

    @Test
    fun parseEventWithUtcTimestamp() {
        val ics = """
            BEGIN:VCALENDAR
            VERSION:2.0
            PRODID:-//Test//EN
            BEGIN:VEVENT
            UID:utc-001
            DTSTAMP:20240115T120000Z
            DTSTART:20240120T140000Z
            DTEND:20240120T150000Z
            SUMMARY:UTC Meeting
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val calendars = parseICalendar(ics)
        val event = calendars.first().events.first()

        val dtStart = event.dtStart as ICalTemporal.DateTimeUtc
        assertNotNull(dtStart.instant)
    }

    @Test
    fun parseEventWithDescriptionAndLocation() {
        val ics = """
            BEGIN:VCALENDAR
            VERSION:2.0
            PRODID:-//Test//EN
            BEGIN:VEVENT
            UID:details-001
            DTSTAMP:20240115T120000Z
            DTSTART:20240120T090000Z
            SUMMARY:Team Standup
            DESCRIPTION:Daily standup meeting to discuss progress
            LOCATION:Conference Room A
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val calendars = parseICalendar(ics)
        val event = calendars.first().events.first()

        assertEquals("Team Standup", event.summary)
        assertEquals("Daily standup meeting to discuss progress", event.description)
        assertEquals("Conference Room A", event.location)
    }

    @Test
    fun parseEventWithOrganizerAndAttendees() {
        val ics = """
            BEGIN:VCALENDAR
            VERSION:2.0
            PRODID:-//Test//EN
            BEGIN:VEVENT
            UID:attendees-001
            DTSTAMP:20240115T120000Z
            DTSTART:20240120T090000Z
            SUMMARY:Project Review
            ORGANIZER;CN=Alice Smith:mailto:alice@example.com
            ATTENDEE;CN=Bob Jones;ROLE=REQ-PARTICIPANT:mailto:bob@example.com
            ATTENDEE;CN=Carol White;ROLE=OPT-PARTICIPANT:mailto:carol@example.com
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val calendars = parseICalendar(ics)
        val event = calendars.first().events.first()

        assertEquals("mailto:alice@example.com", event.organizer)

        val attendees = event.properties.filter { it.name == "ATTENDEE" }
        assertEquals(2, attendees.size)
        assertEquals("Bob Jones", attendees[0].parameter("CN"))
        assertEquals("REQ-PARTICIPANT", attendees[0].parameter("ROLE"))
    }

    @Test
    fun parseEventWithAlarm() {
        val ics = """
            BEGIN:VCALENDAR
            VERSION:2.0
            PRODID:-//Test//EN
            BEGIN:VEVENT
            UID:alarm-001
            DTSTAMP:20240115T120000Z
            DTSTART:20240120T090000Z
            SUMMARY:Meeting with Alarm
            BEGIN:VALARM
            ACTION:DISPLAY
            TRIGGER:-PT15M
            DESCRIPTION:Meeting starts in 15 minutes
            END:VALARM
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val calendars = parseICalendar(ics)
        val event = calendars.first().events.first()

        assertTrue(event.hasAlarms)
        assertEquals(1, event.alarms.size)

        val alarm = event.alarms.first()
        assertEquals(AlarmAction.DISPLAY, alarm.action)
        assertEquals("Meeting starts in 15 minutes", alarm.description)

        val trigger = alarm.trigger as AlarmTrigger.Relative
        assertEquals((-15).minutes, trigger.duration)
        assertEquals(AlarmTrigger.RelatedTo.START, trigger.relatedTo)
    }

    @Test
    fun parseEventWithMultipleAlarms() {
        val ics = """
            BEGIN:VCALENDAR
            VERSION:2.0
            PRODID:-//Test//EN
            BEGIN:VEVENT
            UID:multi-alarm-001
            DTSTAMP:20240115T120000Z
            DTSTART:20240120T090000Z
            SUMMARY:Important Meeting
            BEGIN:VALARM
            ACTION:DISPLAY
            TRIGGER:-PT30M
            DESCRIPTION:30 minutes before
            END:VALARM
            BEGIN:VALARM
            ACTION:AUDIO
            TRIGGER:-PT5M
            END:VALARM
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val calendars = parseICalendar(ics)
        val event = calendars.first().events.first()

        assertEquals(2, event.alarms.size)
        assertEquals(AlarmAction.DISPLAY, event.alarms[0].action)
        assertEquals(AlarmAction.AUDIO, event.alarms[1].action)
    }

    @Test
    fun parseEventWithStatusAndTransparency() {
        val ics = """
            BEGIN:VCALENDAR
            VERSION:2.0
            PRODID:-//Test//EN
            BEGIN:VEVENT
            UID:status-001
            DTSTAMP:20240115T120000Z
            DTSTART:20240120T090000Z
            SUMMARY:Confirmed Meeting
            STATUS:CONFIRMED
            TRANSP:OPAQUE
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val calendars = parseICalendar(ics)
        val event = calendars.first().events.first()

        assertEquals("CONFIRMED", event.status)
        assertEquals("OPAQUE", event.transp)
    }

    @Test
    fun parseEventWithSequenceNumber() {
        val ics = """
            BEGIN:VCALENDAR
            VERSION:2.0
            PRODID:-//Test//EN
            BEGIN:VEVENT
            UID:sequence-001
            DTSTAMP:20240115T120000Z
            DTSTART:20240120T090000Z
            SUMMARY:Updated Meeting
            SEQUENCE:3
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val calendars = parseICalendar(ics)
        val event = calendars.first().events.first()

        assertEquals(3, event.sequence)
    }

    @Test
    fun parseEventWithCustomXProperties() {
        val ics = """
            BEGIN:VCALENDAR
            VERSION:2.0
            PRODID:-//Test//EN
            BEGIN:VEVENT
            UID:custom-001
            DTSTAMP:20240115T120000Z
            DTSTART:20240120T090000Z
            SUMMARY:Custom Event
            X-CUSTOM-FIELD:custom-value
            X-ANOTHER-PROP:another-value
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val calendars = parseICalendar(ics)
        val event = calendars.first().events.first()

        val customProp = event.properties.find { it.name == "X-CUSTOM-FIELD" }
        assertNotNull(customProp)
        assertEquals("custom-value", customProp.value)
        assertTrue(customProp.isExtension)
    }

    @Test
    fun parseFoldedLines() {
        val ics = """
            BEGIN:VCALENDAR
            VERSION:2.0
            PRODID:-//Test//EN
            BEGIN:VEVENT
            UID:folded-001
            DTSTAMP:20240115T120000Z
            DTSTART:20240120T090000Z
            SUMMARY:Meeting with a very long summary that needs to be folded across
              multiple lines in the iCalendar file
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val calendars = parseICalendar(ics)
        val event = calendars.first().events.first()

        assertEquals(
            "Meeting with a very long summary that needs to be folded across multiple lines in the iCalendar file",
            event.summary
        )
    }

    @Test
    fun parseEscapedCharactersInValues() {
        val ics = """
            BEGIN:VCALENDAR
            VERSION:2.0
            PRODID:-//Test//EN
            BEGIN:VEVENT
            UID:escaped-001
            DTSTAMP:20240115T120000Z
            DTSTART:20240120T090000Z
            SUMMARY:Meeting\, with\; special\ncharacters
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val calendars = parseICalendar(ics)
        val event = calendars.first().events.first()

        assertEquals("Meeting\\, with\\; special\\ncharacters", event.summary)
    }

    @Test
    fun parseQuotedParameterValues() {
        val ics = """
            BEGIN:VCALENDAR
            VERSION:2.0
            PRODID:-//Test//EN
            BEGIN:VEVENT
            UID:quoted-001
            DTSTAMP:20240115T120000Z
            DTSTART:20240120T090000Z
            SUMMARY:Meeting
            ORGANIZER;CN="Smith, John":mailto:john@example.com
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val calendars = parseICalendar(ics)
        val event = calendars.first().events.first()

        val organizer = event.properties.find { it.name == "ORGANIZER" }
        assertNotNull(organizer)
        assertEquals("Smith, John", organizer.parameter("CN"))
    }

    @Test
    fun parseMultipleEventsInOneCalendar() {
        val ics = """
            BEGIN:VCALENDAR
            VERSION:2.0
            PRODID:-//Test//EN
            BEGIN:VEVENT
            UID:multi-001
            DTSTAMP:20240115T120000Z
            DTSTART:20240120T090000Z
            SUMMARY:First Event
            END:VEVENT
            BEGIN:VEVENT
            UID:multi-002
            DTSTAMP:20240115T120000Z
            DTSTART:20240121T090000Z
            SUMMARY:Second Event
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val calendars = parseICalendar(ics)
        assertEquals(1, calendars.size)
        assertEquals(2, calendars.first().events.size)
        assertEquals("First Event", calendars.first().events[0].summary)
        assertEquals("Second Event", calendars.first().events[1].summary)
    }

    @Test
    fun parseMultipleCalendarsInOneFile() {
        val ics = """
            BEGIN:VCALENDAR
            VERSION:2.0
            PRODID:-//First//EN
            BEGIN:VEVENT
            UID:cal1-001
            DTSTAMP:20240115T120000Z
            DTSTART:20240120T090000Z
            SUMMARY:Event in First Calendar
            END:VEVENT
            END:VCALENDAR
            BEGIN:VCALENDAR
            VERSION:2.0
            PRODID:-//Second//EN
            BEGIN:VEVENT
            UID:cal2-001
            DTSTAMP:20240115T120000Z
            DTSTART:20240121T090000Z
            SUMMARY:Event in Second Calendar
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val calendars = parseICalendar(ics)
        assertEquals(2, calendars.size)
        assertEquals("-//First//EN", calendars[0].prodId)
        assertEquals("-//Second//EN", calendars[1].prodId)
    }

    @Test
    fun parseCalendarProperties() {
        val ics = """
            BEGIN:VCALENDAR
            VERSION:2.0
            PRODID:-//My Company//My Product//EN
            CALSCALE:GREGORIAN
            METHOD:PUBLISH
            BEGIN:VEVENT
            UID:props-001
            DTSTAMP:20240115T120000Z
            DTSTART:20240120T090000Z
            SUMMARY:Test
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val calendars = parseICalendar(ics)
        val calendar = calendars.first()

        assertEquals("2.0", calendar.version)
        assertEquals("-//My Company//My Product//EN", calendar.prodId)
        assertEquals("GREGORIAN", calendar.calScale)
        assertEquals("PUBLISH", calendar.method)
    }

    @Test
    fun parseUnknownComponentAsUnknownComponent() {
        val ics = """
            BEGIN:VCALENDAR
            VERSION:2.0
            PRODID:-//Test//EN
            BEGIN:VTODO
            UID:todo-001
            DTSTAMP:20240115T120000Z
            SUMMARY:A Todo Item
            END:VTODO
            END:VCALENDAR
        """.trimIndent()

        val calendars = parseICalendar(ics)
        val calendar = calendars.first()

        assertEquals(0, calendar.events.size)
        assertEquals(1, calendar.components.size)

        val todo = calendar.components.first() as UnknownComponent
        assertEquals("VTODO", todo.name)
        assertEquals("A Todo Item", todo.properties.find { it.name == "SUMMARY" }?.value)
    }

    @Test
    fun parseAlarmWithRelatedEnd() {
        val ics = """
            BEGIN:VCALENDAR
            VERSION:2.0
            PRODID:-//Test//EN
            BEGIN:VEVENT
            UID:alarm-end-001
            DTSTAMP:20240115T120000Z
            DTSTART:20240120T090000Z
            DTEND:20240120T100000Z
            SUMMARY:Meeting
            BEGIN:VALARM
            ACTION:DISPLAY
            TRIGGER;RELATED=END:PT0S
            DESCRIPTION:Meeting has ended
            END:VALARM
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val calendars = parseICalendar(ics)
        val alarm = calendars.first().events.first().alarms.first()

        val trigger = alarm.trigger as AlarmTrigger.Relative
        assertEquals(AlarmTrigger.RelatedTo.END, trigger.relatedTo)
    }

    @Test
    fun parseCreatedAndLastModifiedTimestamps() {
        val ics = """
            BEGIN:VCALENDAR
            VERSION:2.0
            PRODID:-//Test//EN
            BEGIN:VEVENT
            UID:timestamps-001
            DTSTAMP:20240115T120000Z
            CREATED:20240101T080000Z
            LAST-MODIFIED:20240110T150000Z
            DTSTART:20240120T090000Z
            SUMMARY:Timestamped Event
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val calendars = parseICalendar(ics)
        val event = calendars.first().events.first()

        assertNotNull(event.dtStamp)
        assertNotNull(event.created)
        assertNotNull(event.lastModified)
    }

    @Test
    fun parseEmptyInputReturnsEmptyList() {
        val calendars = parseICalendar("")
        assertTrue(calendars.isEmpty())
    }

    @Test
    fun parseInvalidInputReturnsEmptyList() {
        val calendars = parseICalendar("not a valid icalendar file")
        assertTrue(calendars.isEmpty())
    }

    @Test
    fun parseEventWithFloatingLocalTime() {
        val ics = """
            BEGIN:VCALENDAR
            VERSION:2.0
            PRODID:-//Test//EN
            BEGIN:VEVENT
            UID:floating-001
            DTSTAMP:20240115T120000Z
            DTSTART:20240120T090000
            DTEND:20240120T100000
            SUMMARY:Floating Time Event
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val calendars = parseICalendar(ics)
        val event = calendars.first().events.first()

        val dtStart = event.dtStart as ICalTemporal.DateTime
        assertNull(dtStart.tzid)
        assertEquals(9, dtStart.dateTime.hour)
    }
}
