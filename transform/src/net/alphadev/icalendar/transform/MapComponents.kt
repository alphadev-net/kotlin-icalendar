package net.alphadev.icalendar.transform

import net.alphadev.icalendar.model.ICalComponent
import net.alphadev.icalendar.model.VCalendar

fun VCalendar.mapComponents(transform: (ICalComponent) -> ICalComponent?): VCalendar {
    return copy(components = components.mapNotNull(transform))
}

fun VCalendar.flatMapComponents(transform: (ICalComponent) -> List<ICalComponent>): VCalendar {
    return copy(components = components.flatMap(transform))
}
