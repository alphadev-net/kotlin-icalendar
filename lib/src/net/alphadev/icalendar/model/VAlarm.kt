package net.alphadev.icalendar.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

data class VAlarm(
    override val properties: List<ICalProperty>,
    override val components: List<ICalComponent> = emptyList()
) : ICalComponent {
    override val componentName: String = NAME
    companion object { const val NAME = "VALARM" }
}

enum class AlarmAction {
    AUDIO, DISPLAY, EMAIL;
    companion object {
        fun fromString(value: String): AlarmAction? =
            entries.find { it.name.equals(value, ignoreCase = true) }
    }
}

sealed interface AlarmTrigger {
    data class Relative(
        val duration: Duration,
        val relatedTo: RelatedTo = RelatedTo.START
    ) : AlarmTrigger

    data class Absolute(val dateTime: ICalTemporal) : AlarmTrigger

    enum class RelatedTo { START, END }
}

val VAlarm.action: AlarmAction?
    get() = properties.firstOrNull { it.name == "ACTION" }?.value?.let { AlarmAction.fromString(it) }

val VAlarm.actionRaw: String?
    get() = properties.firstOrNull { it.name == "ACTION" }?.value

val VAlarm.triggerProperty: ICalProperty?
    get() = properties.firstOrNull { it.name == "TRIGGER" }

val VAlarm.trigger: AlarmTrigger?
    get() = triggerProperty?.toAlarmTrigger()

val VAlarm.description: String?
    get() = properties.firstOrNull { it.name == "DESCRIPTION" }?.value

val VAlarm.summary: String?
    get() = properties.firstOrNull { it.name == "SUMMARY" }?.value

val VAlarm.repeat: Int?
    get() = properties.firstOrNull { it.name == "REPEAT" }?.value?.toIntOrNull()

val VAlarm.repeatDuration: Duration?
    get() = properties.firstOrNull { it.name == "DURATION" }?.value?.parseICalDuration()

val VAlarm.attach: String?
    get() = properties.firstOrNull { it.name == "ATTACH" }?.value

val VAlarm.attendees: List<String>
    get() = properties.filter { it.name == "ATTENDEE" }.map { it.value }

fun ICalProperty.toAlarmTrigger(): AlarmTrigger? {
    val v = value.trim()
    if (v.isEmpty()) return null

    val isAbsolute = valueType.equals("DATE-TIME", ignoreCase = true)

    return if (isAbsolute) {
        toICalTemporal()?.let { AlarmTrigger.Absolute(it) }
    } else {
        val duration = v.parseICalDuration() ?: return null
        val relatedTo = when (parameter("RELATED")?.uppercase()) {
            "END" -> AlarmTrigger.RelatedTo.END
            else -> AlarmTrigger.RelatedTo.START
        }
        AlarmTrigger.Relative(duration, relatedTo)
    }
}

fun String.parseICalDuration(): Duration? {
    val input = this.trim()
    if (input.isEmpty()) return null

    var index = 0
    val isNegative = when {
        input.startsWith('-') -> { index++; true }
        input.startsWith('+') -> { index++; false }
        else -> false
    }

    if (input.getOrNull(index) != 'P') return null
    index++

    var totalDuration = Duration.ZERO
    var inTimePart = false

    while (index < input.length) {
        val char = input[index]

        if (char == 'T') {
            inTimePart = true
            index++
            continue
        }

        val numStart = index
        while (index < input.length && input[index].isDigit()) {
            index++
        }

        if (index == numStart || index >= input.length) break

        val num = input.substring(numStart, index).toLongOrNull() ?: return null
        val unit = input[index]
        index++

        totalDuration += when (unit) {
            'W' -> (num * 7).days
            'D' -> num.days
            'H' -> num.hours
            'M' -> num.minutes
            'S' -> num.seconds
            else -> return null
        }
    }

    return if (isNegative) -totalDuration else totalDuration
}
