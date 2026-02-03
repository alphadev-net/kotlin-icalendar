package net.alphadev.icalendar.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.obj
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import net.alphadev.icalendar.model.VCalendar
import net.alphadev.icalendar.model.VEvent
import net.alphadev.icalendar.transform.mapType
import net.alphadev.icalendar.transform.offsetTimesBy
import net.alphadev.icalendar.transform.removeAlarms
import kotlin.time.Duration

internal class Transform : CliktCommand() {
    private val removeAlarms by option("--remove-alarms").flag()
    private val offsetBy: Duration? by option("--offset-by").convert { Duration.parse(it) }

    override fun run() {
        @Suppress("UNCHECKED_CAST")
        var calendars = currentContext.parent!!.obj as List<VCalendar>

        if (removeAlarms) {
            calendars = calendars.removeEventAlarms()
        }

        offsetBy?.let { calendars = calendars.offsetAllEventsBy(it) }

        currentContext.parent!!.obj = calendars
    }
}

internal fun List<VCalendar>.removeEventAlarms() = map { calendar ->
    calendar.mapType<VEvent> { it.removeAlarms() }
}

internal fun List<VCalendar>.offsetAllEventsBy(offset: Duration) = map { calendar ->
    calendar.mapType<VEvent> { it.offsetTimesBy(offset) }
}
