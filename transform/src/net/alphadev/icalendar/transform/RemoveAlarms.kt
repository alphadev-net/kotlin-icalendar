package net.alphadev.icalendar.transform

import net.alphadev.icalendar.model.VAlarm
import net.alphadev.icalendar.model.VEvent

fun VEvent.removeAlarms(): VEvent {
    return copy(components = components.filterNot { it is VAlarm })
}
