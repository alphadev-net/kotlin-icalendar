package net.alphadev.icalendar.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RRuleParserTest {

    // ===================
    // Basic RRULE parsing
    // ===================

    @Test
    fun parsesLastSundayOfMarch() {
        val rule = parseRRule("FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU")

        assertNotNull(rule)
        assertEquals(Month.MARCH, rule.month)
        assertEquals(DayOfWeek.SUNDAY, rule.dayOfWeek)
        assertEquals(-1, rule.occurrence)
    }

    @Test
    fun parsesFirstSundayOfNovember() {
        val rule = parseRRule("FREQ=YEARLY;BYMONTH=11;BYDAY=1SU")

        assertNotNull(rule)
        assertEquals(Month.NOVEMBER, rule.month)
        assertEquals(DayOfWeek.SUNDAY, rule.dayOfWeek)
        assertEquals(1, rule.occurrence)
    }

    @Test
    fun parsesSecondSundayOfMarch() {
        val rule = parseRRule("FREQ=YEARLY;BYMONTH=3;BYDAY=2SU")

        assertNotNull(rule)
        assertEquals(Month.MARCH, rule.month)
        assertEquals(DayOfWeek.SUNDAY, rule.dayOfWeek)
        assertEquals(2, rule.occurrence)
    }

    @Test
    fun parsesAllDaysOfWeek() {
        val days = DayOfWeek.entries.map { it.name.take(2) to it }

        for ((code, expected) in days) {
            val rule = parseRRule("FREQ=YEARLY;BYMONTH=1;BYDAY=1$code")
            assertNotNull(rule, "Failed to parse $code")
            assertEquals(expected, rule.dayOfWeek, message = "Wrong day for $code")
        }
    }

    @Test
    fun parsesAllMonths() {
        for (monthNum in 1..12) {
            val rule = parseRRule("FREQ=YEARLY;BYMONTH=$monthNum;BYDAY=1SU")
            assertNotNull(rule, "Failed to parse month $monthNum")
            assertEquals(Month.entries[monthNum - 1], rule.month)
        }
    }

    // ===================
    // Invalid RRULE handling
    // ===================

    @Test
    fun rejectsNonYearlyFrequency() {
        assertNull(parseRRule("FREQ=MONTHLY;BYMONTH=3;BYDAY=-1SU"))
        assertNull(parseRRule("FREQ=WEEKLY;BYMONTH=3;BYDAY=-1SU"))
        assertNull(parseRRule("FREQ=DAILY;BYMONTH=3;BYDAY=-1SU"))
    }

    @Test
    fun rejectsMissingBymonth() {
        assertNull(parseRRule("FREQ=YEARLY;BYDAY=-1SU"))
    }

    @Test
    fun rejectsMissingByday() {
        assertNull(parseRRule("FREQ=YEARLY;BYMONTH=3"))
    }

    @Test
    fun rejectsInvalidMonth() {
        assertNull(parseRRule("FREQ=YEARLY;BYMONTH=13;BYDAY=1SU"))
        assertNull(parseRRule("FREQ=YEARLY;BYMONTH=0;BYDAY=1SU"))
        assertNull(parseRRule("FREQ=YEARLY;BYMONTH=abc;BYDAY=1SU"))
    }

    @Test
    fun rejectsInvalidDayCode() {
        assertNull(parseRRule("FREQ=YEARLY;BYMONTH=3;BYDAY=1XX"))
        assertNull(parseRRule("FREQ=YEARLY;BYMONTH=3;BYDAY=1"))
    }

    // ===================
    // Date calculation
    // ===================

    @Test
    fun calculatesLastSundayOfMarch2024() {
        val rule = DstRule(Month.MARCH, DayOfWeek.SUNDAY, -1)
        val date = rule.dateInYear(2024)

        // March 2024: last Sunday is March 31
        assertEquals(LocalDate(2024, 3, 31), date)
    }

    @Test
    fun calculatesSecondSundayOfMarch2024() {
        val rule = DstRule(Month.MARCH, DayOfWeek.SUNDAY, 2)
        val date = rule.dateInYear(2024)

        // March 2024: March 1 is Friday, so first Sunday is March 3, second is March 10
        assertEquals(LocalDate(2024, 3, 10), date)
    }

    @Test
    fun calculatesFirstSundayOfNovember2024() {
        val rule = DstRule(Month.NOVEMBER, DayOfWeek.SUNDAY, 1)
        val date = rule.dateInYear(2024)

        // November 2024: November 1 is Friday, so first Sunday is November 3
        assertEquals(LocalDate(2024, 11, 3), date)
    }

    @Test
    fun calculatesLastFridayOfFebruary2024() {
        val rule = DstRule(Month.FEBRUARY, DayOfWeek.FRIDAY, -1)
        val date = rule.dateInYear(2024)

        // February 2024 is leap year (29 days), Feb 29 is Thursday, so last Friday is Feb 23
        assertEquals(LocalDate(2024, 2, 23), date)
    }

    @Test
    fun calculatesLastFridayOfFebruary2023() {
        val rule = DstRule(Month.FEBRUARY, DayOfWeek.FRIDAY, -1)
        val date = rule.dateInYear(2023)

        // February 2023 is not leap year (28 days), Feb 28 is Tuesday, so last Friday is Feb 24
        assertEquals(LocalDate(2023, 2, 24), date)
    }

    @Test
    fun calculatesFirstMondayOfJanuary2024() {
        val rule = DstRule(Month.JANUARY, DayOfWeek.MONDAY, 1)
        val date = rule.dateInYear(2024)

        // January 2024: January 1 is Monday
        assertEquals(LocalDate(2024, 1, 1), date)
    }

    @Test
    fun calculatesFourthThursdayOfNovember2024() {
        val rule = DstRule(Month.NOVEMBER, DayOfWeek.THURSDAY, 4)
        val date = rule.dateInYear(2024)

        // November 2024: Nov 1 is Friday, first Thursday is Nov 7
        // Fourth Thursday is Nov 28 (Thanksgiving)
        assertEquals(LocalDate(2024, 11, 28), date)
    }
}
