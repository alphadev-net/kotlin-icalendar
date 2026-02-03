package net.alphadev.icalendar.transform

import net.alphadev.icalendar.model.*
import net.alphadev.icalendar.model.dsl.vCalendar
import kotlin.test.*

class MapPropertiesTest {

    @Test
    fun mapPropertiesCanTransformEachProperty() {
        val cal = vCalendar {
            event {
                uid("1")
                summary("Meeting")
                description("Important")
            }
        }

        val result = cal.events.first().mapProperties { property ->
            if (property.name == "SUMMARY") {
                property.copy(value = "${property.value} - Modified")
            } else {
                property
            }
        }

        assertEquals("Meeting - Modified", result.summary)
        assertEquals("Important", result.description)
    }

    @Test
    fun mapPropertiesCanFilterOutProperties() {
        val cal = vCalendar {
            event {
                uid("1")
                summary("Meeting")
                description("Important")
                location("Room 101")
            }
        }

        val result = cal.events.first().mapProperties { property ->
            if (property.name == "DESCRIPTION") null else property
        }

        assertEquals("Meeting", result.summary)
        assertNull(result.description)
        assertEquals("Room 101", result.location)
    }

    @Test
    fun mapPropertiesWorksOnVTodo() {
        val cal = vCalendar {
            todo {
                uid("1")
                summary("Task")
                description("Do this")
            }
        }

        val result = cal.todos.first().mapProperties { property ->
            if (property.name == "SUMMARY") {
                property.copy(value = "Updated ${property.value}")
            } else {
                property
            }
        }

        assertEquals("Updated Task", result.summary)
    }

    @Test
    fun mapPropertiesWorksOnVJournal() {
        val cal = vCalendar {
            journal {
                uid("1")
                summary("Entry")
                description("Journal entry")
            }
        }

        val result = cal.journals.first().mapProperties { property ->
            if (property.name == "SUMMARY") {
                property.copy(value = "${property.value} [Modified]")
            } else {
                property
            }
        }

        assertEquals("Entry [Modified]", result.summary)
    }

    @Test
    fun mapPropertiesWorksOnVCalendar() {
        val cal = vCalendar {
            prodId("-//My Company//EN")
            method("PUBLISH")
        }

        val result = cal.mapProperties { property ->
            if (property.name == "PRODID") {
                property.copy(value = "-//Updated//EN")
            } else {
                property
            }
        }

        assertEquals("-//Updated//EN", result.prodId)
        assertEquals("PUBLISH", result.method)
    }

    @Test
    fun mapPropertiesPreservesComponents() {
        val cal = vCalendar {
            event {
                uid("1")
                summary("Meeting")
                alarm {
                    displayAction()
                }
            }
        }

        val event = cal.events.first()
        val result = event.mapProperties { property ->
            if (property.name == "SUMMARY") {
                property.copy(value = "Modified")
            } else {
                property
            }
        }

        assertEquals(1, result.alarms.size)
    }

    @Test
    fun flatMapPropertiesCanDuplicateProperties() {
        val cal = vCalendar {
            event {
                uid("1")
                summary("Meeting")
            }
        }

        val result = cal.events.first().flatMapProperties { property ->
            if (property.name == "SUMMARY") {
                listOf(property, property.copy(value = "${property.value} Copy"))
            } else {
                listOf(property)
            }
        }

        val summaries = result.properties.filter { it.name == "SUMMARY" }
        assertEquals(2, summaries.size)
        assertEquals("Meeting", summaries[0].value)
        assertEquals("Meeting Copy", summaries[1].value)
    }

    @Test
    fun flatMapPropertiesCanFilterOutProperties() {
        val cal = vCalendar {
            event {
                uid("1")
                summary("Meeting")
                description("Important")
            }
        }

        val result = cal.events.first().flatMapProperties { property ->
            if (property.name == "DESCRIPTION") {
                emptyList()
            } else {
                listOf(property)
            }
        }

        assertEquals("Meeting", result.summary)
        assertNull(result.description)
    }

    @Test
    fun flatMapPropertiesCanAddRelatedProperties() {
        val cal = vCalendar {
            event {
                uid("1")
                summary("Meeting")
            }
        }

        val result = cal.events.first().flatMapProperties { property ->
            if (property.name == "SUMMARY") {
                listOf(
                    property,
                    property.copy(name = "X-SUMMARY-BACKUP", value = property.value)
                )
            } else {
                listOf(property)
            }
        }

        assertEquals("Meeting", result.summary)
        val backup = result.properties.firstOrNull { it.name == "X-SUMMARY-BACKUP" }
        assertNotNull(backup)
        assertEquals("Meeting", backup.value)
    }

    @Test
    fun flatMapPropertiesWorksOnVTodo() {
        val cal = vCalendar {
            todo {
                uid("1")
                summary("Task")
            }
        }

        val result = cal.todos.first().flatMapProperties { property ->
            listOf(property, property)
        }

        assertTrue(result.properties.size > cal.todos.first().properties.size)
    }
}
