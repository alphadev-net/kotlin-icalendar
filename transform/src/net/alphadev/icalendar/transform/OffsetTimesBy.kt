package net.alphadev.icalendar.transform

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import net.alphadev.icalendar.model.*
import kotlin.time.Duration

fun VEvent.offsetTimesBy(duration: Duration): VEvent {
    val updatedProperties = properties.map { property ->
        when (property.name) {
            "DTSTART", "DTEND" -> {
                val offsetInstant = property.instant?.plus(duration)
                if (offsetInstant != null) {
                    val newValue = when {
                        // UTC times end with Z
                        property.value.endsWith("Z") -> offsetInstant.formatUtc()

                        // Timezone-aware times have TZID parameter
                        property.tzid != null -> {
                            val tz = try { TimeZone.of(property.tzid!!) } catch (_: Exception) { TimeZone.UTC }
                            iCalDateTimeFormat.format(offsetInstant.toLocalDateTime(tz))
                        }

                        // Date-only values (VALUE=DATE parameter)
                        property.valueType == "DATE" -> {
                            iCalDateFormat.format(offsetInstant.toLocalDateTime(TimeZone.UTC).date)
                        }

                        // Fallback: assume UTC
                        else -> offsetInstant.formatUtc()
                    }

                    property.copy(value = newValue, instant = offsetInstant)
                } else {
                    property
                }
            }
            else -> property
        }
    }

    return copy(properties = updatedProperties)
}
