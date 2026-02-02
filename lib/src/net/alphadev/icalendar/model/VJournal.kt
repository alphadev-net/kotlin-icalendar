package net.alphadev.icalendar.model

data class VJournal(
    override val properties: List<ICalProperty>,
    override val components: List<ICalComponent>
) : ICalComponent {
    override val componentName: String = NAME

    companion object {
        const val NAME = "VJOURNAL"
    }
}

// Common properties
val VJournal.uid: String?
    get() = properties.firstOrNull { it.name == "UID" }?.value

val VJournal.summary: String?
    get() = properties.firstOrNull { it.name == "SUMMARY" }?.value

val VJournal.description: String?
    get() = properties.firstOrNull { it.name == "DESCRIPTION" }?.value

val VJournal.dtStartProperty: ICalProperty?
    get() = properties.firstOrNull { it.name == "DTSTART" }

val VJournal.status: String?
    get() = properties.firstOrNull { it.name == "STATUS" }?.value

val VJournal.organizer: String?
    get() = properties.firstOrNull { it.name == "ORGANIZER" }?.value

val VJournal.categories: List<String>
    get() = properties.firstOrNull { it.name == "CATEGORIES" }?.value?.split(",")?.map { it.trim() } ?: emptyList()

val VJournal.attachments: List<String>
    get() = properties.filter { it.name == "ATTACH" }.map { it.value }
