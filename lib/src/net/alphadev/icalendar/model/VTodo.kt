package net.alphadev.icalendar.model

import kotlin.time.Instant

data class VTodo(
    override val properties: List<ICalProperty>,
    override val components: List<ICalComponent>
) : ICalComponent {
    override val componentName: String = NAME

    companion object {
        const val NAME = "VTODO"
    }
}

// Common properties
val VTodo.uid: String?
    get() = properties.firstOrNull { it.name == "UID" }?.value

val VTodo.summary: String?
    get() = properties.firstOrNull { it.name == "SUMMARY" }?.value

val VTodo.description: String?
    get() = properties.firstOrNull { it.name == "DESCRIPTION" }?.value

val VTodo.dtStartProperty: ICalProperty?
    get() = properties.firstOrNull { it.name == "DTSTAMP" }

val VTodo.dueProperty: ICalProperty?
    get() = properties.firstOrNull { it.name == "DUE" }

val VTodo.completedProperty: ICalProperty?
    get() = properties.firstOrNull { it.name == "COMPLETED" }

val VTodo.dtStart: Instant?
    get() = dtStartProperty?.instant

val VTodo.due: Instant?
    get() = dueProperty?.instant

val VTodo.completed: Instant?
    get() = completedProperty?.instant

val VTodo.percentComplete: Int?
    get() = properties.firstOrNull { it.name == "PERCENT-COMPLETE" }?.value?.toIntOrNull()

val VTodo.status: String?
    get() = properties.firstOrNull { it.name == "STATUS" }?.value

val VTodo.priority: Int?
    get() = properties.firstOrNull { it.name == "PRIORITY" }?.value?.toIntOrNull()

val VTodo.location: String?
    get() = properties.firstOrNull { it.name == "LOCATION" }?.value

val VTodo.organizer: String?
    get() = properties.firstOrNull { it.name == "ORGANIZER" }?.value

val VTodo.categories: List<String>
    get() = properties.firstOrNull { it.name == "CATEGORIES" }?.value?.split(",")?.map { it.trim() } ?: emptyList()

val VTodo.alarms: List<VAlarm>
    get() = components.filterIsInstance<VAlarm>()

val VTodo.attendees: List<String>
    get() = properties.filter { it.name == "ATTENDEE" }.map { it.value }
