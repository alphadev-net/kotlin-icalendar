package net.alphadev.icalendar.filter

import net.alphadev.icalendar.model.*

public fun VCalendar.filter(condition: (ICalComponent) -> Boolean): VCalendar {
    return copy(components = components.filter(condition))
}

public fun VCalendar.filterEvents(condition: (VEvent) -> Boolean): VCalendar {
    return filterType<VEvent>(condition)
}

public fun VCalendar.filterJournals(condition: (VJournal) -> Boolean): VCalendar {
    return filterType<VJournal>(condition)
}

public fun VCalendar.filterTodos(condition: (VTodo) -> Boolean): VCalendar {
    return filterType<VTodo>(condition)
}

private inline fun <reified Type : ICalComponent> VCalendar.filterType(crossinline condition: (Type) -> Boolean): VCalendar =
    filter {
        if (Type::class.isInstance(it)) {
            condition(it as Type)
        } else {
            true
        }
    }
