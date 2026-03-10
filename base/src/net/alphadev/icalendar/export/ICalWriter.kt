package net.alphadev.icalendar.export

import net.alphadev.icalendar.model.ICalComponent
import net.alphadev.icalendar.model.ICalProperty
import net.alphadev.icalendar.model.VCalendar
import kotlinx.io.Buffer
import kotlinx.io.Sink
import kotlinx.io.buffered
import kotlinx.io.writeString
import kotlinx.io.readString

public data class Config(
    val useCrLf: Boolean = true,
    val foldLines: Boolean = true
)

public fun VCalendar.toICalString(config: Config = Config()): String {
    val buffer = Buffer()
    buffer.writeCalendar(this, config)
    return buffer.readString()
}

public fun List<VCalendar>.toICalString(config: Config = Config()): String {
    val buffer = Buffer()
    forEach { buffer.writeCalendar(it, config) }
    return buffer.readString()
}

public fun VCalendar.writeTo(sink: Sink, config: Config = Config()) {
    val buffered = sink.buffered()
    buffered.writeCalendar(this, config)
    buffered.flush()
}

public fun List<VCalendar>.writeTo(sink: Sink, config: Config = Config()) {
    val buffered = sink.buffered()
    forEach { buffered.writeCalendar(it, config) }
    buffered.flush()
}

private fun Sink.writeCalendar(calendar: VCalendar, config: Config) =
    writeComponent(ensureVersion(calendar), config)

private fun ensureVersion(calendar: VCalendar): VCalendar {
    val hasVersion = calendar.properties.any { it.name.equals("VERSION", ignoreCase = true) }
    if (hasVersion) return calendar
    val properties = listOf(ICalProperty("VERSION", emptyMap(), "2.0")) + calendar.properties
    return VCalendar(properties, calendar.components)
}

private fun Sink.writeComponent(component: ICalComponent, config: Config) {
    writeLine("BEGIN:${component.componentName}", config)
    for (property in component.properties) {
        writeProperty(property, config)
    }
    for (nested in component.components) {
        writeComponent(nested, config)
    }
    writeLine("END:${component.componentName}", config)
}

private fun Sink.writeProperty(property: ICalProperty, config: Config) {
    writeLine(buildContentLine(property), config)
}

private fun Sink.writeLine(line: String, config: Config) {
    val lineEnding = if (config.useCrLf) "\r\n" else "\n"
    val outputLine = if (config.foldLines) LineFolding.fold(line) else line
    writeString(outputLine)
    writeString(lineEnding)
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

private val PARAM_UNSAFE_CHARS = setOf(':', ';', ',', '"', '\n', '\r')
