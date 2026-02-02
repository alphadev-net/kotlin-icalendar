package net.alphadev.icalendar.model.dsl

import net.alphadev.icalendar.model.ICalComponent
import net.alphadev.icalendar.model.ICalProperty
import net.alphadev.icalendar.model.VCalendar

enum class EventStatus { TENTATIVE, CONFIRMED, CANCELLED }
enum class Transparency { OPAQUE, TRANSPARENT }

@ICalDsl
class VCalendarBuilder {
    private val properties = mutableListOf<ICalProperty>()
    private val components = mutableListOf<ICalComponent>()

    init { property("VERSION", "2.0") }

    fun prodId(value: String) = property("PRODID", value)
    fun calScale(value: String) = property("CALSCALE", value)
    fun method(value: String) = property("METHOD", value)

    fun property(name: String, value: String, parameters: Map<String, List<String>> = emptyMap()) {
        properties.removeAll { it.name.equals(name, ignoreCase = true) }
        properties.add(ICalProperty(name, parameters, value))
    }

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
