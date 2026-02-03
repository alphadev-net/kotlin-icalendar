package net.alphadev.icalendar.transform

import net.alphadev.icalendar.model.*

inline fun <reified T : ICalComponent> T.mapProperties(transform: (ICalProperty) -> ICalProperty?): T {
    return when (this) {
        is VEvent -> copy(properties = properties.mapNotNull(transform))
        is VTodo -> copy(properties = properties.mapNotNull(transform))
        is VJournal -> copy(properties = properties.mapNotNull(transform))
        is VCalendar -> copy(properties = properties.mapNotNull(transform))
        is VAlarm -> copy(properties = properties.mapNotNull(transform))
        else -> this
    } as T
}

inline fun <reified T : ICalComponent> T.flatMapProperties(transform: (ICalProperty) -> List<ICalProperty>): T {
    return when (this) {
        is VEvent -> copy(properties = properties.flatMap(transform))
        is VTodo -> copy(properties = properties.flatMap(transform))
        is VJournal -> copy(properties = properties.flatMap(transform))
        is VCalendar -> copy(properties = properties.flatMap(transform))
        is VAlarm -> copy(properties = properties.flatMap(transform))
        else -> this
    } as T
}
