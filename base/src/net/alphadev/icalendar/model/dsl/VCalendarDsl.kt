package net.alphadev.icalendar.model.dsl

import net.alphadev.icalendar.model.VCalendar

public enum class EventStatus { TENTATIVE, CONFIRMED, CANCELLED }
public enum class Transparency { OPAQUE, TRANSPARENT }

@ICalDsl
public class VCalendarBuilder {

    private val builderState = IComponentBuilder()

    init {
        version("2.0")
    }

    public fun version(value: String) {
        builderState.property("VERSION", value)
    }

    public fun summary(value: String) {
        builderState.property("SUMMARY", value)
    }

    public fun prodId(value: String) {
        builderState.property("PRODID", value)
    }

    public fun calScale(value: String) {
        builderState.property("CALSCALE", value)
    }

    public fun method(value: String) {
        builderState.property("METHOD", value)
    }

    public fun event(block: VEventBuilder.() -> Unit) {
        builderState.components.add(VEventBuilder().apply(block).build())
    }

    public fun journal(block: VJournalBuilder.() -> Unit) {
        builderState.components.add(VJournalBuilder().apply(block).build())
    }

    public fun todo(block: VTodoBuilder.() -> Unit) {
        builderState.components.add(VTodoBuilder().apply(block).build())
    }

    public fun freeBusy(block: VFreeBusyBuilder.() -> Unit) {
        builderState.components.add(VFreeBusyBuilder().apply(block).build())
    }

    fun build(): VCalendar {
        return VCalendar(builderState.properties.toList(), builderState.components.toList())
    }
}
