package net.alphadev.icalendar.transform

import net.alphadev.icalendar.model.*
import net.alphadev.icalendar.model.dsl.vCalendar
import kotlin.test.*

class MapTypeTest {

    @Test
    fun mapTypeOnlyTransformsSpecifiedType() {
        val cal = vCalendar {
            event { uid("1"); summary("Event 1") }
            event { uid("2"); summary("Event 2") }
            todo { uid("3"); summary("Todo 1") }
            journal { uid("4"); summary("Journal 1") }
        }

        val result = cal.mapType<VEvent> { event ->
            event.copy(
                properties = event.properties.map {
                    if (it.name == "SUMMARY") it.copy(value = "${it.value} - Modified")
                    else it
                }
            )
        }

        assertEquals("Event 1 - Modified", result.events[0].summary)
        assertEquals("Event 2 - Modified", result.events[1].summary)
        assertEquals("Todo 1", result.todos[0].summary)
        assertEquals("Journal 1", result.journals[0].summary)
    }

    @Test
    fun mapTypeCanFilterSpecificType() {
        val cal = vCalendar {
            event { uid("1"); summary("Keep") }
            event { uid("2"); summary("Remove") }
            todo { uid("3"); summary("Todo") }
        }

        val result = cal.mapType<VEvent> { event ->
            if (event.summary == "Remove") null else event
        }

        assertEquals(1, result.events.size)
        assertEquals("Keep", result.events[0].summary)
        assertEquals(1, result.todos.size)
    }

    @Test
    fun mapTypeWorksWithVTodo() {
        val cal = vCalendar {
            event { uid("1"); summary("Event") }
            todo { uid("2"); summary("Todo 1") }
            todo { uid("3"); summary("Todo 2") }
        }

        val result = cal.mapType<VTodo> { todo ->
            todo.copy(
                properties = todo.properties.map {
                    if (it.name == "SUMMARY") it.copy(value = "${it.value} [Updated]")
                    else it
                }
            )
        }

        assertEquals("Event", result.events[0].summary)
        assertEquals("Todo 1 [Updated]", result.todos[0].summary)
        assertEquals("Todo 2 [Updated]", result.todos[1].summary)
    }

    @Test
    fun mapTypeWorksWithVJournal() {
        val cal = vCalendar {
            journal { uid("1"); summary("Journal 1") }
            journal { uid("2"); summary("Journal 2") }
            event { uid("3"); summary("Event") }
        }

        val result = cal.mapType<VJournal> { journal ->
            journal.copy(
                properties = journal.properties.map {
                    if (it.name == "SUMMARY") it.copy(value = "${it.value} *")
                    else it
                }
            )
        }

        assertEquals("Journal 1 *", result.journals[0].summary)
        assertEquals("Journal 2 *", result.journals[1].summary)
        assertEquals("Event", result.events[0].summary)
    }

    @Test
    fun mapTypePreservesCalendarProperties() {
        val cal = vCalendar {
            prodId("-//My Company//EN")
            method("PUBLISH")
            event { uid("1"); summary("Event") }
        }

        val result = cal.mapType<VEvent> { event ->
            event.copy(
                properties = event.properties.map {
                    if (it.name == "SUMMARY") it.copy(value = "Modified")
                    else it
                }
            )
        }

        assertEquals(cal.prodId, result.prodId)
        assertEquals(cal.method, result.method)
    }

    @Test
    fun flatMapTypeCanDuplicateSpecificType() {
        val cal = vCalendar {
            event { uid("1"); summary("Event") }
            todo { uid("2"); summary("Todo") }
        }

        val result = cal.flatMapType<VEvent> { event ->
            listOf(
                event,
                event.copy(
                    properties = event.properties.map {
                        if (it.name == "UID") it.copy(value = "${it.value}-copy")
                        else it
                    }
                )
            )
        }

        assertEquals(2, result.events.size)
        assertEquals("1", result.events[0].uid)
        assertEquals("1-copy", result.events[1].uid)
        assertEquals(1, result.todos.size)
    }

    @Test
    fun flatMapTypeCanSplitComponentIntoMultiple() {
        val cal = vCalendar {
            event { uid("1"); summary("Meeting") }
            todo { uid("2"); summary("Todo") }
        }

        val result = cal.flatMapType<VEvent> { event ->
            listOf(
                event.copy(
                    properties = event.properties.map {
                        if (it.name == "SUMMARY") it.copy(value = "${it.value} - Part 1")
                        else it
                    }
                ),
                event.copy(
                    properties = event.properties.map {
                        if (it.name == "SUMMARY") it.copy(value = "${it.value} - Part 2")
                        else it
                    }
                )
            )
        }

        assertEquals(2, result.events.size)
        assertEquals("Meeting - Part 1", result.events[0].summary)
        assertEquals("Meeting - Part 2", result.events[1].summary)
        assertEquals(1, result.todos.size)
    }

    @Test
    fun flatMapTypeCanFilterSpecificType() {
        val cal = vCalendar {
            event { uid("1"); summary("Keep") }
            event { uid("2"); summary("Remove") }
            todo { uid("3"); summary("Todo") }
        }

        val result = cal.flatMapType<VEvent> { event ->
            if (event.summary == "Remove") emptyList() else listOf(event)
        }

        assertEquals(1, result.events.size)
        assertEquals("Keep", result.events[0].summary)
        assertEquals(1, result.todos.size)
    }

    @Test
    fun flatMapTypeWorksWithVTodo() {
        val cal = vCalendar {
            todo { uid("1"); summary("Todo") }
            event { uid("2"); summary("Event") }
        }

        val result = cal.flatMapType<VTodo> { todo ->
            listOf(todo, todo)
        }

        assertEquals(2, result.todos.size)
        assertEquals(1, result.events.size)
    }

    @Test
    fun flatMapTypePreservesCalendarProperties() {
        val cal = vCalendar {
            prodId("-//My Company//EN")
            method("PUBLISH")
            event { uid("1"); summary("Event") }
        }

        val result = cal.flatMapType<VEvent> { event ->
            listOf(event, event)
        }

        assertEquals(cal.prodId, result.prodId)
        assertEquals(cal.method, result.method)
    }

    @Test
    fun mapTypeCanBeChainedForDifferentTypes() {
        val cal = vCalendar {
            event { uid("1"); summary("Event") }
            todo { uid("2"); summary("Todo") }
            journal { uid("3"); summary("Journal") }
        }

        val result = cal
            .mapType<VEvent> { it.copy(
                properties = it.properties.map { p ->
                    if (p.name == "SUMMARY") p.copy(value = "${p.value} [E]") else p
                }
            )}
            .mapType<VTodo> { it.copy(
                properties = it.properties.map { p ->
                    if (p.name == "SUMMARY") p.copy(value = "${p.value} [T]") else p
                }
            )}

        assertEquals("Event [E]", result.events[0].summary)
        assertEquals("Todo [T]", result.todos[0].summary)
        assertEquals("Journal", result.journals[0].summary)
    }
}
