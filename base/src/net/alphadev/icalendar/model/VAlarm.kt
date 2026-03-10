package net.alphadev.icalendar.model

import kotlin.time.Duration
import kotlin.time.Instant

public data class VAlarm(
    override val properties: List<ICalProperty>,
    override val components: List<ICalComponent> = emptyList()
) : ICalComponent {
    override val componentName: String = NAME
    companion object { const val NAME = "VALARM" }
}

public enum class AlarmAction { DISPLAY, AUDIO, EMAIL, PROCEDURE }

public sealed interface AlarmTrigger {
    public enum class RelatedTo { START, END }

    public data class Relative(
        val duration: Duration,
        val relatedTo: RelatedTo = RelatedTo.START
    ) : AlarmTrigger

    public data class Absolute(val instant: Instant) : AlarmTrigger
}

public val VAlarm.action: AlarmAction?
    get() = properties.firstOrNull { it.name == "ACTION" }?.value?.let { value ->
        AlarmAction.entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
    }

public val VAlarm.trigger: AlarmTrigger?
    get() {
        val prop = properties.firstOrNull { it.name == "TRIGGER" } ?: return null

        // Absolute trigger (DATE-TIME value)
        if (prop.valueType.equals("DATE-TIME", ignoreCase = true)) {
            return prop.instant?.let { AlarmTrigger.Absolute(it) }
        }

        // Relative trigger (DURATION value, default)
        val relatedTo = when (prop.parameter("RELATED")?.uppercase()) {
            "END" -> AlarmTrigger.RelatedTo.END
            else -> AlarmTrigger.RelatedTo.START
        }

        val duration = parseICalDuration(prop.value) ?: return null
        return AlarmTrigger.Relative(duration, relatedTo)
    }

public val VAlarm.description: String?
    get() = properties.firstOrNull { it.name == "DESCRIPTION" }?.value

public val VAlarm.summary: String?
    get() = properties.firstOrNull { it.name == "SUMMARY" }?.value

public val VAlarm.attendees: List<String>
    get() = properties.filter { it.name == "ATTENDEE" }.map { it.value }

public val VAlarm.attach: String?
    get() = properties.firstOrNull { it.name == "ATTACH" }?.value

public val VAlarm.repeat: Int?
    get() = properties.firstOrNull { it.name == "REPEAT" }?.value?.toIntOrNull()

public val VAlarm.repeatDuration: Duration?
    get() = properties.firstOrNull { it.name == "DURATION" }?.value?.let { parseICalDuration(it) }

internal fun parseICalDuration(value: String): Duration? {
    val input = value.trim()
    if (input.isEmpty() || !input.contains('P')) return null

    // iCalendar uses 'W' for weeks, ISO 8601 doesn't support it
    // Convert weeks to days: 1W = 7D
    val normalized = input.replace(Regex("""(\d+)W""")) { match ->
        val weeks = match.groupValues[1].toLong()
        "${weeks * 7}D"
    }

    return try {
        Duration.parseIsoStringOrNull(normalized)
    } catch (e: Exception) {
        null
    }
}
