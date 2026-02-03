package net.alphadev.icalendar.transform

import net.alphadev.icalendar.model.VAlarm
import net.alphadev.icalendar.model.VEvent
import net.alphadev.icalendar.model.dsl.VAlarmBuilder

fun VEvent.addAlarm(alarm: VAlarm): VEvent {
    return copy(components = components + alarm)
}

fun VEvent.addAlarm(block: VAlarmBuilder.() -> Unit): VEvent {
    val alarm = VAlarmBuilder().apply(block).build()
    return copy(components = components + alarm)
}
