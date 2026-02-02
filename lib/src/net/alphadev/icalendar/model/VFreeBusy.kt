package net.alphadev.icalendar.model

import kotlin.time.Instant

data class VFreeBusy(
    override val properties: List<ICalProperty>,
    override val components: List<ICalComponent>
) : ICalComponent {
    override val componentName: String = NAME

    companion object {
        const val NAME = "VFREEBUSY"
    }
}

// Common properties
val VFreeBusy.uid: String?
    get() = properties.firstOrNull { it.name == "UID" }?.value

val VFreeBusy.dtStartProperty: ICalProperty?
    get() = properties.firstOrNull { it.name == "DTSTART" }

val VFreeBusy.dtEndProperty: ICalProperty?
    get() = properties.firstOrNull { it.name == "DTEND" }

val VFreeBusy.dtStart: Instant?
    get() = dtStartProperty?.instant

val VFreeBusy.dtEnd: Instant?
    get() = dtEndProperty?.instant

val VFreeBusy.organizer: String?
    get() = properties.firstOrNull { it.name == "ORGANIZER" }?.value

val VFreeBusy.attendees: List<String>
    get() = properties.filter { it.name == "ATTENDEE" }.map { it.value }

val VFreeBusy.url: String?
    get() = properties.firstOrNull { it.name == "URL" }?.value

val VFreeBusy.freeBusyPeriods: List<String>
    get() = properties.filter { it.name == "FREEBUSY" }.map { it.value }
