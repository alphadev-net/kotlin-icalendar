package net.alphadev.icalendar.export

import net.alphadev.icalendar.model.ICalComponent
import net.alphadev.icalendar.model.ICalProperty
import net.alphadev.icalendar.model.VCalendar

class ICalWriter(private val config: Config = Config()) {
    data class Config(
        val useCrLf: Boolean = true,
        val foldLines: Boolean = true
    )

    private val lineEnding = if (config.useCrLf) "\r\n" else "\n"

    fun write(calendar: VCalendar): String = buildString {
        writeComponent(ensureVersion(calendar))
    }

    fun writeAll(calendars: List<VCalendar>): String = buildString {
        calendars.forEach { writeComponent(ensureVersion(it)) }
    }

    private fun ensureVersion(calendar: VCalendar): VCalendar {
        val hasVersion = calendar.properties.any { it.name.equals("VERSION", ignoreCase = true) }
        if (hasVersion) return calendar

        val properties = listOf(ICalProperty("VERSION", emptyMap(), "2.0")) + calendar.properties
        return VCalendar(properties, calendar.components)
    }

    private fun StringBuilder.writeComponent(component: ICalComponent) {
        writeLine("BEGIN:${component.componentName}")

        for (property in component.properties) {
            writeProperty(property)
        }

        for (nested in component.components) {
            writeComponent(nested)
        }

        writeLine("END:${component.componentName}")
    }

    private fun StringBuilder.writeProperty(property: ICalProperty) {
        val line = buildContentLine(property)
        writeLine(line)
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

    private fun StringBuilder.writeLine(line: String) {
        val outputLine = if (config.foldLines) LineFolding.fold(line) else line
        append(outputLine)
        append(lineEnding)
    }

    companion object {
        private val PARAM_UNSAFE_CHARS = setOf(':', ';', ',', '"', '\n', '\r')
    }
}

fun VCalendar.toICalString(config: ICalWriter.Config = ICalWriter.Config()): String {
    return ICalWriter(config).write(this)
}
