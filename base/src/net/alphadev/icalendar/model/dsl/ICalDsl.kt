package net.alphadev.icalendar.model.dsl

import net.alphadev.icalendar.model.ICalComponent
import net.alphadev.icalendar.model.ICalProperty
import net.alphadev.icalendar.model.VCalendar
import kotlin.time.Instant

public fun vCalendar(block: VCalendarBuilder.() -> Unit): VCalendar {
    return VCalendarBuilder().apply(block).build()
}

@DslMarker
annotation class ICalDsl

internal class IComponentBuilder(
    properties: List<ICalProperty> = emptyList(),
    components: List<ICalComponent> = emptyList()
) {
    internal val properties: MutableList<ICalProperty> = mutableListOf<ICalProperty>().apply {
        addAll(properties)
    }

    internal val components: MutableList<ICalComponent> = mutableListOf<ICalComponent>().apply {
        addAll(components)
    }

    internal fun xProperty(name: String, value: String, parameters: Map<String, List<String>> = emptyMap()) {
        val propertyName = if (name.startsWith("X-", ignoreCase = true)) name else "X-$name"
        property(propertyName, value, parameters)
    }

    internal fun property(name: String, value: String, parameters: Map<String, List<String>> = emptyMap()) {
        properties.removeAll { it.name.equals(name, ignoreCase = true) }
        properties.add(ICalProperty(name.uppercase(), parameters, value))
    }

    internal fun propertyWithInstant(
        name: String,
        value: String,
        parameters: Map<String, List<String>> = emptyMap(),
        instant: Instant? = null
    ) {
        properties.removeAll { it.name.equals(name, ignoreCase = true) }
        properties.add(ICalProperty(name.uppercase(), parameters, value, instant))
    }
}
