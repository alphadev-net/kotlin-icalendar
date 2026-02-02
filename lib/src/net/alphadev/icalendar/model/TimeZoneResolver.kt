package net.alphadev.icalendar.model

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.asTimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Instant

sealed interface TimezoneResolver {
    fun resolve(localDateTime: LocalDateTime): Instant

    companion object {
        val UTC = FixedTimezoneResolver(UtcOffset.ZERO)

        fun from(tzid: String?, timezones: Map<String, VTimezone> = emptyMap()): TimezoneResolver {
            if (tzid == null) return UTC

            val normalized = normalizeTzid(tzid)

            // Try UTC offset first (before IANA, since TimeZone.of() also accepts offsets)
            if (looksLikeOffset(normalized)) {
                FixedTimezoneResolver.tryCreate(normalized)?.let { return it }
            }

            IanaTimezoneResolver.tryCreate(normalized)?.let { return it }

            WINDOWS_TO_IANA[normalized]?.let {
                IanaTimezoneResolver.tryCreate(it)?.let { iana -> return iana }
            }

            // Try offset parsing for non-obvious formats
            FixedTimezoneResolver.tryCreate(normalized)?.let { return it }

            timezones[tzid]?.let { return CustomTimezoneResolver(it) }

            return UTC
        }

        private fun looksLikeOffset(value: String): Boolean {
            return value.startsWith("+") || value.startsWith("-") ||
                   value.startsWith("UTC") || value.startsWith("GMT")
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

data class IanaTimezoneResolver(val timeZone: TimeZone) : TimezoneResolver {
    override fun resolve(localDateTime: LocalDateTime): Instant =
        localDateTime.toInstant(timeZone)

    companion object {
        fun tryCreate(tzid: String): IanaTimezoneResolver? =
            try { IanaTimezoneResolver(TimeZone.of(tzid)) } catch (_: Exception) { null }
    }
}

data class FixedTimezoneResolver(val offset: UtcOffset) : TimezoneResolver {
    override fun resolve(localDateTime: LocalDateTime): Instant =
        localDateTime.toInstant(offset.asTimeZone())

    companion object {
        fun tryCreate(value: String): FixedTimezoneResolver? {
            try { return FixedTimezoneResolver(UtcOffset.Formats.FOUR_DIGITS.parse(value)) } catch (_: Exception) {}
            try { return FixedTimezoneResolver(UtcOffset.parse(value)) } catch (_: Exception) {}
            return null
        }
    }
}

data class CustomTimezoneResolver(val vtimezone: VTimezone) : TimezoneResolver {
    override fun resolve(localDateTime: LocalDateTime): Instant {
        val offset = vtimezone.offsetAt(localDateTime)
        return localDateTime.toInstant(offset.asTimeZone())
    }
}
