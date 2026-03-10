package net.alphadev.icalendar.transform

import net.alphadev.icalendar.model.ICalProperty
import net.alphadev.icalendar.model.VEvent
import net.alphadev.icalendar.model.dsl.ICalDsl

private val PrivacyRedacted = setOf(
    "SUMMARY",
    "DESCRIPTION",
    "LOCATION",
    "ORGANIZER",
    "ATTENDEE"
)

public fun VEvent.anonymize(filter: Set<String> = PrivacyRedacted): VEvent {
    return copy(
        properties = anonymizeProperties(properties, filter),
        components = components,
    )
}

@ICalDsl
public class AnonymizeFilter {
    private val properties = mutableSetOf<String>()

    public fun summary(): Boolean {
        return properties.add("SUMMARY")
    }

    public fun description(): Boolean {
        return properties.add("DESCRIPTION")
    }

    public fun location(): Boolean {
        return properties.add("LOCATION")
    }

    public fun organizer(): Boolean {
        return properties.add("ORGANIZER")
    }

    public fun attendee(): Boolean {
        return properties.add("ATTENDEE")
    }

    public fun privacyRedacted(): Boolean {
        return properties.addAll(PrivacyRedacted)
    }

    internal fun build(): Set<String> = properties.toSet()
}

public fun VEvent.anonymize(block: AnonymizeFilter.() -> Unit): VEvent =
    anonymize(AnonymizeFilter().apply(block).build())

private fun anonymizeProperties(properties: List<ICalProperty>, filterList: Set<String>) = properties
    .filterNot { it.name in filterList }
