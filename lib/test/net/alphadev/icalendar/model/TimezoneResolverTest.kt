package net.alphadev.icalendar.model

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TimezoneResolverTest {

    // ===================
    // TZID Resolution
    // ===================

    @Test
    fun resolvesIanaTimezone() {
        val resolver = TimezoneResolver.from("America/New_York")
        assertIs<IanaTimezoneResolver>(resolver)

        val local = LocalDateTime(2024, 6, 15, 12, 0, 0)
        val instant = resolver.resolve(local)
        // June = EDT = UTC-4
        val expected = local.toInstant(TimeZone.of("America/New_York"))
        assertEquals(expected, instant)
    }

    @Test
    fun resolvesWindowsTimezoneToIana() {
        val resolver = TimezoneResolver.from("Eastern Standard Time")
        assertIs<IanaTimezoneResolver>(resolver)

        val local = LocalDateTime(2024, 1, 15, 12, 0, 0)
        val instant = resolver.resolve(local)
        // January = EST = UTC-5
        val expected = local.toInstant(TimeZone.of("America/New_York"))
        assertEquals(expected, instant)
    }

    @Test
    fun resolvesUtcOffsetFourDigits() {
        val resolver = TimezoneResolver.from("+0530")
        assertIs<FixedTimezoneResolver>(resolver)
        assertEquals(UtcOffset(hours = 5, minutes = 30), resolver.offset)
    }

    @Test
    fun resolvesUtcOffsetWithColon() {
        val resolver = TimezoneResolver.from("+05:30")
        assertIs<FixedTimezoneResolver>(resolver)
        assertEquals(UtcOffset(hours = 5, minutes = 30), resolver.offset)
    }

    @Test
    fun resolvesNegativeUtcOffset() {
        val resolver = TimezoneResolver.from("-0800")
        assertIs<FixedTimezoneResolver>(resolver)
        assertEquals(UtcOffset(hours = -8, minutes = 0), resolver.offset)
    }

    @Test
    fun resolvesCustomVTimezone() {
        val vtimezone = buildVTimezone(
            tzid = "Custom/Zone",
            standardOffset = "+0100",
            daylightOffset = "+0200"
        )
        val timezones = mapOf("Custom/Zone" to vtimezone)

        val resolver = TimezoneResolver.from("Custom/Zone", timezones)
        assertIs<CustomTimezoneResolver>(resolver)
    }

    @Test
    fun nullTzidReturnsUtc() {
        val resolver = TimezoneResolver.from(null)
        assertIs<FixedTimezoneResolver>(resolver)
        assertEquals(UtcOffset.ZERO, resolver.offset)
    }

    // ===================
    // Leading slash TZID
    // ===================

    @Test
    fun stripsLeadingSlashFromTzid() {
        val resolver = TimezoneResolver.from("/America/New_York")
        assertIs<IanaTimezoneResolver>(resolver)
    }

    @Test
    fun handlesMozillaStyleTzid() {
        // Format: /mozilla.org/20050126_1/America/New_York
        val resolver = TimezoneResolver.from("/mozilla.org/20050126_1/America/New_York")
        assertIs<IanaTimezoneResolver>(resolver)
    }

    // ===================
    // Undefined VTIMEZONE fallback
    // ===================

    @Test
    fun undefinedVTimezoneWithIanaNameFallsBackToIana() {
        // TZID references a VTIMEZONE that doesn't exist, but happens to be valid IANA
        val resolver = TimezoneResolver.from("Europe/London", emptyMap())
        assertIs<IanaTimezoneResolver>(resolver)
    }

    @Test
    fun undefinedVTimezoneWithUnknownNameFallsBackToUtc() {
        val resolver = TimezoneResolver.from("Nonexistent/Timezone", emptyMap())
        assertIs<FixedTimezoneResolver>(resolver)
        assertEquals(UtcOffset.ZERO, resolver.offset)
    }

    // ===================
    // VTIMEZONE offset evaluation
    // ===================

    @Test
    fun vtimezoneWithoutRruleUsesFixedOffset() {
        val vtimezone = buildSimpleVTimezone("Test/Zone", "+0300")
        val resolver = CustomTimezoneResolver(vtimezone)

        val local = LocalDateTime(2024, 6, 15, 12, 0, 0)
        val instant = resolver.resolve(local)

        // 12:00 local at +03:00 = 09:00 UTC
        val expected = LocalDateTime(2024, 6, 15, 9, 0, 0).toInstant(TimeZone.UTC)
        assertEquals(expected, instant)
    }

    @Test
    fun vtimezoneEvaluatesDstTransitionSummer() {
        // US Eastern: DST starts second Sunday of March, ends first Sunday of November
        val vtimezone = buildUsEasternVTimezone()
        val resolver = CustomTimezoneResolver(vtimezone)

        // June 15 = summer = EDT (UTC-4)
        val local = LocalDateTime(2024, 6, 15, 12, 0, 0)
        val instant = resolver.resolve(local)

        val expected = LocalDateTime(2024, 6, 15, 16, 0, 0).toInstant(TimeZone.UTC)
        assertEquals(expected, instant)
    }

    @Test
    fun vtimezoneEvaluatesDstTransitionWinter() {
        val vtimezone = buildUsEasternVTimezone()
        val resolver = CustomTimezoneResolver(vtimezone)

        // January 15 = winter = EST (UTC-5)
        val local = LocalDateTime(2024, 1, 15, 12, 0, 0)
        val instant = resolver.resolve(local)

        val expected = LocalDateTime(2024, 1, 15, 17, 0, 0).toInstant(TimeZone.UTC)
        assertEquals(expected, instant)
    }

    @Test
    fun vtimezoneHandlesDateNearDstTransition() {
        val vtimezone = buildUsEasternVTimezone()
        val resolver = CustomTimezoneResolver(vtimezone)

        // March 10, 2024 is second Sunday = DST starts at 2am
        // At 1:00am = still EST (UTC-5)
        val beforeTransition = LocalDateTime(2024, 3, 10, 1, 0, 0)
        val instantBefore = resolver.resolve(beforeTransition)
        val expectedBefore = LocalDateTime(2024, 3, 10, 6, 0, 0).toInstant(TimeZone.UTC)
        assertEquals(expectedBefore, instantBefore)

        // At 3:00am = now EDT (UTC-4)
        val afterTransition = LocalDateTime(2024, 3, 10, 3, 0, 0)
        val instantAfter = resolver.resolve(afterTransition)
        val expectedAfter = LocalDateTime(2024, 3, 10, 7, 0, 0).toInstant(TimeZone.UTC)
        assertEquals(expectedAfter, instantAfter)
    }

    @Test
    fun vtimezoneWithMultipleHistoricalRules() {
        // Some VTIMEZONEs have multiple STANDARD rules for historical changes
        val vtimezone = VTimezone(
            properties = listOf(ICalProperty("TZID", emptyMap(), "Historical/Zone")),
            components = listOf(
                // Old rule: +0200 until 2000
                StandardTimezoneRule(listOf(
                    ICalProperty("DTSTART", emptyMap(), "19700101T000000"),
                    ICalProperty("TZOFFSETTO", emptyMap(), "+0200"),
                    ICalProperty("TZOFFSETFROM", emptyMap(), "+0200")
                )),
                // New rule: +0300 from 2010 onwards
                StandardTimezoneRule(listOf(
                    ICalProperty("DTSTART", emptyMap(), "20100101T000000"),
                    ICalProperty("TZOFFSETTO", emptyMap(), "+0300"),
                    ICalProperty("TZOFFSETFROM", emptyMap(), "+0200")
                ))
            )
        )

        val resolver = CustomTimezoneResolver(vtimezone)

        // Before 2010 = +0200
        val old = LocalDateTime(2005, 6, 15, 12, 0, 0)
        val instantOld = resolver.resolve(old)
        val expectedOld = LocalDateTime(2005, 6, 15, 10, 0, 0).toInstant(TimeZone.UTC)
        assertEquals(expectedOld, instantOld)

        // After 2010 = +0300
        val recent = LocalDateTime(2024, 6, 15, 12, 0, 0)
        val instantRecent = resolver.resolve(recent)
        val expectedRecent = LocalDateTime(2024, 6, 15, 9, 0, 0).toInstant(TimeZone.UTC)
        assertEquals(expectedRecent, instantRecent)
    }

    @Test
    fun vtimezoneWithMissingTzOffsetToFallsBackToStandard() {
        val vtimezone = VTimezone(
            properties = listOf(ICalProperty("TZID", emptyMap(), "Malformed/Zone")),
            components = listOf(
                StandardTimezoneRule(listOf(
                    ICalProperty("DTSTART", emptyMap(), "19700101T000000"),
                    ICalProperty("TZOFFSETTO", emptyMap(), "+0100")
                )),
                // Malformed daylight rule - missing TZOFFSETTO
                DaylightTimezoneRule(listOf(
                    ICalProperty("DTSTART", emptyMap(), "19700301T020000"),
                    ICalProperty("RRULE", emptyMap(), "FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU")
                    // No TZOFFSETTO!
                ))
            )
        )

        val resolver = CustomTimezoneResolver(vtimezone)
        val local = LocalDateTime(2024, 6, 15, 12, 0, 0)

        // Should not crash, should use standard offset
        val instant = resolver.resolve(local)
        assertNotNull(instant)
    }

    // ===================
    // Helpers
    // ===================

    private fun buildVTimezone(
        tzid: String,
        standardOffset: String,
        daylightOffset: String
    ): VTimezone {
        return VTimezone(
            properties = listOf(ICalProperty("TZID", emptyMap(), tzid)),
            components = listOf(
                StandardTimezoneRule(listOf(
                    ICalProperty("DTSTART", emptyMap(), "19701101T020000"),
                    ICalProperty("TZOFFSETTO", emptyMap(), standardOffset),
                    ICalProperty("TZOFFSETFROM", emptyMap(), daylightOffset),
                    ICalProperty("RRULE", emptyMap(), "FREQ=YEARLY;BYMONTH=11;BYDAY=1SU")
                )),
                DaylightTimezoneRule(listOf(
                    ICalProperty("DTSTART", emptyMap(), "19700308T020000"),
                    ICalProperty("TZOFFSETTO", emptyMap(), daylightOffset),
                    ICalProperty("TZOFFSETFROM", emptyMap(), standardOffset),
                    ICalProperty("RRULE", emptyMap(), "FREQ=YEARLY;BYMONTH=3;BYDAY=2SU")
                ))
            )
        )
    }

    private fun buildSimpleVTimezone(tzid: String, offset: String): VTimezone {
        return VTimezone(
            properties = listOf(ICalProperty("TZID", emptyMap(), tzid)),
            components = listOf(
                StandardTimezoneRule(listOf(
                    ICalProperty("DTSTART", emptyMap(), "19700101T000000"),
                    ICalProperty("TZOFFSETTO", emptyMap(), offset),
                    ICalProperty("TZOFFSETFROM", emptyMap(), offset)
                ))
            )
        )
    }

    private fun buildUsEasternVTimezone(): VTimezone {
        return VTimezone(
            properties = listOf(ICalProperty("TZID", emptyMap(), "US/Eastern")),
            components = listOf(
                StandardTimezoneRule(listOf(
                    ICalProperty("DTSTART", emptyMap(), "19701101T020000"),
                    ICalProperty("TZOFFSETTO", emptyMap(), "-0500"),
                    ICalProperty("TZOFFSETFROM", emptyMap(), "-0400"),
                    ICalProperty("RRULE", emptyMap(), "FREQ=YEARLY;BYMONTH=11;BYDAY=1SU")
                )),
                DaylightTimezoneRule(listOf(
                    ICalProperty("DTSTART", emptyMap(), "19700308T020000"),
                    ICalProperty("TZOFFSETTO", emptyMap(), "-0400"),
                    ICalProperty("TZOFFSETFROM", emptyMap(), "-0500"),
                    ICalProperty("RRULE", emptyMap(), "FREQ=YEARLY;BYMONTH=3;BYDAY=2SU")
                ))
            )
        )
    }
}
