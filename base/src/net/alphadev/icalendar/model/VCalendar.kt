package net.alphadev.icalendar.model

public data class VCalendar(
    override val properties: List<ICalProperty>,
    override val components: List<ICalComponent>
) : ICalComponent {
    override val componentName: String = NAME
    companion object { const val NAME = "VCALENDAR" }
}

public val VCalendar.version: String?
    get() = properties.firstOrNull { it.name == "VERSION" }?.value

public val VCalendar.prodId: String?
    get() = properties.firstOrNull { it.name == "PRODID" }?.value

public val VCalendar.calScale: String?
    get() = properties.firstOrNull { it.name == "CALSCALE" }?.value

public val VCalendar.method: String?
    get() = properties.firstOrNull { it.name == "METHOD" }?.value

public val VCalendar.events: List<VEvent>
    get() = components.filterIsInstance<VEvent>()

public val VCalendar.timezones: List<VTimezone>
    get() = components.filterIsInstance<VTimezone>()

public fun VCalendar.eventByUid(uid: String): VEvent? =
    events.firstOrNull { it.uid == uid }

public fun VCalendar.timezoneByTzid(tzid: String): VTimezone? =
    timezones.firstOrNull { it.tzid == tzid }

public val VCalendar.todos: List<VTodo>
    get() = components.filterIsInstance<VTodo>()

public val VCalendar.journals: List<VJournal>
    get() = components.filterIsInstance<VJournal>()

public val VCalendar.freeBusyItems: List<VFreeBusy>
    get() = components.filterIsInstance<VFreeBusy>()

public fun VCalendar.todoByUid(uid: String): VTodo? =
    todos.firstOrNull { it.uid == uid }

public fun VCalendar.journalByUid(uid: String): VJournal? =
    journals.firstOrNull { it.uid == uid }

public fun VCalendar.freeBusyByUid(uid: String): VFreeBusy? =
    freeBusyItems.firstOrNull { it.uid == uid }
