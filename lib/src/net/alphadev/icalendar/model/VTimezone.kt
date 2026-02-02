package net.alphadev.icalendar.model

import kotlinx.datetime.LocalDateTime
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
}

data class StandardTimezoneRule(override val properties: List<ICalProperty>) : VTimezoneRule() {
    override val componentName: String = NAME
    companion object { const val NAME = "STANDARD" }
}

data class DaylightTimezoneRule(override val properties: List<ICalProperty>) : VTimezoneRule() {
    override val componentName: String = NAME
    companion object { const val NAME = "DAYLIGHT" }
}

val VTimezone.tzid: String?
    get() = properties.firstOrNull { it.name == "TZID" }?.value

val VTimezone.standardRules: List<StandardTimezoneRule>
    get() = components.filterIsInstance<StandardTimezoneRule>()

val VTimezone.daylightRules: List<DaylightTimezoneRule>
    get() = components.filterIsInstance<DaylightTimezoneRule>()

val VTimezone.standardOffset: UtcOffset?
    get() = standardRules.firstOrNull()?.tzOffsetTo

val VTimezoneRule.tzOffsetTo: UtcOffset?
    get() = properties.firstOrNull { it.name == "TZOFFSETTO" }?.value?.parseUtcOffset()

val VTimezoneRule.tzOffsetFrom: UtcOffset?
    get() = properties.firstOrNull { it.name == "TZOFFSETFROM" }?.value?.parseUtcOffset()

val VTimezoneRule.dtStart: LocalDateTime?
    get() = properties.firstOrNull { it.name == "DTSTART" }?.value?.let { parseICalDateTime(it) }

val VTimezoneRule.rrule: String?
    get() = properties.firstOrNull { it.name == "RRULE" }?.value

val VTimezoneRule.dstRule: DstRule?
    get() = rrule?.let { parseRRule(it) }

val VTimezoneRule.rdates: List<LocalDateTime>
    get() = properties
        .filter { it.name == "RDATE" }
        .mapNotNull { parseICalDateTime(it.value) }

private fun String.parseUtcOffset(): UtcOffset? {
    try { return UtcOffset.Formats.FOUR_DIGITS.parse(this) } catch (_: Exception) {}
    try { return UtcOffset.parse(this) } catch (_: Exception) {}
    return null
}

/**
 * Determine the UTC offset in effect at the given local datetime.
 * Evaluates STANDARD and DAYLIGHT rules with their RRULE patterns.
 */
fun VTimezone.offsetAt(localDateTime: LocalDateTime): UtcOffset {
    data class Transition(val dateTime: LocalDateTime, val offset: UtcOffset)

    val year = localDateTime.year
    val transitions = mutableListOf<Transition>()

    for (rule in standardRules + daylightRules) {
        val offset = rule.tzOffsetTo ?: continue
        val dstRule = rule.dstRule
        val dtStart = rule.dtStart

        if (dstRule != null && dtStart != null) {
            for (y in listOf(year - 1, year)) {
                val transitionDate = dstRule.dateInYear(y)
                val transitionDateTime = LocalDateTime(
                    transitionDate.year, transitionDate.month, transitionDate.day,
                    dtStart.hour, dtStart.minute, dtStart.second
                )
                transitions.add(Transition(transitionDateTime, offset))
            }
        } else if (dtStart != null) {
            transitions.add(Transition(dtStart, offset))
        }

        // Handle RDATE - explicit transition dates
        for (rdate in rule.rdates) {
            if (rdate.year in listOf(year - 1, year, year + 1)) {
                transitions.add(Transition(rdate, offset))
            }
        }
    }

    if (transitions.isEmpty()) {
        return standardOffset ?: UtcOffset.ZERO
    }

    transitions.sortBy { it.dateTime }

    var activeOffset = transitions.first().offset
    for (t in transitions) {
        if (t.dateTime <= localDateTime) {
            activeOffset = t.offset
        } else {
            break
        }
    }

    return activeOffset
}
