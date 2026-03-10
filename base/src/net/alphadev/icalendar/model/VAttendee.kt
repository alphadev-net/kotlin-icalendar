package net.alphadev.icalendar.model

public data class VAttendee(
    override val properties: List<ICalProperty>,
    override val components: List<ICalComponent> = emptyList()
) : ICalComponent {
    override val componentName: String = NAME

    public companion object {
        internal const val NAME = "ATTENDEE"
    }

    val value: String
        get() = properties.firstOrNull()?.value.orEmpty()
}

public val VAttendee.email: String
    get() = value.removePrefix("mailto:")

public val VAttendee.name: String?
    get() = properties.firstOrNull()?.parameter("CN")

public val VAttendee.rsvp: Boolean
    get() = properties.firstOrNull()?.parameter("RSVP")?.equals("TRUE", ignoreCase = true) ?: false

public val VAttendee.role: String?
    get() = properties.firstOrNull()?.parameter("ROLE")

public val VAttendee.cutype: String?
    get() = properties.firstOrNull()?.parameter("CUTYPE")
