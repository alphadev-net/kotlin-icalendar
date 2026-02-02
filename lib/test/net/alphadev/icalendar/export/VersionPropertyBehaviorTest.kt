package net.alphadev.icalendar.export

import net.alphadev.icalendar.model.ICalProperty
import net.alphadev.icalendar.model.VCalendar
import net.alphadev.icalendar.model.dsl.vCalendar
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

/**
 * Documents and tests the VERSION property handling behavior.
 *
 * RFC 5545 requires VERSION:2.0 in all iCalendar objects.
 * This test class ensures that VERSION is always present and correctly positioned,
 * regardless of whether the calendar is created via DSL or constructor.
 */
class VersionPropertyBehaviorTest {

    // ===========================================
    // DSL Behavior
    // ===========================================

    @Test
    fun dslAutomaticallyAddsVersion() {
        val calendar = vCalendar { }

        val result = ICalWriter().write(calendar)

        assertContains(result, "VERSION:2.0")
    }

    @Test
    fun dslVersionIsFirstProperty() {
        val calendar = vCalendar {
            prodId("Test")
            calScale("GREGORIAN")
        }

        val result = ICalWriter().write(calendar)
        val lines = result.lines().filter { it.isNotBlank() }

        assertEquals("BEGIN:VCALENDAR", lines[0])
        assertEquals("VERSION:2.0", lines[1])
        assertEquals("PRODID:Test", lines[2])
    }

    @Test
    fun dslCannotRemoveVersion() {
        // Even if you try to override VERSION, the DSL's init block ensures it exists
        val calendar = vCalendar {
            prodId("Test")
            // No way to remove VERSION through DSL
        }

        val result = ICalWriter().write(calendar)

        assertContains(result, "VERSION:2.0")
    }

    @Test
    fun dslExplicitVersionIsNotDuplicated() {
        val calendar = vCalendar {
            property("VERSION", "2.0")
            prodId("Test")
        }

        val result = ICalWriter().write(calendar)

        // Count VERSION occurrences - should be exactly 1
        val versionCount = result.lines().count { it.trim() == "VERSION:2.0" }
        assertEquals(1, versionCount, "VERSION should appear exactly once")
    }

    // ===========================================
    // Constructor Behavior
    // ===========================================

    @Test
    fun constructorEmptyPropertiesGetsVersion() {
        val calendar = VCalendar(
            properties = emptyList(),
            components = emptyList()
        )

        val result = ICalWriter().write(calendar)

        assertContains(result, "VERSION:2.0")
        assertEquals("BEGIN:VCALENDAR\r\nVERSION:2.0\r\nEND:VCALENDAR\r\n", result)
    }

    @Test
    fun constructorMissingVersionGetsPrepended() {
        val calendar = VCalendar(
            properties = listOf(
                ICalProperty("PRODID", emptyMap(), "Test"),
                ICalProperty("CALSCALE", emptyMap(), "GREGORIAN")
            ),
            components = emptyList()
        )

        val result = ICalWriter().write(calendar)
        val lines = result.lines().filter { it.isNotBlank() }

        // VERSION should be prepended, not appended
        assertEquals("BEGIN:VCALENDAR", lines[0])
        assertEquals("VERSION:2.0", lines[1])
        assertEquals("PRODID:Test", lines[2])
        assertEquals("CALSCALE:GREGORIAN", lines[3])
        assertEquals("END:VCALENDAR", lines[4])
    }

    @Test
    fun constructorExistingVersionIsNotDuplicated() {
        val calendar = VCalendar(
            properties = listOf(
                ICalProperty("VERSION", emptyMap(), "2.0"),
                ICalProperty("PRODID", emptyMap(), "Test")
            ),
            components = emptyList()
        )

        val result = ICalWriter().write(calendar)

        // Count VERSION occurrences - should be exactly 1
        val versionCount = result.lines().count { it.trim() == "VERSION:2.0" }
        assertEquals(1, versionCount, "VERSION should appear exactly once")
    }

    @Test
    fun constructorVersionInMiddleIsNotDuplicated() {
        val calendar = VCalendar(
            properties = listOf(
                ICalProperty("PRODID", emptyMap(), "Test"),
                ICalProperty("VERSION", emptyMap(), "2.0"),
                ICalProperty("CALSCALE", emptyMap(), "GREGORIAN")
            ),
            components = emptyList()
        )

        val result = ICalWriter().write(calendar)

        // Should not prepend another VERSION
        val versionCount = result.lines().count { it.trim() == "VERSION:2.0" }
        assertEquals(1, versionCount, "VERSION should appear exactly once")
        assertContains(result, "VERSION:2.0")
    }

    @Test
    fun constructorCaseInsensitiveVersionDetection() {
        val calendar = VCalendar(
            properties = listOf(
                ICalProperty("version", emptyMap(), "2.0"),
                ICalProperty("PRODID", emptyMap(), "Test")
            ),
            components = emptyList()
        )

        val result = ICalWriter().write(calendar)

        // Should detect lowercase "version" and not add another
        val versionCount = result.lines().count { it.contains("VERSION", ignoreCase = true) && it.contains("2.0") }
        assertEquals(1, versionCount, "VERSION should appear exactly once regardless of case")
    }

    // ===========================================
    // Edge Cases
    // ===========================================

    @Test
    fun differentVersionValueIsNotReplaced() {
        // If someone explicitly sets a different version, respect it
        val calendar = VCalendar(
            properties = listOf(
                ICalProperty("VERSION", emptyMap(), "1.0")
            ),
            components = emptyList()
        )

        val result = ICalWriter().write(calendar)

        // Should not add VERSION:2.0, keep VERSION:1.0
        assertContains(result, "VERSION:1.0")
        val version20Count = result.lines().count { it.trim() == "VERSION:2.0" }
        assertEquals(0, version20Count, "Should not add VERSION:2.0 when another version exists")
    }

    @Test
    fun versionPropertyPreservesPositionWhenAlreadyFirst() {
        val calendar = VCalendar(
            properties = listOf(
                ICalProperty("VERSION", emptyMap(), "2.0"),
                ICalProperty("PRODID", emptyMap(), "Test")
            ),
            components = emptyList()
        )

        val result = ICalWriter().write(calendar)
        val lines = result.lines().filter { it.isNotBlank() }

        assertEquals("BEGIN:VCALENDAR", lines[0])
        assertEquals("VERSION:2.0", lines[1])
        assertEquals("PRODID:Test", lines[2])
    }
}
