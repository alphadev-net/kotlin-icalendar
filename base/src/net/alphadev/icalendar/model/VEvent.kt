package net.alphadev.icalendar.model

import kotlin.time.Instant

public data class VEvent(
    override val properties: List<ICalProperty>,
    override val components: List<ICalComponent> = emptyList()
) : ICalComponent {
    override val componentName: String = NAME

    public companion object {
        internal const val NAME = "VEVENT"
    }
}

public val VEvent.uid: String?
    get() = properties.firstOrNull { it.name == "UID" }?.value

public val VEvent.summary: String?
    get() = properties.firstOrNull { it.name == "SUMMARY" }?.value

public val VEvent.description: String?
    get() = properties.firstOrNull { it.name == "DESCRIPTION" }?.value

public val VEvent.location: String?
    get() = properties.firstOrNull { it.name == "LOCATION" }?.value

public val VEvent.sequence: Int?
    get() = properties.firstOrNull { it.name == "SEQUENCE" }?.value?.toIntOrNull()

public val VEvent.status: String?
    get() = properties.firstOrNull { it.name == "STATUS" }?.value

public val VEvent.transp: String?
    get() = properties.firstOrNull { it.name == "TRANSP" }?.value

public val VEvent.organizer: String?
    get() = properties.firstOrNull { it.name == "ORGANIZER" }?.value

public val VEvent.dtStartProperty: ICalProperty?
    get() = properties.firstOrNull { it.name == "DTSTART" }

public val VEvent.dtEndProperty: ICalProperty?
    get() = properties.firstOrNull { it.name == "DTEND" }

public val VEvent.dtStart: Instant?
    get() = dtStartProperty?.instant

public val VEvent.dtEnd: Instant?
    get() = dtEndProperty?.instant

public val VEvent.dtStamp: Instant?
    get() = properties.firstOrNull { it.name == "DTSTAMP" }?.instant

public val VEvent.created: Instant?
    get() = properties.firstOrNull { it.name == "CREATED" }?.instant

public val VEvent.lastModified: Instant?
    get() = properties.firstOrNull { it.name == "LAST-MODIFIED" }?.instant

public val VEvent.isAllDay: Boolean
    get() = dtStartProperty?.valueType.equals("DATE", ignoreCase = true)
            || (dtStartProperty?.value?.contains("T") == false)

public val VEvent.alarms: List<VAlarm>
    get() = components.filterIsInstance<VAlarm>()

public val VEvent.hasAlarms: Boolean
    get() = components.any { it is VAlarm }

public val VEvent.attendees: List<VAttendee>
    get() = properties
        .filter { it.name == "ATTENDEE" }
        .map { VAttendee(listOf(it)) }
