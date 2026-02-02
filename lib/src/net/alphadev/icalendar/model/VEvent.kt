package net.alphadev.icalendar.model

import kotlin.time.Instant

data class VEvent(
    override val properties: List<ICalProperty>,
    override val components: List<ICalComponent> = emptyList()
) : ICalComponent {
    override val componentName: String = NAME
    companion object { const val NAME = "VEVENT" }
}

val VEvent.uid: String?
    get() = properties.firstOrNull { it.name == "UID" }?.value

val VEvent.summary: String?
    get() = properties.firstOrNull { it.name == "SUMMARY" }?.value

val VEvent.description: String?
    get() = properties.firstOrNull { it.name == "DESCRIPTION" }?.value

val VEvent.location: String?
    get() = properties.firstOrNull { it.name == "LOCATION" }?.value

val VEvent.sequence: Int?
    get() = properties.firstOrNull { it.name == "SEQUENCE" }?.value?.toIntOrNull()

val VEvent.status: String?
    get() = properties.firstOrNull { it.name == "STATUS" }?.value

val VEvent.transp: String?
    get() = properties.firstOrNull { it.name == "TRANSP" }?.value

val VEvent.organizer: String?
    get() = properties.firstOrNull { it.name == "ORGANIZER" }?.value

val VEvent.dtStartProperty: ICalProperty?
    get() = properties.firstOrNull { it.name == "DTSTART" }

val VEvent.dtEndProperty: ICalProperty?
    get() = properties.firstOrNull { it.name == "DTEND" }

val VEvent.dtStart: Instant?
    get() = dtStartProperty?.instant

val VEvent.dtEnd: Instant?
    get() = dtEndProperty?.instant

val VEvent.dtStamp: Instant?
    get() = properties.firstOrNull { it.name == "DTSTAMP" }?.instant

val VEvent.created: Instant?
    get() = properties.firstOrNull { it.name == "CREATED" }?.instant

val VEvent.lastModified: Instant?
    get() = properties.firstOrNull { it.name == "LAST-MODIFIED" }?.instant

val VEvent.isAllDay: Boolean
    get() = dtStartProperty?.valueType.equals("DATE", ignoreCase = true)
            || (dtStartProperty?.value?.contains("T") == false)

val VEvent.alarms: List<VAlarm>
    get() = components.filterIsInstance<VAlarm>()

val VEvent.hasAlarms: Boolean
    get() = components.any { it is VAlarm }

val VEvent.attendees: List<VAttendee>
    get() = properties
        .filter { it.name == "ATTENDEE" }
        .map { VAttendee(listOf(it)) }
