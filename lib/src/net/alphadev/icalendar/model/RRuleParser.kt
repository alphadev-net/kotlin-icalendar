package net.alphadev.icalendar.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

/**
 * Minimal RRULE parser for VTIMEZONE DST transitions.
 * Handles: FREQ=YEARLY;BYMONTH=X;BYDAY=±NSU/MO/TU/WE/TH/FR/SA
 */
data class DstRule(
    val month: Month,
    val dayOfWeek: DayOfWeek,
    val occurrence: Int // 1-4 for "first-fourth", -1 for "last"
) {
    /**
     * Find the date this rule fires in a given year.
     */
    fun dateInYear(year: Int): LocalDate {
        return if (occurrence > 0) {
            nthDayOfWeekInMonth(year, month, dayOfWeek, occurrence)
        } else {
            lastDayOfWeekInMonth(year, month, dayOfWeek)
        }
    }
}

fun parseRRule(rrule: String): DstRule? {
    val parts = rrule.split(';').associate { part ->
        val (k, v) = part.split('=', limit = 2).let {
            if (it.size == 2) it[0] to it[1] else return@associate it[0] to ""
        }
        k.uppercase() to v.uppercase()
    }

    // Only support YEARLY frequency
    if (parts["FREQ"] != "YEARLY") return null

    val month = parts["BYMONTH"]?.toIntOrNull()?.let { monthNumber ->
        Month.entries.getOrNull(monthNumber - 1)
    } ?: return null

    val byday = parts["BYDAY"] ?: return null
    val (occurrence, dayOfWeek) = parseByday(byday) ?: return null

    return DstRule(month, dayOfWeek, occurrence)
}

private fun parseByday(byday: String): Pair<Int, DayOfWeek>? {
    // Format: [-]N[SU|MO|TU|WE|TH|FR|SA] e.g., "-1SU", "2SU", "1MO"
    val regex = Regex("^(-?\\d)?([A-Z]{2})$")
    val match = regex.matchEntire(byday) ?: return null

    val occurrenceStr = match.groupValues[1]
    val dayStr = match.groupValues[2]

    val occurrence = when {
        occurrenceStr.isEmpty() -> 1 // No number means first
        else -> occurrenceStr.toIntOrNull() ?: return null
    }

    val dayOfWeek = DayOfWeek.entries.firstOrNull {
        it.name.startsWith(dayStr, ignoreCase = true)
    } ?: return null

    return occurrence to dayOfWeek
}

private fun nthDayOfWeekInMonth(year: Int, month: Month, dayOfWeek: DayOfWeek, n: Int): LocalDate {
    require(n in 1..5)
    val first = LocalDate(year, month, 1)
    val firstDow = first.dayOfWeek

    // Days until first occurrence of target day
    val daysUntil = (dayOfWeek.ordinal - firstDow.ordinal + 7) % 7
    val firstOccurrence = first.toEpochDays() + daysUntil
    val nthOccurrence = firstOccurrence + (n - 1) * 7

    return LocalDate.fromEpochDays(nthOccurrence.toInt())
}

private fun lastDayOfWeekInMonth(year: Int, month: Month, dayOfWeek: DayOfWeek): LocalDate {
    val lastDay = LocalDate(year, month, month.lastDay(year))
    val lastDow = lastDay.dayOfWeek

    // Days back to last occurrence of target day
    val daysBack = (lastDow.ordinal - dayOfWeek.ordinal + 7) % 7
    return LocalDate.fromEpochDays(lastDay.toEpochDays() - daysBack)
}

private fun Month.lastDay(year: Int): Int {
    // Use kotlinx-datetime to determine month length by finding the last valid day
    val nextMonth = if (this == Month.DECEMBER) {
        LocalDate(year + 1, Month.JANUARY, 1)
    } else {
        LocalDate(year, this.ordinal + 2, 1) // ordinal is 0-based, months are 1-based
    }
    return LocalDate.fromEpochDays(nextMonth.toEpochDays() - 1).day
}
