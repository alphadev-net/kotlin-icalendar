package net.alphadev.icalendar.model.dsl

import net.alphadev.icalendar.model.ICalComponent
import net.alphadev.icalendar.model.VEvent

public fun VEvent.builder(block: (VEventBuilder) -> Unit): VEvent {
    return VEventBuilder(this.data()).apply(block).build()
}

private fun ICalComponent.data(): IComponentBuilder {
    val source = this
    return IComponentBuilder(
        components = source.components,
        properties = source.properties
    )
}
