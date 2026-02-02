package net.alphadev.icalendar.model.dsl

import net.alphadev.icalendar.model.VCalendar

enum class EventStatus { TENTATIVE, CONFIRMED, CANCELLED }
enum class Transparency { OPAQUE, TRANSPARENT }

@ICalDsl
class VCalendarBuilder: IComponentBuilder() {

    init { property("VERSION", "2.0") }

    fun prodId(value: String) = property("PRODID", value)
    fun calScale(value: String) = property("CALSCALE", value)
    fun method(value: String) = property("METHOD", value)

    fun event(block: VEventBuilder.() -> Unit) {
        components.add(VEventBuilder().apply(block).build())
    }

    fun journal(block: VJournalBuilder.() -> Unit) {
        components.add(VJournalBuilder().apply(block).build())
    }

    fun todo(block: VTodoBuilder.() -> Unit) {
        components.add(VTodoBuilder().apply(block).build())
    }

    fun build(): VCalendar = VCalendar(properties.toList(), components.toList())
}
