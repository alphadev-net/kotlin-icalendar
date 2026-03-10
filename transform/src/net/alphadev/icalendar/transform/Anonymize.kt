package net.alphadev.icalendar.transform

import net.alphadev.icalendar.model.ICalProperty
import net.alphadev.icalendar.model.VEvent

private val PrivacyRedacted = setOf(
    "SUMMARY",
    "DESCRIPTION",
    "LOCATION",
    "ORGANIZER",
    "ATTENDEE"
)

fun VEvent.anonymize(filter: Set<String> = PrivacyRedacted): VEvent {
    return copy(
        properties = anonymizeProperties(properties, filter),
        components = components,
    )
}

private fun anonymizeProperties(properties: List<ICalProperty>, filterList: Set<String>) = properties
    .filterNot { it.name in filterList }
