package net.alphadev.icalendar.export

import net.alphadev.icalendar.dsl.vCalendar
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ICalWriterTest {

    @Test
    fun writesBasicVCalendar() {
        val calendar = vCalendar {
            prodId("-//Test//Test//EN")
        }

        val result = ICalWriter().write(calendar)

        assertTrue(result.startsWith("BEGIN:VCALENDAR\r\n"))
        assertTrue(result.endsWith("END:VCALENDAR\r\n"))
        assertContains(result, "VERSION:2.0")
        assertContains(result, "PRODID:-//Test//Test//EN")
    }

    @Test
    fun writesVCalendarWithEvent() {
        val calendar = vCalendar {
            event {
                summary("Test Event")
                property("DTSTART", "20240101T120000Z")
            }
        }

        val result = ICalWriter().write(calendar)

        assertContains(result, "BEGIN:VCALENDAR")
        assertContains(result, "BEGIN:VEVENT")
        assertContains(result, "SUMMARY:Test Event")
        assertContains(result, "DTSTART:20240101T120000Z")
        assertContains(result, "END:VEVENT")
        assertContains(result, "END:VCALENDAR")
    }

    @Test
    fun writesPropertyWithParameters() {
        val calendar = vCalendar {
            event {
                property("DTSTART", "20240101T120000",
                    mapOf("TZID" to listOf("America/New_York"), "VALUE" to listOf("DATE-TIME")))
            }
        }

        val result = ICalWriter().write(calendar)

        assertContains(result, "DTSTART;TZID=America/New_York;VALUE=DATE-TIME:20240101T120000")
    }

    @Test
    fun quotesParameterValuesWithUnsafeChars() {
        val calendar = vCalendar {
            event {
                property("ATTENDEE", "mailto:john@example.com",
                    mapOf("CN" to listOf("John Doe, Manager")))
            }
        }

        val result = ICalWriter().write(calendar)

        assertContains(result, """ATTENDEE;CN="John Doe, Manager":mailto:john@example.com""")
    }

    @Test
    fun quotesParameterValuesWithColons() {
        val calendar = vCalendar {
            event {
                property("ATTENDEE", "mailto:test@example.com",
                    mapOf("CN" to listOf("Company: Inc")))
            }
        }

        val result = ICalWriter().write(calendar)

        assertContains(result, """ATTENDEE;CN="Company: Inc":mailto:test@example.com""")
    }

    @Test
    fun handlesMultipleParameterValues() {
        val calendar = vCalendar {
            event {
                property("CATEGORIES", "MEETING",
                    mapOf("LANGUAGE" to listOf("en", "fr", "de")))
            }
        }

        val result = ICalWriter().write(calendar)

        assertContains(result, "CATEGORIES;LANGUAGE=en,fr,de:MEETING")
    }

    @Test
    fun escapesBackslashInValue() {
        val calendar = vCalendar {
            event {
                summary("Path: C:\\Users\\Test")
            }
        }

        val result = ICalWriter().write(calendar)

        assertContains(result, "SUMMARY:Path: C:\\\\Users\\\\Test")
    }

    @Test
    fun escapesNewlineInValue() {
        val calendar = vCalendar {
            event {
                description("Line 1\nLine 2")
            }
        }

        val result = ICalWriter().write(calendar)

        assertContains(result, "DESCRIPTION:Line 1\\nLine 2")
    }

    @Test
    fun escapesCommaInValue() {
        val calendar = vCalendar {
            event {
                location("Room 1, Building A")
            }
        }

        val result = ICalWriter().write(calendar)

        assertContains(result, "LOCATION:Room 1\\, Building A")
    }

    @Test
    fun escapesSemicolonInValue() {
        val calendar = vCalendar {
            event {
                summary("Note; Important")
            }
        }

        val result = ICalWriter().write(calendar)

        assertContains(result, "SUMMARY:Note\\; Important")
    }

    @Test
    fun removesCarriageReturnInValue() {
        val calendar = vCalendar {
            event {
                description("Line 1\r\nLine 2")
            }
        }

        val result = ICalWriter().write(calendar)

        assertContains(result, "DESCRIPTION:Line 1\\nLine 2")
        assertTrue(!result.contains("\r\n\r\n")) // No double CRLF from escaped \r
    }

    @Test
    fun usesLfWhenConfigured() {
        val calendar = vCalendar { }
        val config = ICalWriter.Config(useCrLf = false)

        val result = ICalWriter(config).write(calendar)

        assertTrue(result.contains("BEGIN:VCALENDAR\n"))
        assertTrue(result.contains("VERSION:2.0\n"))
        assertTrue(result.contains("END:VCALENDAR\n"))
        assertTrue(!result.contains("\r"))
    }

    @Test
    fun usesCrLfByDefault() {
        val calendar = vCalendar { }

        val result = ICalWriter().write(calendar)

        assertTrue(result.contains("\r\n"))
    }

    @Test
    fun foldsLongLinesByDefault() {
        val longValue = "A".repeat(100)
        val calendar = vCalendar {
            event {
                summary(longValue)
            }
        }

        val result = ICalWriter().write(calendar)

        // Should contain folding (CRLF + space)
        assertTrue(result.contains("\r\n "), "Long lines should be folded by default")
    }

    @Test
    fun doesNotFoldWhenConfigured() {
        val longValue = "A".repeat(100)
        val calendar = vCalendar {
            event {
                summary(longValue)
            }
        }
        val config = ICalWriter.Config(foldLines = false)

        val result = ICalWriter(config).write(calendar)

        // Count CRLF occurrences - should only be at end of lines, not folding
        val crlfCount = result.windowed(2).count { it == "\r\n" }
        val lineCount = result.lines().size - 1 // -1 for trailing empty
        assertEquals(lineCount, crlfCount, "Should have no folding CRLFs")
    }

    @Test
    fun writesNestedComponents() {
        val calendar = vCalendar {
            event {
                summary("Meeting")
                alarm {
                    displayAction()
                    triggerBefore(kotlin.time.Duration.parse("PT15M"))
                }
            }
        }

        val result = ICalWriter().write(calendar)

        assertContains(result, "BEGIN:VCALENDAR")
        assertContains(result, "BEGIN:VEVENT")
        assertContains(result, "BEGIN:VALARM")
        assertContains(result, "ACTION:DISPLAY")
        assertContains(result, "TRIGGER:-PT15M")
        assertContains(result, "END:VALARM")
        assertContains(result, "END:VEVENT")
        assertContains(result, "END:VCALENDAR")
    }

    @Test
    fun writesMultipleEvents() {
        val calendar = vCalendar {
            event {
                summary("Event 1")
            }
            event {
                summary("Event 2")
            }
        }

        val result = ICalWriter().write(calendar)

        assertContains(result, "SUMMARY:Event 1")
        assertContains(result, "SUMMARY:Event 2")
        val beginCount = result.windowed(12).count { it == "BEGIN:VEVENT" }
        assertEquals(2, beginCount)
    }

    @Test
    fun writeAllWritesMultipleCalendars() {
        val calendar1 = vCalendar {
            prodId("Calendar1")
        }
        val calendar2 = vCalendar {
            prodId("Calendar2")
        }

        val result = ICalWriter().writeAll(listOf(calendar1, calendar2))

        assertContains(result, "PRODID:Calendar1")
        assertContains(result, "PRODID:Calendar2")
        val beginCount = result.windowed(15).count { it == "BEGIN:VCALENDAR" }
        assertEquals(2, beginCount)
    }

    @Test
    fun toICalStringExtension() {
        val calendar = vCalendar { }

        val result = calendar.toICalString()

        assertContains(result, "BEGIN:VCALENDAR")
        assertContains(result, "VERSION:2.0")
        assertContains(result, "END:VCALENDAR")
    }

    @Test
    fun toICalStringWithConfig() {
        val calendar = vCalendar { }
        val config = ICalWriter.Config(useCrLf = false, foldLines = false)

        val result = calendar.toICalString(config)

        assertTrue(result.contains("BEGIN:VCALENDAR\n"))
        assertTrue(!result.contains("\r"))
    }

    @Test
    fun propertyOrderIsPreserved() {
        val calendar = vCalendar {
            prodId("Test")
            calScale("GREGORIAN")
            method("PUBLISH")
        }

        val result = ICalWriter().write(calendar)
        val lines = result.lines()

        val versionIndex = lines.indexOfFirst { it.contains("VERSION") }
        val prodidIndex = lines.indexOfFirst { it.contains("PRODID") }
        val calscaleIndex = lines.indexOfFirst { it.contains("CALSCALE") }
        val methodIndex = lines.indexOfFirst { it.contains("METHOD") }

        assertTrue(versionIndex < prodidIndex)
        assertTrue(prodidIndex < calscaleIndex)
        assertTrue(calscaleIndex < methodIndex)
    }

    @Test
    fun componentOrderIsPreserved() {
        val calendar = vCalendar {
            event { uid("1") }
            event { uid("2") }
            event { uid("3") }
        }

        val result = ICalWriter().write(calendar)
        val lines = result.lines()

        val uid1Index = lines.indexOfFirst { it.contains("UID:1") }
        val uid2Index = lines.indexOfFirst { it.contains("UID:2") }
        val uid3Index = lines.indexOfFirst { it.contains("UID:3") }

        assertTrue(uid1Index < uid2Index)
        assertTrue(uid2Index < uid3Index)
    }

    @Test
    fun emptyCalendar() {
        // ICalWriter always adds VERSION:2.0 if not present
        val calendar = vCalendar { }

        val result = ICalWriter().write(calendar)

        assertEquals("BEGIN:VCALENDAR\r\nVERSION:2.0\r\nEND:VCALENDAR\r\n", result)
    }

    @Test
    fun doesNotDuplicateVersion() {
        // If VERSION already exists, don't add another
        val calendar = vCalendar {
            property("VERSION", "2.0")
        }

        val result = ICalWriter().write(calendar)

        assertEquals("BEGIN:VCALENDAR\r\nVERSION:2.0\r\nEND:VCALENDAR\r\n", result)
    }

    @Test
    fun propertyWithNoValue() {
        val calendar = vCalendar {
            property("SUMMARY", "")
        }

        val result = ICalWriter().write(calendar)

        assertContains(result, "SUMMARY:")
    }

    @Test
    fun complexEscaping() {
        val value = "Line 1\nLine 2; with semicolon, comma\\and backslash"
        val calendar = vCalendar {
            event {
                description(value)
            }
        }

        val result = ICalWriter().write(calendar)

        assertContains(result, "DESCRIPTION:Line 1\\nLine 2\\; with semicolon\\, comma\\\\and backslash")
    }
}
