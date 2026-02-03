package net.alphadev.icalendar.transform

import net.alphadev.icalendar.model.*
import net.alphadev.icalendar.model.dsl.vCalendar
import kotlin.test.*
import kotlin.time.Duration.Companion.minutes

class VEventRemoveAlarmsTest {

    @Test
    fun removeAlarmsRemovesAllAlarms() {
        val cal = vCalendar {
            event {
                uid("test-1")
                summary("Meeting")
                alarm {
                    displayAction()
                    triggerBefore(15.minutes)
                }
                alarm {
                    audioAction()
                    triggerBefore(5.minutes)
                }
            }
        }

        val result = cal.events.first().removeAlarms()

        assertEquals(0, result.alarms.size)
    }

    @Test
    fun removeAlarmsOnEventWithoutAlarms() {
        val cal = vCalendar {
            event {
                uid("test-1")
                summary("Meeting")
            }
        }

        val result = cal.events.first().removeAlarms()

        assertEquals(0, result.alarms.size)
    }

    @Test
    fun removeAlarmsPreservesEventProperties() {
        val cal = vCalendar {
            event {
                uid("test-1")
                summary("Meeting")
                description("Important meeting")
                location("Conference Room")
                alarm {
                    displayAction()
                    triggerBefore(15.minutes)
                }
            }
        }

        val original = cal.events.first()
        val result = original.removeAlarms()

        assertEquals(original.uid, result.uid)
        assertEquals(original.summary, result.summary)
        assertEquals(original.description, result.description)
        assertEquals(original.location, result.location)
    }
}
