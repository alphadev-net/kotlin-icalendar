package net.alphadev.icalendar.model

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.asTimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Instant

sealed interface TimezoneResolver {
    fun resolve(localDateTime: LocalDateTime): Instant

    data class Iana(val timeZone: TimeZone) : TimezoneResolver {
        override fun resolve(localDateTime: LocalDateTime): Instant =
            localDateTime.toInstant(timeZone)
    }

    data class Fixed(val offset: UtcOffset) : TimezoneResolver {
        override fun resolve(localDateTime: LocalDateTime): Instant =
            localDateTime.toInstant(offset.asTimeZone())
    }

    companion object {
        val UTC = Fixed(UtcOffset.ZERO)

        fun from(tzid: String?, timezones: Map<String, VTimezone> = emptyMap()): TimezoneResolver {
            if (tzid == null) return UTC

            val normalized = normalizeTzid(tzid)

            // Try IANA first
            tryIana(normalized)?.let { return it }

            // Try Windows name mapping
            WINDOWS_TO_IANA[normalized]?.let { tryIana(it)?.let { iana -> return iana } }

            // Try UTC offset format
            tryUtcOffset(normalized)?.let { return Fixed(it) }

            // Check VTIMEZONE for fallback offset
            timezones[tzid]?.standardOffset?.let { return Fixed(it) }

            return UTC
        }

        private fun normalizeTzid(tzid: String): String {
            val stripped = tzid.trimStart('/')
            if (!stripped.contains('/')) return stripped

            val parts = stripped.split('/')
            if (parts.size >= 2) {
                val candidate = "${parts[parts.size - 2]}/${parts.last()}"
                if (candidate.first().isUpperCase()) return candidate
            }
            return stripped
        }

        private fun tryIana(tzid: String): Iana? =
            try { Iana(TimeZone.of(tzid)) } catch (_: Exception) { null }

        private fun tryUtcOffset(value: String): UtcOffset? {
            try { return UtcOffset.Formats.FOUR_DIGITS.parse(value) } catch (_: Exception) {}
            try { return UtcOffset.parse(value) } catch (_: Exception) {}
            return null
        }

        private val WINDOWS_TO_IANA = mapOf(
            "Eastern Standard Time" to "America/New_York",
            "Pacific Standard Time" to "America/Los_Angeles",
            "Central Standard Time" to "America/Chicago",
            "Mountain Standard Time" to "America/Denver",
            "GMT Standard Time" to "Europe/London",
            "W. Europe Standard Time" to "Europe/Berlin",
            "Central European Standard Time" to "Europe/Warsaw",
            "Romance Standard Time" to "Europe/Paris",
            "China Standard Time" to "Asia/Shanghai",
            "Tokyo Standard Time" to "Asia/Tokyo",
            "India Standard Time" to "Asia/Kolkata",
            "AUS Eastern Standard Time" to "Australia/Sydney"
        )
    }
}
