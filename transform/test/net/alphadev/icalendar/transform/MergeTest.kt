package net.alphadev.icalendar.transform

import net.alphadev.icalendar.model.*
import net.alphadev.icalendar.model.dsl.vCalendar
import kotlin.test.*

class CalendarTransformTest {

    @Test
    fun mergeComponentsCombinesComponentsFromBothCalendars() {
        val cal1 = vCalendar {
            event { uid("1") }
            todo { uid("2") }
        }
        val cal2 = vCalendar {
            event { uid("3") }
            journal { uid("4") }
        }

        val result = cal1.mergeComponents(cal2)

        assertEquals(4, result.components.size)
        assertEquals(2, result.events.size)
        assertEquals(1, result.todos.size)
        assertEquals(1, result.journals.size)
    }

    @Test
    fun mergeComponentsUsesPropertiesFromFirstCalendar() {
        val cal1 = vCalendar {
            prodId("-//My Company//My Product//EN")
            method("PUBLISH")
            event { uid("1") }
        }
        val cal2 = vCalendar {
            prodId("-//Other Company//Other Product//EN")
            method("REQUEST")
            event { uid("2") }
        }

        val result = cal1.mergeComponents(cal2)

        assertEquals("-//My Company//My Product//EN", result.prodId)
        assertEquals("PUBLISH", result.method)
        assertEquals(2, result.events.size)
    }

    @Test
    fun mergeComponentsPreservesOrder() {
        val cal1 = vCalendar {
            event { uid("1") }
            event { uid("2") }
        }
        val cal2 = vCalendar {
            event { uid("3") }
        }

        val result = cal1.mergeComponents(cal2)

        assertEquals(listOf("1", "2", "3"), result.events.map { it.uid })
    }

    @Test
    fun mergeComponentsWithEmptyCalendar() {
        val cal1 = vCalendar {
            event { uid("1") }
        }
        val cal2 = vCalendar { }

        val result = cal1.mergeComponents(cal2)

        assertEquals(1, result.components.size)
    }
}
