package net.alphadev.icalendar.model

data class VCalendar(
    override val properties: List<ICalProperty>,
    override val components: List<ICalComponent>
) : ICalComponent {
    override val componentName: String = NAME
    companion object { const val NAME = "VCALENDAR" }
}

val VCalendar.version: String?
    get() = properties.firstOrNull { it.name == "VERSION" }?.value

val VCalendar.prodId: String?
    get() = properties.firstOrNull { it.name == "PRODID" }?.value

val VCalendar.calScale: String?
    get() = properties.firstOrNull { it.name == "CALSCALE" }?.value

val VCalendar.method: String?
    get() = properties.firstOrNull { it.name == "METHOD" }?.value

val VCalendar.events: List<VEvent>
    get() = components.filterIsInstance<VEvent>()

val VCalendar.timezones: List<VTimezone>
    get() = components.filterIsInstance<VTimezone>()

fun VCalendar.eventByUid(uid: String): VEvent? =
    events.firstOrNull { it.uid == uid }

fun VCalendar.timezoneByTzid(tzid: String): VTimezone? =
    timezones.firstOrNull { it.tzid == tzid }
