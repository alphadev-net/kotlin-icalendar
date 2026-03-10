package net.alphadev.icalendar.transform

import net.alphadev.icalendar.model.VEvent
import net.alphadev.icalendar.model.dsl.VAlarmBuilder
import net.alphadev.icalendar.model.dsl.builder

public fun VEvent.addAlarm(block: VAlarmBuilder.() -> Unit): VEvent = builder {
    it.alarm(block)
}
