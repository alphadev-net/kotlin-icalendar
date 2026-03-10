@file:Suppress("NO_EXPLICIT_VISIBILITY_IN_API_MODE_WARNING")
package net.alphadev.icalendar.transform

import net.alphadev.icalendar.model.*
import net.alphadev.icalendar.model.dsl.vCalendar
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

class VEventAlarmTest {

    @Test
    fun addAlarmWithBuilderAddsAlarmToEvent() {
        val cal = vCalendar {
            event {
                uid("test-1")
                summary("Meeting")
            }
        }

        val result = cal.events.first().addAlarm {
            displayAction()
            triggerBefore(15.minutes)
            description("Reminder")
        }

        assertEquals(1, result.alarms.size)
        assertEquals(AlarmAction.DISPLAY, result.alarms.first().action)
        assertEquals("Reminder", result.alarms.first().description)
    }

    @Test
    fun addAlarmPreservesExistingAlarms() {
        val cal = vCalendar {
            event {
                uid("test-1")
                summary("Meeting")
                alarm {
                    displayAction()
                    triggerBefore(30.minutes)
                }
            }
        }

        val result = cal.events.first().addAlarm {
            audioAction()
            triggerBefore(5.minutes)
        }

        assertEquals(2, result.alarms.size)
        assertEquals(AlarmAction.DISPLAY, result.alarms[0].action)
        assertEquals(AlarmAction.AUDIO, result.alarms[1].action)
    }

    @Test
    fun addAlarmCanBeChained() {
        val cal = vCalendar {
            event {
                uid("test-1")
                summary("Meeting")
            }
        }

        val result = cal.events.first()
            .addAlarm {
                displayAction()
                triggerBefore(15.minutes)
            }
            .addAlarm {
                emailAction()
                triggerBefore(30.minutes)
            }
            .addAlarm {
                audioAction()
                triggerBefore(5.minutes)
            }

        assertEquals(3, result.alarms.size)
        assertEquals(AlarmAction.DISPLAY, result.alarms[0].action)
        assertEquals(AlarmAction.EMAIL, result.alarms[1].action)
        assertEquals(AlarmAction.AUDIO, result.alarms[2].action)
    }

    @Test
    fun addAlarmPreservesEventProperties() {
        val cal = vCalendar {
            event {
                uid("test-1")
                summary("Meeting")
                description("Important meeting")
                location("Conference Room")
            }
        }

        val original = cal.events.first()
        val result = original.addAlarm {
            displayAction()
            triggerBefore(15.minutes)
        }

        assertEquals(original.uid, result.uid)
        assertEquals(original.summary, result.summary)
        assertEquals(original.description, result.description)
        assertEquals(original.location, result.location)
    }
}
