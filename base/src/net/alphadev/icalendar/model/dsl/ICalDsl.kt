package net.alphadev.icalendar.model.dsl

import net.alphadev.icalendar.model.ICalComponent
import net.alphadev.icalendar.model.ICalProperty
import net.alphadev.icalendar.model.VCalendar
import kotlin.time.Instant

fun vCalendar(block: VCalendarBuilder.() -> Unit): VCalendar {
    return VCalendarBuilder().apply(block).build()
}

@DslMarker
annotation class ICalDsl

abstract class IComponentBuilder {
    protected val properties = mutableListOf<ICalProperty>()
    protected val components = mutableListOf<ICalComponent>()

    fun xProperty(name: String, value: String, parameters: Map<String, List<String>> = emptyMap()) {
        require(name.startsWith("X-", ignoreCase = true)) { "Extension properties must start with X-" }
        property(name, value, parameters)
    }

    fun property(name: String, value: String, parameters: Map<String, List<String>> = emptyMap()) {
        properties.removeAll { it.name.equals(name, ignoreCase = true) }
        properties.add(ICalProperty(name.uppercase(), parameters, value))
    }

    protected fun propertyWithInstant(
        name: String,
        value: String,
        parameters: Map<String, List<String>> = emptyMap(),
        instant: Instant? = null
    ) {
        properties.removeAll { it.name.equals(name, ignoreCase = true) }
        properties.add(ICalProperty(name.uppercase(), parameters, value, instant))
    }
}