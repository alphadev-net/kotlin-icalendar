package net.alphadev.icalendar.import

import net.alphadev.icalendar.model.*

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
    return VCalendar(properties, components)
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
        else -> UnknownComponent(name, properties, components)
    }
}

private fun ContentLine.toProperty() = ICalProperty(
    name = name,
    parameters = parameters,
    value = value
)
