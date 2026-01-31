package net.alphadev.icalendar.import

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import net.alphadev.icalendar.model.*
import kotlin.time.Instant

fun parseICalendar(input: String): List<VCalendar> {
    val lines = LineUnfolder.unfold(input)
    if (lines.isEmpty()) return emptyList()

    val contentLines = lines
        .filter { it.isNotBlank() }
        .map { ContentLineParser.parse(it) }

    val result = mutableListOf<VCalendar>()
    val iterator = contentLines.iterator()

    while (iterator.hasNext()) {
        val line = iterator.next()
        if (line.isBegin && line.componentName == "VCALENDAR") {
            parseVCalendar(iterator)?.let { result.add(it) }
        }
    }

    return result
}

private fun parseVCalendar(iterator: Iterator<ContentLine>): VCalendar? {
    val (properties, components) = parseComponentBody("VCALENDAR", iterator)
    val calendar = VCalendar(properties, components)

    // Build timezone map and resolve instants
    val timezoneMap = calendar.timezones.mapNotNull { tz ->
        tz.tzid?.let { it to tz }
    }.toMap()

    return calendar.resolveInstants(timezoneMap)
}

private fun parseComponentBody(
    componentName: String,
    iterator: Iterator<ContentLine>
): Pair<List<ICalProperty>, List<ICalComponent>> {
    val properties = mutableListOf<ICalProperty>()
    val components = mutableListOf<ICalComponent>()

    while (iterator.hasNext()) {
        val line = iterator.next()

        when {
            line.isEnd && line.componentName == componentName -> {
                return properties to components
            }
            line.isEnd -> { }
            line.isBegin -> {
                val nested = parseNestedComponent(line.componentName, iterator)
                components.add(nested)
            }
            else -> {
                properties.add(line.toProperty())
            }
        }
    }

    return properties to components
}

private fun parseNestedComponent(name: String, iterator: Iterator<ContentLine>): ICalComponent {
    val (properties, components) = parseComponentBody(name, iterator)

    return when (name) {
        VEvent.NAME -> VEvent(properties, components)
        VAlarm.NAME -> VAlarm(properties, components)
        VCalendar.NAME -> VCalendar(properties, components)
        VTimezone.NAME -> VTimezone(properties, components)
        VTimezoneRule.Standard.NAME -> VTimezoneRule.Standard(properties)
        VTimezoneRule.Daylight.NAME -> VTimezoneRule.Daylight(properties)
        else -> UnknownComponent(name, properties, components)
    }
}

private fun ContentLine.toProperty() = ICalProperty(
    name = name,
    parameters = parameters,
    value = value
)

// Instant resolution

private fun VCalendar.resolveInstants(timezones: Map<String, VTimezone>): VCalendar {
    return copy(
        properties = properties.map { it.resolveInstant(timezones) },
        components = components.map { it.resolveInstants(timezones) }
    )
}

private fun ICalComponent.resolveInstants(timezones: Map<String, VTimezone>): ICalComponent {
    return when (this) {
        is VEvent -> copy(
            properties = properties.map { it.resolveInstant(timezones) },
            components = components.map { it.resolveInstants(timezones) }
        )
        is VAlarm -> copy(
            properties = properties.map { it.resolveInstant(timezones) },
            components = components.map { it.resolveInstants(timezones) }
        )
        is VCalendar -> copy(
            properties = properties.map { it.resolveInstant(timezones) },
            components = components.map { it.resolveInstants(timezones) }
        )
        is VTimezone -> this
        is VTimezoneRule.Standard -> this
        is VTimezoneRule.Daylight -> this
        is UnknownComponent -> copy(
            properties = properties.map { it.resolveInstant(timezones) },
            components = components.map { it.resolveInstants(timezones) }
        )
    }
}

private fun ICalProperty.resolveInstant(timezones: Map<String, VTimezone>): ICalProperty {
    if (!isDateTimeProperty()) return this

    val resolved = tryParseInstant(timezones)
    return if (resolved != null) copy(instant = resolved) else this
}

private fun ICalProperty.isDateTimeProperty(): Boolean {
    return name in DATE_TIME_PROPERTIES
}

private val DATE_TIME_PROPERTIES = setOf(
    "DTSTART", "DTEND", "DTSTAMP", "CREATED", "LAST-MODIFIED",
    "COMPLETED", "DUE", "TRIGGER", "EXDATE", "RDATE"
)

private fun ICalProperty.tryParseInstant(timezones: Map<String, VTimezone>): Instant? {
    val v = value.trim()
    if (v.isEmpty()) return null

    return try {
        when {
            valueType.equals("DATE", ignoreCase = true) || (!v.contains("T") && v.length == 8) -> {
                val date = iCalDateFormat.parse(v)
                val resolver = TimezoneResolver.from(tzid, timezones)
                val midnight = LocalDateTime(date.year, date.month, date.day, 0, 0, 0)
                resolver.resolve(midnight)
            }
            v.endsWith("Z") -> {
                iCalDateTimeFormat.parse(v.dropLast(1)).toInstant(TimeZone.UTC)
            }
            else -> {
                val localDateTime = iCalDateTimeFormat.parse(v)
                val resolver = TimezoneResolver.from(tzid, timezones)
                resolver.resolve(localDateTime)
            }
        }
    } catch (_: Exception) {
        null
    }
}

private val iCalDateFormat = LocalDate.Format {
    year(Padding.ZERO)
    monthNumber(Padding.ZERO)
    day(Padding.ZERO)
}

private val iCalDateTimeFormat = LocalDateTime.Format {
    year(Padding.ZERO)
    monthNumber(Padding.ZERO)
    day(Padding.ZERO)
    char('T')
    hour(Padding.ZERO)
    minute(Padding.ZERO)
    second(Padding.ZERO)
}
