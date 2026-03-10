package net.alphadev.icalendar.transform

import net.alphadev.icalendar.model.ICalProperty
import net.alphadev.icalendar.model.VEvent

fun VEvent.anonymize(): VEvent {
    return copy(
        properties = anonymizeProperties(properties),
        components = components,
    )
}

private val ANONYMIZED_PROPERTIES = setOf(
    "SUMMARY",
    "DESCRIPTION",
    "LOCATION",
    "ORGANIZER",
    "ATTENDEE"
)

private fun anonymizeProperties(properties: List<ICalProperty>) = properties
    .filterNot { it.name in ANONYMIZED_PROPERTIES }
