package net.alphadev.icalendar.model

import kotlinx.datetime.UtcOffset

data class VTimezone(
    override val properties: List<ICalProperty>,
    override val components: List<ICalComponent> = emptyList()
) : ICalComponent {
    override val componentName: String = NAME
    companion object { const val NAME = "VTIMEZONE" }
}

sealed class VTimezoneRule : ICalComponent {
    abstract override val properties: List<ICalProperty>
    override val components: List<ICalComponent> get() = emptyList()

    data class Standard(override val properties: List<ICalProperty>) : VTimezoneRule() {
        override val componentName: String = NAME
        companion object { const val NAME = "STANDARD" }
    }

    data class Daylight(override val properties: List<ICalProperty>) : VTimezoneRule() {
        override val componentName: String = NAME
        companion object { const val NAME = "DAYLIGHT" }
    }
}

val VTimezone.tzid: String?
    get() = properties.firstOrNull { it.name == "TZID" }?.value

val VTimezone.standardRules: List<VTimezoneRule.Standard>
    get() = components.filterIsInstance<VTimezoneRule.Standard>()

val VTimezone.daylightRules: List<VTimezoneRule.Daylight>
    get() = components.filterIsInstance<VTimezoneRule.Daylight>()

val VTimezone.standardOffset: UtcOffset?
    get() = standardRules.firstOrNull()?.tzOffsetTo

val VTimezoneRule.tzOffsetTo: UtcOffset?
    get() = properties.firstOrNull { it.name == "TZOFFSETTO" }?.value?.parseUtcOffset()

val VTimezoneRule.tzOffsetFrom: UtcOffset?
    get() = properties.firstOrNull { it.name == "TZOFFSETFROM" }?.value?.parseUtcOffset()

private fun String.parseUtcOffset(): UtcOffset? {
    try { return UtcOffset.Formats.FOUR_DIGITS.parse(this) } catch (_: Exception) {}
    try { return UtcOffset.parse(this) } catch (_: Exception) {}
    return null
}
