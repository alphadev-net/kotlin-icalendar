package net.alphadev.icalendar.transform

import net.alphadev.icalendar.model.VCalendar

fun VCalendar.mergeComponents(from: VCalendar): VCalendar {
    return copy(components = components + from.components)
}
