package net.alphadev.icalendar.model

import kotlin.time.Instant

public data class VJournal(
    override val properties: List<ICalProperty>,
    override val components: List<ICalComponent>
) : ICalComponent {
    override val componentName: String = NAME

    companion object {
        const val NAME = "VJOURNAL"
    }
}

// Common properties
public val VJournal.uid: String?
    get() = properties.firstOrNull { it.name == "UID" }?.value

public val VJournal.summary: String?
    get() = properties.firstOrNull { it.name == "SUMMARY" }?.value

public val VJournal.description: String?
    get() = properties.firstOrNull { it.name == "DESCRIPTION" }?.value

public val VJournal.dtStartProperty: ICalProperty?
    get() = properties.firstOrNull { it.name == "DTSTART" }

public val VJournal.dtStart: Instant?
    get() = dtStartProperty?.instant

public val VJournal.status: String?
    get() = properties.firstOrNull { it.name == "STATUS" }?.value

public val VJournal.organizer: String?
    get() = properties.firstOrNull { it.name == "ORGANIZER" }?.value

public val VJournal.categories: List<String>
    get() = properties.firstOrNull { it.name == "CATEGORIES" }?.value?.split(",")?.map { it.trim() } ?: emptyList()

public val VJournal.attachments: List<String>
    get() = properties.filter { it.name == "ATTACH" }.map { it.value }

public val VJournal.attendees: List<String>
    get() = properties.filter { it.name == "ATTENDEE" }.map { it.value }
