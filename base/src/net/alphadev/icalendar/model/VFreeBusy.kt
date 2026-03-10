package net.alphadev.icalendar.model

import kotlin.time.Instant

public enum class FreeBusyType { BUSY, FREE, BUSY_UNAVAILABLE, BUSY_TENTATIVE }

public data class VFreeBusy(
    override val properties: List<ICalProperty>,
    override val components: List<ICalComponent>
) : ICalComponent {
    override val componentName: String = NAME

    public companion object {
        internal const val NAME = "VFREEBUSY"
    }
}

// Common properties
public val VFreeBusy.uid: String?
    get() = properties.firstOrNull { it.name == "UID" }?.value

public val VFreeBusy.dtStartProperty: ICalProperty?
    get() = properties.firstOrNull { it.name == "DTSTART" }

public val VFreeBusy.dtEndProperty: ICalProperty?
    get() = properties.firstOrNull { it.name == "DTEND" }

public val VFreeBusy.dtStampProperty: ICalProperty?
    get() = properties.firstOrNull { it.name == "DTSTAMP" }

public val VFreeBusy.dtStart: Instant?
    get() = dtStartProperty?.instant

public val VFreeBusy.dtEnd: Instant?
    get() = dtEndProperty?.instant

public val VFreeBusy.dtStamp: Instant?
    get() = dtStampProperty?.instant

public val VFreeBusy.organizer: String?
    get() = properties.firstOrNull { it.name == "ORGANIZER" }?.value

public val VFreeBusy.attendees: List<String>
    get() = properties.filter { it.name == "ATTENDEE" }.map { it.value }

public val VFreeBusy.url: String?
    get() = properties.firstOrNull { it.name == "URL" }?.value

public val VFreeBusy.freeBusyPeriods: List<String>
    get() = properties.filter { it.name == "FREEBUSY" }.map { it.value }
