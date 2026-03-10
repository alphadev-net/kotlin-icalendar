package net.alphadev.icalendar.model

import kotlin.time.Instant

public data class VTodo(
    override val properties: List<ICalProperty>,
    override val components: List<ICalComponent>
) : ICalComponent {
    override val componentName: String = NAME

    public companion object {
        internal const val NAME = "VTODO"
    }
}

// Common properties
public val VTodo.uid: String?
    get() = properties.firstOrNull { it.name == "UID" }?.value

public val VTodo.summary: String?
    get() = properties.firstOrNull { it.name == "SUMMARY" }?.value

public val VTodo.description: String?
    get() = properties.firstOrNull { it.name == "DESCRIPTION" }?.value

public val VTodo.dtStartProperty: ICalProperty?
    get() = properties.firstOrNull { it.name == "DTSTAMP" }

public val VTodo.dueProperty: ICalProperty?
    get() = properties.firstOrNull { it.name == "DUE" }

public val VTodo.completedProperty: ICalProperty?
    get() = properties.firstOrNull { it.name == "COMPLETED" }

public val VTodo.dtStart: Instant?
    get() = dtStartProperty?.instant

public val VTodo.due: Instant?
    get() = dueProperty?.instant

public val VTodo.completed: Instant?
    get() = completedProperty?.instant

public val VTodo.percentComplete: Int?
    get() = properties.firstOrNull { it.name == "PERCENT-COMPLETE" }?.value?.toIntOrNull()

public val VTodo.status: String?
    get() = properties.firstOrNull { it.name == "STATUS" }?.value

public val VTodo.priority: Int?
    get() = properties.firstOrNull { it.name == "PRIORITY" }?.value?.toIntOrNull()

public val VTodo.location: String?
    get() = properties.firstOrNull { it.name == "LOCATION" }?.value

public val VTodo.organizer: String?
    get() = properties.firstOrNull { it.name == "ORGANIZER" }?.value

public val VTodo.categories: List<String>
    get() = properties.firstOrNull { it.name == "CATEGORIES" }?.value?.split(",")?.map { it.trim() } ?: emptyList()

public val VTodo.alarms: List<VAlarm>
    get() = components.filterIsInstance<VAlarm>()

public val VTodo.attendees: List<String>
    get() = properties.filter { it.name == "ATTENDEE" }.map { it.value }
