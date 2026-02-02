package net.alphadev.icalendar.model.dsl

import net.alphadev.icalendar.model.VCalendar

fun vCalendar(block: VCalendarBuilder.() -> Unit): VCalendar {
    return VCalendarBuilder().apply(block).build()
}

@DslMarker
annotation class ICalDsl
