package net.alphadev.icalendar.transform

import net.alphadev.icalendar.model.ICalProperty
import net.alphadev.icalendar.model.VEvent

fun VEvent.anonymize(): VEvent {
    return copy(
        properties = anonymizeProperties(properties),
        components = components,
    )
}

private fun anonymizeProperties(properties: List<ICalProperty>) = properties
    .filterNot { it.name == "SUMMARY" }
    .filterNot { it.name == "DESCRIPTION" }
    .filterNot { it.name == "LOCATION" }
