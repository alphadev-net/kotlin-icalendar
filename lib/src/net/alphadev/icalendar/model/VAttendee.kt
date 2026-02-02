package net.alphadev.icalendar.model

data class VAttendee(
    override val properties: List<ICalProperty>,
    override val components: List<ICalComponent> = emptyList()
) : ICalComponent {
    override val componentName: String = NAME
    companion object { const val NAME = "ATTENDEE" }

    val value: String
        get() = properties.firstOrNull()?.value.orEmpty()

    val email: String
        get() = value.removePrefix("mailto:")

    val name: String?
        get() = properties.firstOrNull()?.parameter("CN")

    val rsvp: Boolean
        get() = properties.firstOrNull()?.parameter("RSVP")?.equals("TRUE", ignoreCase = true) ?: false

    val role: String?
        get() = properties.firstOrNull()?.parameter("ROLE")

    val cutype: String?
        get() = properties.firstOrNull()?.parameter("CUTYPE")
}
