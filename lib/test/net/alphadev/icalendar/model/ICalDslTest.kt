package net.alphadev.icalendar.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import net.alphadev.icalendar.model.*
import net.alphadev.icalendar.model.dsl.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class ICalDslTest {

    @Test
    fun createEmptyCalendar() {
        val calendar = vCalendar {
            prodId("-//Test//EN")
        }

        assertEquals("2.0", calendar.version)
        assertEquals("-//Test//EN", calendar.prodId)
        assertTrue(calendar.events.isEmpty())
    }

    @Test
    fun createCalendarWithSingleEvent() {
        val calendar = vCalendar {
            prodId("-//Test//EN")
            event {
                uid("test-001")
                summary("Test Event")
            }
        }

        assertEquals(1, calendar.events.size)
        assertEquals("test-001", calendar.events.first().uid)
        assertEquals("Test Event", calendar.events.first().summary)
    }

    @Test
    fun createCalendarWithMultipleEvents() {
        val calendar = vCalendar {
            prodId("-//Test//EN")
            event {
                uid("event-001")
                summary("First Event")
            }
            event {
                uid("event-002")
                summary("Second Event")
            }
        }

        assertEquals(2, calendar.events.size)
        assertEquals("First Event", calendar.events[0].summary)
        assertEquals("Second Event", calendar.events[1].summary)
    }

    @Test
    fun createEventWithAllProperties() {
        val calendar = vCalendar {
            prodId("-//Test//EN")
            event {
                uid("full-001")
                summary("Full Event")
                description("A complete event with all properties")
                location("Conference Room A")
                status(EventStatus.CONFIRMED)
                transp(Transparency.OPAQUE)
                sequence(1)
                organizer("alice@example.com", "Alice Smith")
            }
        }

        val event = calendar.events.first()
        assertEquals("full-001", event.uid)
        assertEquals("Full Event", event.summary)
        assertEquals("A complete event with all properties", event.description)
        assertEquals("Conference Room A", event.location)
        assertEquals("CONFIRMED", event.status)
        assertEquals("OPAQUE", event.transp)
        assertEquals(1, event.sequence)
        assertEquals("mailto:alice@example.com", event.organizer)
    }

    @Test
    fun createEventWithLocalDateTime() {
        val calendar = vCalendar {
            prodId("-//Test//EN")
            event {
                uid("datetime-001")
                summary("DateTime Event")
                dtStart(LocalDateTime(2024, 6, 15, 10, 0))
                dtEnd(LocalDateTime(2024, 6, 15, 11, 0))
            }
        }

        val event = calendar.events.first()
        val dtStart = event.dtStart
        assertNotNull(dtStart)
        val utc = dtStart.toLocalDateTime(TimeZone.UTC)
        assertEquals(2024, utc.year)
        assertEquals(6, utc.month.number)
        assertEquals(15, utc.day)
        assertEquals(10, utc.hour)
    }

    @Test
    fun createEventWithTimezone() {
        val calendar = vCalendar {
            prodId("-//Test//EN")
            event {
                uid("tz-001")
                summary("Timezone Event")
                dtStart(LocalDateTime(2024, 6, 15, 10, 0), "America/New_York")
                dtEnd(LocalDateTime(2024, 6, 15, 11, 0), "America/New_York")
            }
        }

        val event = calendar.events.first()
        assertEquals("America/New_York", event.dtStartProperty?.tzid)
    }

    @Test
    fun createAllDayEvent() {
        val calendar = vCalendar {
            prodId("-//Test//EN")
            event {
                uid("allday-001")
                summary("All Day Event")
                dtStartDate(LocalDate(2024, 6, 15))
                dtEndDate(LocalDate(2024, 6, 16))
            }
        }

        val event = calendar.events.first()
        assertTrue(event.isAllDay)
        val dtStart = event.dtStart
        assertNotNull(dtStart)
        val utc = dtStart.toLocalDateTime(TimeZone.UTC)
        assertEquals(2024, utc.year)
        assertEquals(6, utc.month.number)
        assertEquals(15, utc.day)
    }

    @Test
    fun createEventWithDuration() {
        val calendar = vCalendar {
            prodId("-//Test//EN")
            event {
                uid("duration-001")
                summary("Duration Event")
                dtStart(LocalDateTime(2024, 6, 15, 10, 0))
                duration(2.hours)
            }
        }

        val event = calendar.events.first()
        val durationProp = event.properties.find { it.name == "DURATION" }
        assertNotNull(durationProp)
        assertEquals("PT2H", durationProp.value)
    }

    @Test
    fun createEventWithDisplayAlarm() {
        val calendar = vCalendar {
            prodId("-//Test//EN")
            event {
                uid("alarm-001")
                summary("Event with Alarm")
                dtStart(LocalDateTime(2024, 6, 15, 10, 0))
                alarm {
                    displayAction()
                    triggerBefore(15.minutes)
                    description("Meeting starts soon")
                }
            }
        }

        val event = calendar.events.first()
        assertEquals(1, event.alarms.size)

        val alarm = event.alarms.first()
        assertEquals(AlarmAction.DISPLAY, alarm.action)
        assertEquals("Meeting starts soon", alarm.description)

        val trigger = alarm.trigger as AlarmTrigger.Relative
        assertEquals((-15).minutes, trigger.duration)
    }

    @Test
    fun createEventWithAudioAlarm() {
        val calendar = vCalendar {
            prodId("-//Test//EN")
            event {
                uid("audio-001")
                summary("Event with Audio Alarm")
                dtStart(LocalDateTime(2024, 6, 15, 10, 0))
                alarm {
                    audioAction()
                    triggerBefore(5.minutes)
                    attach("file:///sounds/alert.wav")
                }
            }
        }

        val alarm = calendar.events.first().alarms.first()
        assertEquals(AlarmAction.AUDIO, alarm.action)
        assertEquals("file:///sounds/alert.wav", alarm.attach)
    }

    @Test
    fun createEventWithEmailAlarm() {
        val calendar = vCalendar {
            prodId("-//Test//EN")
            event {
                uid("email-001")
                summary("Event with Email Alarm")
                dtStart(LocalDateTime(2024, 6, 15, 10, 0))
                alarm {
                    emailAction()
                    triggerBefore(1.hours)
                    summary("Reminder: Meeting Soon")
                    description("Your meeting starts in 1 hour")
                    attendee("user@example.com")
                }
            }
        }

        val alarm = calendar.events.first().alarms.first()
        assertEquals(AlarmAction.EMAIL, alarm.action)
        assertEquals("Reminder: Meeting Soon", alarm.summary)
        assertEquals("Your meeting starts in 1 hour", alarm.description)
        assertEquals(listOf("mailto:user@example.com"), alarm.attendees)
    }

    @Test
    fun createEventWithMultipleAlarms() {
        val calendar = vCalendar {
            prodId("-//Test//EN")
            event {
                uid("multi-alarm-001")
                summary("Event with Multiple Alarms")
                dtStart(LocalDateTime(2024, 6, 15, 10, 0))
                alarm {
                    displayAction()
                    triggerBefore(30.minutes)
                    description("30 minutes before")
                }
                alarm {
                    displayAction()
                    triggerBefore(5.minutes)
                    description("5 minutes before")
                }
            }
        }

        val event = calendar.events.first()
        assertEquals(2, event.alarms.size)
    }

    @Test
    fun createEventWithAlarmRelatedToEnd() {
        val calendar = vCalendar {
            prodId("-//Test//EN")
            event {
                uid("end-alarm-001")
                summary("Event with End Alarm")
                dtStart(LocalDateTime(2024, 6, 15, 10, 0))
                dtEnd(LocalDateTime(2024, 6, 15, 11, 0))
                alarm {
                    displayAction()
                    triggerAfterEnd(0.minutes)
                    description("Meeting has ended")
                }
            }
        }

        val alarm = calendar.events.first().alarms.first()
        val trigger = alarm.trigger as AlarmTrigger.Relative
        assertEquals(AlarmTrigger.RelatedTo.END, trigger.relatedTo)
    }

    @Test
    fun createEventWithRepeatingAlarm() {
        val calendar = vCalendar {
            prodId("-//Test//EN")
            event {
                uid("repeat-alarm-001")
                summary("Event with Repeating Alarm")
                dtStart(LocalDateTime(2024, 6, 15, 10, 0))
                alarm {
                    displayAction()
                    triggerBefore(30.minutes)
                    description("Reminder")
                    repeat(3, 5.minutes)
                }
            }
        }

        val alarm = calendar.events.first().alarms.first()
        assertEquals(3, alarm.repeat)
        assertEquals(5.minutes, alarm.repeatDuration)
    }

    @Test
    fun createEventWithXProperty() {
        val calendar = vCalendar {
            prodId("-//Test//EN")
            event {
                uid("xprop-001")
                summary("Event with X-Property")
                xProperty("X-CUSTOM-FIELD", "custom-value")
            }
        }

        val event = calendar.events.first()
        val customProp = event.properties.find { it.name == "X-CUSTOM-FIELD" }
        assertNotNull(customProp)
        assertEquals("custom-value", customProp.value)
        assertTrue(customProp.isExtension)
    }

    @Test
    fun createCalendarWithCalScale() {
        val calendar = vCalendar {
            prodId("-//Test//EN")
            calScale("GREGORIAN")
        }

        assertEquals("GREGORIAN", calendar.calScale)
    }

    @Test
    fun createCalendarWithMethod() {
        val calendar = vCalendar {
            prodId("-//Test//EN")
            method("PUBLISH")
        }

        assertEquals("PUBLISH", calendar.method)
    }

    @Test
    fun eventHasAutoGeneratedUidAndDtStamp() {
        val calendar = vCalendar {
            prodId("-//Test//EN")
            event {
                summary("Auto Generated Fields")
            }
        }

        val event = calendar.events.first()
        assertNotNull(event.uid)
        assertNotNull(event.dtStamp)
    }

    @Test
    fun overwritingPropertyReplacesIt() {
        val calendar = vCalendar {
            prodId("-//Test//EN")
            event {
                uid("first-uid")
                uid("second-uid")
                summary("First")
                summary("Second")
            }
        }

        val event = calendar.events.first()
        assertEquals("second-uid", event.uid)
        assertEquals("Second", event.summary)
        assertEquals(1, event.properties.count { it.name == "UID" })
        assertEquals(1, event.properties.count { it.name == "SUMMARY" })
    }

    @Test
    fun `vCalendar can build a VJournal with correct properties`() {
        val calendar = vCalendar {
            journal {
                summary("My Journal Entry")
                description("Reflecting on today's achievements")
                dtStartDate(LocalDate(2026, 2, 2))
                status(JournalStatus.FINAL)
                classType(JournalClass.PRIVATE)
                attendee("alice@example.com", "Alice")
                xProperty("X-CUSTOM", "CustomValue")
            }
        }

        assertEquals(1, calendar.components.size)
        val journal = calendar.components.first()
        assertTrue(journal is VJournal)

        val props = journal.properties.associateBy { it.name }

        assertEquals("My Journal Entry", props["SUMMARY"]?.value)
        assertEquals("Reflecting on today's achievements", props["DESCRIPTION"]?.value)
        assertEquals("FINAL", props["STATUS"]?.value)
        assertEquals("PRIVATE", props["CLASS"]?.value)

        val attendee = props["ATTENDEE"]
        assertEquals("mailto:alice@example.com", attendee?.value)
        assertEquals(listOf("Alice"), attendee?.parameters?.get("CN"))

        val xProp = props["X-CUSTOM"]
        assertEquals("CustomValue", xProp?.value)

        // DTSTAMP and UID should exist
        assertTrue(props.containsKey("DTSTAMP"))
        assertTrue(props.containsKey("UID"))
    }

    @Test
    fun `vCalendar can build a VTodo with correct properties`() {
        val start = LocalDateTime(2026, 2, 2, 9, 0)
        val due = LocalDateTime(2026, 2, 5, 17, 0)

        val calendar = vCalendar {
            todo {
                summary("Finish Kotlin DSL")
                description("Implement VTODO support")
                dtStart(start)
                dtDue(due)
                status(TodoStatus.NEEDS_ACTION)
                priority(1)
                percentComplete(50)
                categories("Work", "Kotlin")
                attendee("bob@example.com", "Bob")
                xProperty("X-CUSTOM-TODO", "CustomValue")
            }
        }

        assertEquals(1, calendar.components.size)
        val todo = calendar.components.first()
        assertTrue(todo is VTodo)

        val props = todo.properties.associateBy { it.name }

        assertEquals("Finish Kotlin DSL", props["SUMMARY"]?.value)
        assertEquals("Implement VTODO support", props["DESCRIPTION"]?.value)
        assertEquals("NEEDS_ACTION", props["STATUS"]?.value)
        assertEquals("1", props["PRIORITY"]?.value)
        assertEquals("50", props["PERCENT-COMPLETE"]?.value)
        assertEquals("Work,Kotlin", props["CATEGORIES"]?.value)

        val attendee = props["ATTENDEE"]
        assertEquals("mailto:bob@example.com", attendee?.value)
        assertEquals(listOf("Bob"), attendee?.parameters?.get("CN"))

        val xProp = props["X-CUSTOM-TODO"]
        assertEquals("CustomValue", xProp?.value)

        // DTSTAMP and UID should exist
        assertTrue(props.containsKey("DTSTAMP"))
        assertTrue(props.containsKey("UID"))

        // DTSTART and DUE should be set
        val dtStart = props["DTSTART"]
        val dtDue = props["DUE"]
        assertEquals(start.toInstant(TimeZone.UTC), dtStart?.instant)
        assertEquals(due.toInstant(TimeZone.UTC), dtDue?.instant)
    }
}
