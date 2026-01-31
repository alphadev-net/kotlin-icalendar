package net.alphadev.icalendar.model

import kotlin.time.Duration
import kotlin.time.Instant

data class VAlarm(
    override val properties: List<ICalProperty>,
    override val components: List<ICalComponent> = emptyList()
) : ICalComponent {
    override val componentName: String = NAME
    companion object { const val NAME = "VALARM" }
}

enum class AlarmAction { DISPLAY, AUDIO, EMAIL, PROCEDURE }

sealed interface AlarmTrigger {
    enum class RelatedTo { START, END }

    data class Relative(
        val duration: Duration,
        val relatedTo: RelatedTo = RelatedTo.START
    ) : AlarmTrigger

    data class Absolute(val instant: Instant) : AlarmTrigger
}

val VAlarm.action: AlarmAction?
    get() = properties.firstOrNull { it.name == "ACTION" }?.value?.let { value ->
        AlarmAction.entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
    }

val VAlarm.trigger: AlarmTrigger?
    get() {
        val prop = properties.firstOrNull { it.name == "TRIGGER" } ?: return null
        val value = prop.value

        // Absolute trigger (DATE-TIME value)
        if (prop.valueType.equals("DATE-TIME", ignoreCase = true)) {
            return prop.toInstant()?.let { AlarmTrigger.Absolute(it) }
        }

        // Relative trigger (DURATION value, default)
        val relatedTo = when (prop.parameter("RELATED")?.uppercase()) {
            "END" -> AlarmTrigger.RelatedTo.END
            else -> AlarmTrigger.RelatedTo.START
        }

        val duration = parseICalDuration(value) ?: return null
        return AlarmTrigger.Relative(duration, relatedTo)
    }

val VAlarm.description: String?
    get() = properties.firstOrNull { it.name == "DESCRIPTION" }?.value

val VAlarm.summary: String?
    get() = properties.firstOrNull { it.name == "SUMMARY" }?.value

val VAlarm.attendees: List<String>
    get() = properties.filter { it.name == "ATTENDEE" }.map { it.value }

val VAlarm.attach: String?
    get() = properties.firstOrNull { it.name == "ATTACH" }?.value

val VAlarm.repeat: Int?
    get() = properties.firstOrNull { it.name == "REPEAT" }?.value?.toIntOrNull()

val VAlarm.repeatDuration: Duration?
    get() = properties.firstOrNull { it.name == "DURATION" }?.value?.let { parseICalDuration(it) }

internal fun parseICalDuration(value: String): Duration? {
    val input = value.trim()
    if (input.isEmpty()) return null

    var idx = 0
    val negative = if (input[idx] == '-') { idx++; true } else { if (input[idx] == '+') idx++; false }

    if (idx >= input.length || input[idx] != 'P') return null
    idx++

    var totalSeconds = 0L
    var inTime = false

    while (idx < input.length) {
        if (input[idx] == 'T') {
            inTime = true
            idx++
            continue
        }

        val numStart = idx
        while (idx < input.length && input[idx].isDigit()) idx++
        if (idx == numStart) return null

        val num = input.substring(numStart, idx).toLongOrNull() ?: return null
        if (idx >= input.length) return null

        val unit = input[idx++]
        totalSeconds += when {
            unit == 'W' -> num * 7 * 24 * 3600
            unit == 'D' -> num * 24 * 3600
            unit == 'H' && inTime -> num * 3600
            unit == 'M' && inTime -> num * 60
            unit == 'S' && inTime -> num
            else -> return null
        }
    }

    val seconds = if (negative) -totalSeconds else totalSeconds
    return Duration.parseIsoStringOrNull("PT${seconds}S")
}
