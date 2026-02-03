package net.alphadev.icalendar.filter

import net.alphadev.icalendar.model.*
import net.alphadev.icalendar.model.dsl.vCalendar
import kotlin.test.*

class CalendarFilterTest {

    @Test
    fun filterRemovesComponentsThatDontMatchCondition() {
        val cal = vCalendar {
            event { uid("1"); summary("Event 1") }
            event { uid("2"); summary("Event 2") }
            event { uid("3"); summary("Event 3") }
        }

        val result = cal.filter { (it as? VEvent)?.uid == "2" }

        assertEquals(1, result.components.size)
        assertEquals("2", (result.components[0] as VEvent).uid)
    }

    @Test
    fun filterKeepsAllComponentsWhenAllMatch() {
        val cal = vCalendar {
            event { uid("1") }
            event { uid("2") }
        }

        val result = cal.filter { it is VEvent }

        assertEquals(2, result.components.size)
    }

    @Test
    fun filterReturnsEmptyWhenNoneMatch() {
        val cal = vCalendar {
            event { uid("1") }
            event { uid("2") }
        }

        val result = cal.filter { false }

        assertTrue(result.components.isEmpty())
    }

    @Test
    fun filterEventsOnlyFiltersVEventComponents() {
        val cal = vCalendar {
            event { uid("1") }
            todo { uid("2") }
            event { uid("3") }
        }

        val result = cal.filterEvents { it.uid == "3" }

        assertEquals(2, result.components.size)
        assertTrue(result.components.any { it is VTodo })
        assertTrue(result.components.filterIsInstance<VEvent>().all { it.uid == "3" })
    }

    @Test
    fun filterEventsKeepsNonEventComponents() {
        val cal = vCalendar {
            event { uid("1") }
            todo { uid("2") }
            journal { uid("3") }
        }

        val result = cal.filterEvents { it.uid == "1" }

        assertEquals(3, result.components.size)
        assertEquals(1, result.events.size)
        assertEquals(1, result.todos.size)
        assertEquals(1, result.journals.size)
    }

    @Test
    fun filterTodosOnlyFiltersVTodoComponents() {
        val cal = vCalendar {
            todo { uid("1") }
            event { uid("2") }
            todo { uid("3") }
        }

        val result = cal.filterTodos { it.uid == "3" }

        assertEquals(2, result.components.size)
        assertTrue(result.components.any { it is VEvent })
        assertTrue(result.components.filterIsInstance<VTodo>().all { it.uid == "3" })
    }

    @Test
    fun filterJournalsOnlyFiltersVJournalComponents() {
        val cal = vCalendar {
            journal { uid("1") }
            event { uid("2") }
            journal { uid("3") }
        }

        val result = cal.filterJournals { it.uid == "1" }

        assertEquals(2, result.components.size)
        assertTrue(result.components.any { it is VEvent })
        assertTrue(result.components.filterIsInstance<VJournal>().all { it.uid == "1" })
    }

    @Test
    fun filterBySummaryProperty() {
        val cal = vCalendar {
            event { uid("1"); summary("Meeting") }
            event { uid("2"); summary("Lunch") }
            event { uid("3"); summary("Meeting") }
        }

        val result = cal.filterEvents { it.summary == "Meeting" }

        assertEquals(2, result.events.size)
        assertTrue(result.events.all { it.summary == "Meeting" })
    }
}
