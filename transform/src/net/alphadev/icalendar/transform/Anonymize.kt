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

fun VEvent.anonymize(filter: Set<String> = PrivacyRedacted): VEvent {
    return copy(
        properties = anonymizeProperties(properties, filter),
        components = components,
    )
}

@ICalDsl
class AnonymizeFilter {
    private val properties = mutableSetOf<String>()

    fun summary() = properties.add("SUMMARY")
    fun description() = properties.add("DESCRIPTION")
    fun location() = properties.add("LOCATION")
    fun organizer() = properties.add("ORGANIZER")
    fun attendee() = properties.add("ATTENDEE")
    fun privacyRedacted() = properties.addAll(PrivacyRedacted)

    internal fun build(): Set<String> = properties.toSet()
}

fun VEvent.anonymize(block: AnonymizeFilter.() -> Unit): VEvent =
    anonymize(AnonymizeFilter().apply(block).build())

private fun anonymizeProperties(properties: List<ICalProperty>, filterList: Set<String>) = properties
    .filterNot { it.name in filterList }
