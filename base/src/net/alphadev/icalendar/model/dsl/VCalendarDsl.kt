package net.alphadev.icalendar.model.dsl

import net.alphadev.icalendar.model.VCalendar

public enum class EventStatus { TENTATIVE, CONFIRMED, CANCELLED }
public enum class Transparency { OPAQUE, TRANSPARENT }

@ICalDsl
public class VCalendarBuilder: IComponentBuilder() {

    init { property("VERSION", "2.0") }

    public fun prodId(value: String) {
        property("PRODID", value)
    }

    public fun calScale(value: String) {
        property("CALSCALE", value)
    }

    public fun method(value: String) {
        property("METHOD", value)
    }

    public fun event(block: VEventBuilder.() -> Unit) {
        components.add(VEventBuilder().apply(block).build())
    }

    public fun journal(block: VJournalBuilder.() -> Unit) {
        components.add(VJournalBuilder().apply(block).build())
    }

    public fun todo(block: VTodoBuilder.() -> Unit) {
        components.add(VTodoBuilder().apply(block).build())
    }

    public fun freeBusy(block: VFreeBusyBuilder.() -> Unit) {
        components.add(VFreeBusyBuilder().apply(block).build())
    }

    public fun build(): VCalendar {
        return VCalendar(properties.toList(), components.toList())
    }
}
