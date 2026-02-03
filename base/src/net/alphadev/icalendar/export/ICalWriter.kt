package net.alphadev.icalendar.export

import net.alphadev.icalendar.model.ICalComponent
import net.alphadev.icalendar.model.ICalProperty
import net.alphadev.icalendar.model.VCalendar

data class Config(
    val useCrLf: Boolean = true,
    val foldLines: Boolean = true
)

fun VCalendar.toICalString(config: Config = Config()): String = buildString {
    writeComponent(ensureVersion(this@toICalString), config)
}

fun List<VCalendar>.toICalString(config: Config = Config()): String {
    return joinToString(separator = "\n") { it.toICalString(config) }
}

private fun ensureVersion(calendar: VCalendar): VCalendar {
    val hasVersion = calendar.properties.any { it.name.equals("VERSION", ignoreCase = true) }
    if (hasVersion) return calendar
    val properties = listOf(ICalProperty("VERSION", emptyMap(), "2.0")) + calendar.properties
    return VCalendar(properties, calendar.components)
}

private fun StringBuilder.writeComponent(component: ICalComponent, config: Config) {
    writeLine("BEGIN:${component.componentName}", config)
    for (property in component.properties) {
        writeProperty(property, config)
    }
    for (nested in component.components) {
        writeComponent(nested, config)
    }
    writeLine("END:${component.componentName}", config)
}

private fun StringBuilder.writeProperty(property: ICalProperty, config: Config) {
    val line = buildContentLine(property)
    writeLine(line, config)
}

private fun buildContentLine(property: ICalProperty): String = buildString {
    append(property.name)
    for ((paramName, paramValues) in property.parameters) {
        append(';')
        append(paramName)
        append('=')
        append(formatParamValues(paramValues))
    }
    append(':')
    append(escapePropertyValue(property.value))
}

private fun formatParamValues(values: List<String>): String {
    return values.joinToString(",") { value ->
        if (value.any { it in PARAM_UNSAFE_CHARS }) "\"$value\"" else value
    }
}

private fun escapePropertyValue(value: String): String {
    return value
        .replace("\\", "\\\\")
        .replace("\n", "\\n")
        .replace("\r", "")
        .replace(",", "\\,")
        .replace(";", "\\;")
}

private fun StringBuilder.writeLine(line: String, config: Config) {
    val lineEnding = if (config.useCrLf) "\r\n" else "\n"
    val outputLine = if (config.foldLines) LineFolding.fold(line) else line
    append(outputLine)
    append(lineEnding)
}

private val PARAM_UNSAFE_CHARS = setOf(':', ';', ',', '"', '\n', '\r')
