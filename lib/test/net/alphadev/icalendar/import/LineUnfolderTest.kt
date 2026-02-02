package net.alphadev.icalendar.import

import kotlin.test.Test
import kotlin.test.assertEquals

class LineUnfolderTest {

    @Test
    fun emptyStringReturnsEmptyList() {
        val result = LineUnfolder.unfold("")
        assertEquals(emptyList(), result)
    }

    @Test
    fun singleLineWithoutFolding() {
        val result = LineUnfolder.unfold("SUMMARY:Test Event")
        assertEquals(listOf("SUMMARY:Test Event"), result)
    }

    @Test
    fun multipleUnfoldedLines() {
        val input = "BEGIN:VEVENT\nSUMMARY:Test\nEND:VEVENT"
        val result = LineUnfolder.unfold(input)
        assertEquals(listOf("BEGIN:VEVENT", "SUMMARY:Test", "END:VEVENT"), result)
    }

    @Test
    fun foldedLineWithSpace() {
        val input = "DESCRIPTION:This is a very long line that has been\n folded with a space"
        val result = LineUnfolder.unfold(input)
        assertEquals(listOf("DESCRIPTION:This is a very long line that has beenfolded with a space"), result)
    }

    @Test
    fun foldedLineWithTab() {
        val input = "DESCRIPTION:This is a very long line that has been\n\tfolded with a tab"
        val result = LineUnfolder.unfold(input)
        assertEquals(listOf("DESCRIPTION:This is a very long line that has beenfolded with a tab"), result)
    }

    @Test
    fun multipleFoldedLines() {
        val input = "DESCRIPTION:Line one\n continues here\n and here\n and ends"
        val result = LineUnfolder.unfold(input)
        assertEquals(listOf("DESCRIPTION:Line onecontinues hereand hereand ends"), result)
    }

    @Test
    fun crlfLineEndings() {
        val input = "BEGIN:VEVENT\r\nSUMMARY:Test\r\nEND:VEVENT"
        val result = LineUnfolder.unfold(input)
        assertEquals(listOf("BEGIN:VEVENT", "SUMMARY:Test", "END:VEVENT"), result)
    }

    @Test
    fun crlfWithFolding() {
        val input = "DESCRIPTION:This is folded\r\n with CRLF"
        val result = LineUnfolder.unfold(input)
        assertEquals(listOf("DESCRIPTION:This is foldedwith CRLF"), result)
    }

    @Test
    fun mixedLineEndings() {
        val input = "LINE1:Value\nLINE2:Value\r\nLINE3:Value"
        val result = LineUnfolder.unfold(input)
        assertEquals(listOf("LINE1:Value", "LINE2:Value", "LINE3:Value"), result)
    }

    @Test
    fun emptyLinesAreSkipped() {
        val input = "LINE1:Value\n\nLINE2:Value"
        val result = LineUnfolder.unfold(input)
        assertEquals(listOf("LINE1:Value", "LINE2:Value"), result)
    }

    @Test
    fun foldingAtStartOfInput() {
        val input = " Folded start\nNormal line"
        val result = LineUnfolder.unfold(input)
        assertEquals(listOf(" Folded start", "Normal line"), result)
    }

    @Test
    fun consecutiveFoldedLines() {
        val input = "DESCRIPTION:Start\n continuation1\n continuation2\nNEXT:Property"
        val result = LineUnfolder.unfold(input)
        assertEquals(listOf("DESCRIPTION:Startcontinuation1continuation2", "NEXT:Property"), result)
    }

    @Test
    fun realWorldExample() {
        val input = """BEGIN:VEVENT
DTSTART:20240101T120000Z
SUMMARY:Team Meeting
DESCRIPTION:This is a long description that exceeds the 75 character
  limit recommended by RFC 5545 and therefore needs to be folded across
  multiple lines to maintain compatibility.
LOCATION:Conference Room A
END:VEVENT"""
        val result = LineUnfolder.unfold(input)
        assertEquals(
            listOf(
                "BEGIN:VEVENT",
                "DTSTART:20240101T120000Z",
                "SUMMARY:Team Meeting",
                "DESCRIPTION:This is a long description that exceeds the 75 character limit recommended by RFC 5545 and therefore needs to be folded across multiple lines to maintain compatibility.",
                "LOCATION:Conference Room A",
                "END:VEVENT"
            ),
            result
        )
    }

    @Test
    fun trailingNewline() {
        val input = "LINE1:Value\nLINE2:Value\n"
        val result = LineUnfolder.unfold(input)
        assertEquals(listOf("LINE1:Value", "LINE2:Value"), result)
    }

    @Test
    fun onlyNewlines() {
        val input = "\n\n\n"
        val result = LineUnfolder.unfold(input)
        assertEquals(emptyList(), result)
    }

    @Test
    fun spaceAfterNormalLineBreak() {
        // Space at start of new line indicates folding
        val input = "PROP1:Value1\n PROP2:Value2"
        val result = LineUnfolder.unfold(input)
        assertEquals(listOf("PROP1:Value1PROP2:Value2"), result)
    }

    @Test
    fun noSpaceAfterLineBreak() {
        // No space means it's a real line break
        val input = "PROP1:Value1\nPROP2:Value2"
        val result = LineUnfolder.unfold(input)
        assertEquals(listOf("PROP1:Value1", "PROP2:Value2"), result)
    }
}
