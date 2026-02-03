package net.alphadev.icalendar.transform

import net.alphadev.icalendar.model.*
import net.alphadev.icalendar.model.dsl.vCalendar
import kotlin.test.*

class MapComponentsTest {

    @Test
    fun mapComponentsCanTransformEachComponent() {
        val cal = vCalendar {
            event { uid("1"); summary("Event 1") }
            event { uid("2"); summary("Event 2") }
            todo { uid("3"); summary("Todo 1") }
        }

        val result = cal.mapComponents { component ->
            when (component) {
                is VEvent -> component.copy(
                    properties = component.properties.map {
                        if (it.name == "SUMMARY") it.copy(value = "${it.value} - Modified")
                        else it
                    }
                )
                else -> component
            }
        }

        assertEquals("Event 1 - Modified", result.events[0].summary)
        assertEquals("Event 2 - Modified", result.events[1].summary)
        assertEquals("Todo 1", result.todos[0].summary)
    }

    @Test
    fun mapComponentsCanFilterOutComponents() {
        val cal = vCalendar {
            event { uid("1"); summary("Keep") }
            event { uid("2"); summary("Remove") }
            event { uid("3"); summary("Keep") }
        }

        val result = cal.mapComponents { component ->
            if ((component as? VEvent)?.summary == "Remove") {
                null
            } else {
                component
            }
        }

        assertEquals(2, result.events.size)
        assertTrue(result.events.all { it.summary == "Keep" })
    }

    @Test
    fun mapComponentsReturnsNullToRemove() {
        val cal = vCalendar {
            event { uid("1") }
            todo { uid("2") }
            journal { uid("3") }
        }

        val result = cal.mapComponents { component ->
            if (component is VTodo) null else component
        }

        assertEquals(1, result.events.size)
        assertEquals(0, result.todos.size)
        assertEquals(1, result.journals.size)
    }

    @Test
    fun mapComponentsPreservesCalendarProperties() {
        val cal = vCalendar {
            prodId("-//My Company//My Product//EN")
            method("PUBLISH")
            event { uid("1") }
        }

        val result = cal.mapComponents { it }

        assertEquals(cal.prodId, result.prodId)
        assertEquals(cal.method, result.method)
    }

    @Test
    fun mapComponentsWithEmptyCalendar() {
        val cal = vCalendar { }

        val result = cal.mapComponents { it }

        assertEquals(0, result.components.size)
    }

    @Test
    fun mapComponentsCanChangeComponentTypes() {
        val cal = vCalendar {
            event { uid("1"); summary("Event") }
        }

        val result = cal.mapComponents { component ->
            if (component is VEvent) {
                VTodo(component.properties, emptyList())
            } else {
                component
            }
        }

        assertEquals(0, result.events.size)
        assertEquals(1, result.todos.size)
        assertEquals("Event", result.todos[0].summary)
    }

    @Test
    fun flatMapComponentsCanDuplicateComponents() {
        val cal = vCalendar {
            event { uid("1"); summary("Event") }
        }

        val result = cal.flatMapComponents { component ->
            listOf(component, component)
        }

        assertEquals(2, result.events.size)
        assertEquals("Event", result.events[0].summary)
        assertEquals("Event", result.events[1].summary)
    }

    @Test
    fun flatMapComponentsCanSplitComponentIntoMultiple() {
        val cal = vCalendar {
            event { uid("1"); summary("Meeting") }
        }

        val result = cal.flatMapComponents { component ->
            if (component is VEvent) {
                listOf(
                    component.copy(properties = component.properties.map {
                        if (it.name == "SUMMARY") it.copy(value = "Part 1")
                        else it
                    }),
                    component.copy(properties = component.properties.map {
                        if (it.name == "SUMMARY") it.copy(value = "Part 2")
                        else it
                    })
                )
            } else {
                listOf(component)
            }
        }

        assertEquals(2, result.events.size)
        assertEquals("Part 1", result.events[0].summary)
        assertEquals("Part 2", result.events[1].summary)
    }

    @Test
    fun flatMapComponentsCanFilterOutComponents() {
        val cal = vCalendar {
            event { uid("1"); summary("Keep") }
            event { uid("2"); summary("Remove") }
            event { uid("3"); summary("Keep") }
        }

        val result = cal.flatMapComponents { component ->
            if ((component as? VEvent)?.summary == "Remove") {
                emptyList()
            } else {
                listOf(component)
            }
        }

        assertEquals(2, result.events.size)
        assertTrue(result.events.all { it.summary == "Keep" })
    }

    @Test
    fun flatMapComponentsCanMixOperations() {
        val cal = vCalendar {
            event { uid("1"); summary("Duplicate") }
            event { uid("2"); summary("Remove") }
            event { uid("3"); summary("Keep") }
        }

        val result = cal.flatMapComponents { component ->
            when ((component as? VEvent)?.summary) {
                "Duplicate" -> listOf(component, component)
                "Remove" -> emptyList()
                else -> listOf(component)
            }
        }

        assertEquals(3, result.events.size)
        assertEquals("Duplicate", result.events[0].summary)
        assertEquals("Duplicate", result.events[1].summary)
        assertEquals("Keep", result.events[2].summary)
    }

    @Test
    fun flatMapComponentsPreservesCalendarProperties() {
        val cal = vCalendar {
            prodId("-//My Company//My Product//EN")
            method("PUBLISH")
            event { uid("1") }
        }

        val result = cal.flatMapComponents { listOf(it) }

        assertEquals(cal.prodId, result.prodId)
        assertEquals(cal.method, result.method)
    }

    @Test
    fun flatMapComponentsWithEmptyCalendar() {
        val cal = vCalendar { }

        val result = cal.flatMapComponents { listOf(it) }

        assertEquals(0, result.components.size)
    }
}