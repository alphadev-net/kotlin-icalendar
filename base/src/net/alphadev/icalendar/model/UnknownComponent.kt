package net.alphadev.icalendar.model

public data class UnknownComponent(
    val name: String,
    override val properties: List<ICalProperty>,
    override val components: List<ICalComponent>
) : ICalComponent {
    override val componentName: String = name
}

public val UnknownComponent.isExtension: Boolean
    get() = name.startsWith("X-", ignoreCase = true)

public val UnknownComponent.isStandardUnimplemented: Boolean
    get() = name in STANDARD_COMPONENT_NAMES && !isExtension

private val STANDARD_COMPONENT_NAMES = setOf(
    "VTODO", "VJOURNAL", "VFREEBUSY"
)
