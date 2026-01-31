package net.alphadev.icalendar.model

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char

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

val VTimezoneRule.dtStart: LocalDateTime?
    get() = properties.firstOrNull { it.name == "DTSTART" }?.value?.let {
        try { tzRuleDateTimeFormat.parse(it) } catch (_: Exception) { null }
    }

val VTimezoneRule.tzOffsetFrom: UtcOffset?
    get() = properties.firstOrNull { it.name == "TZOFFSETFROM" }?.value?.let { parseUtcOffset(it) }

val VTimezoneRule.tzOffsetTo: UtcOffset?
    get() = properties.firstOrNull { it.name == "TZOFFSETTO" }?.value?.let { parseUtcOffset(it) }

val VTimezoneRule.tzName: String?
    get() = properties.firstOrNull { it.name == "TZNAME" }?.value

val VTimezoneRule.rrule: String?
    get() = properties.firstOrNull { it.name == "RRULE" }?.value

private val tzRuleDateTimeFormat = LocalDateTime.Format {
    year(Padding.ZERO)
    monthNumber(Padding.ZERO)
    day(Padding.ZERO)
    char('T')
    hour(Padding.ZERO)
    minute(Padding.ZERO)
    second(Padding.ZERO)
}

private fun parseUtcOffset(value: String): UtcOffset? {
    try { return UtcOffset.Formats.FOUR_DIGITS.parse(value) } catch (_: Exception) {}
    try { return UtcOffset.parse(value) } catch (_: Exception) {}
    return null
}
