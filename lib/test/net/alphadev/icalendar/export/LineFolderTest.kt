package net.alphadev.icalendar.export

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LineFoldingTest {

    @Test
    fun shortLineIsNotFolded() {
        val line = "SUMMARY:Short event"
        val result = LineFolding.fold(line)
        assertEquals(line, result)
    }

    @Test
    fun emptyLineIsNotFolded() {
        val line = ""
        val result = LineFolding.fold(line)
        assertEquals("", result)
    }

    @Test
    fun exactly75OctetsIsNotFolded() {
        // Create a line with exactly 75 octets (75 ASCII characters)
        val line = "X".repeat(75)
        val result = LineFolding.fold(line)
        assertEquals(line, result)
        assertEquals(75, line.encodeToByteArray().size)
    }

    @Test
    fun line76OctetsIsFolded() {
        // 76 ASCII characters = 76 octets, should fold
        val line = "X".repeat(76)
        val result = LineFolding.fold(line)
        assertTrue(result.contains("\r\n "))
        assertEquals(line, result.replace("\r\n ", ""))
    }

    @Test
    fun longAsciiLineIsFolded() {
        val line = "DESCRIPTION:This is a very long description that exceeds the seventy-five octet limit and should be folded"
        val result = LineFolding.fold(line)
        assertTrue(result.contains("\r\n "))
        assertEquals(line, result.replace("\r\n ", ""))
    }

    @Test
    fun foldedLineUnfoldsCorrectly() {
        val line = "A".repeat(100)
        val folded = LineFolding.fold(line)
        val unfolded = folded.replace("\r\n ", "")
        assertEquals(line, unfolded)
    }

    @Test
    fun multiByteCharactersAreFoldedCorrectly() {
        // Test with multi-byte characters that aren't surrogate pairs
        // ñ (U+00F1) is 2 bytes in UTF-8 and 1 char in String
        val line = "SUMMARY:" + "ñ".repeat(40)
        val lineBytes = line.encodeToByteArray().size

        assertTrue(lineBytes > 75, "Test line should exceed 75 octets, got $lineBytes")

        val result = LineFolding.fold(line)
        assertTrue(result.contains("\r\n "), "Line with $lineBytes octets should be folded")
        assertEquals(line, result.replace("\r\n ", ""))
    }

    @Test
    fun emojiSurrogatePairsAreFoldedCorrectly() {
        // UTF-8 emoji: 😀 is 4 octets and 2 chars (surrogate pair)
        // This tests proper handling of surrogate pairs during folding
        val emoji = "😀"

        // Use enough emoji to definitely exceed 75 octets
        // Each emoji is 4 bytes, so 20 emoji = 80 bytes + 8 for "SUMMARY:" = 88 bytes
        val line = "SUMMARY:" + emoji.repeat(20)
        val lineBytes = line.encodeToByteArray().size

        assertTrue(lineBytes > 75, "Test line should exceed 75 octets, got $lineBytes")

        val result = LineFolding.fold(line)

        assertTrue(result.contains("\r\n "),
            "Line with $lineBytes octets should be folded")
        assertEquals(line, result.replace("\r\n ", ""))
    }

    @Test
    fun multiByteCharactersNotSplitAcrossFold() {
        // Ensure multi-byte characters aren't split in the middle
        val line = "DESCRIPTION:" + "Ä".repeat(40) // Ä is 2 octets in UTF-8
        val result = LineFolding.fold(line)

        // Verify we can unfold and get the original
        val unfolded = result.replace("\r\n ", "")
        assertEquals(line, unfolded)

        // Verify all characters are intact
        assertTrue(unfolded.count { it == 'Ä' } == 40)
    }

    @Test
    fun mixedAsciiAndMultiByteCharacters() {
        val line = "SUMMARY:Meeting at Café ☕ with 日本語 text and more English words to exceed limit"
        val result = LineFolding.fold(line)

        if (line.encodeToByteArray().size > 75) {
            assertTrue(result.contains("\r\n "))
        }
        assertEquals(line, result.replace("\r\n ", ""))
    }

    @Test
    fun veryLongLineHasMultipleFolds() {
        val line = "DESCRIPTION:" + "X".repeat(200)
        val result = LineFolding.fold(line)

        val foldCount = result.count { it == '\r' }
        assertTrue(foldCount >= 2, "Expected multiple folds for 200+ character line")
        assertEquals(line, result.replace("\r\n ", ""))
    }

    @Test
    fun foldingPreservesAllCharacters() {
        val line = "DESCRIPTION:Special chars !@#$%^&*()_+-=[]{}|;:',.<>?/~`" + "X".repeat(50)
        val result = LineFolding.fold(line)
        assertEquals(line, result.replace("\r\n ", ""))
    }

    @Test
    fun eachFoldedLineSegmentUnder75Octets() {
        val line = "A".repeat(200)
        val result = LineFolding.fold(line)

        val segments = result.split("\r\n")
        for ((index, segment) in segments.withIndex()) {
            val segmentToCheck = if (index > 0) segment.removePrefix(" ") else segment
            val octets = segmentToCheck.encodeToByteArray().size
            assertTrue(octets <= 75, "Segment $index has $octets octets, expected <= 75")
        }
    }

    @Test
    fun foldingUsesCorrectCRLFAndSpace() {
        val line = "X".repeat(100)
        val result = LineFolding.fold(line)

        assertTrue(result.contains("\r\n "))
        assertTrue(result.split("\r\n").drop(1).all { it.startsWith(" ") })
    }

    @Test
    fun realWorldVEventProperty() {
        val line = "DESCRIPTION:This is a comprehensive event description that includes multiple details about the meeting location\\, agenda items\\, and expected participants. It definitely exceeds the seventy-five octet limitation."
        val result = LineFolding.fold(line)

        assertTrue(result.contains("\r\n "))
        assertEquals(line, result.replace("\r\n ", ""))

        // Verify each segment is properly sized
        val segments = result.split("\r\n")
        for ((index, segment) in segments.withIndex()) {
            val segmentToCheck = if (index > 0) segment.removePrefix(" ") else segment
            assertTrue(segmentToCheck.encodeToByteArray().size <= 75)
        }
    }

    @Test
    fun chineseCharactersAreFoldedCorrectly() {
        // Chinese characters are typically 3 octets in UTF-8
        val line = "SUMMARY:" + "会".repeat(30)
        val lineBytes = line.encodeToByteArray().size

        // Verify our assumption that this exceeds 75 octets
        assertTrue(lineBytes > 75, "Test line should exceed 75 octets, got $lineBytes")

        val result = LineFolding.fold(line)
        assertTrue(result.contains("\r\n "), "Line with $lineBytes octets should be folded")
        assertEquals(line, result.replace("\r\n ", ""))
    }

    @Test
    fun lineWith74OctetsIsNotFolded() {
        val line = "X".repeat(74)
        val result = LineFolding.fold(line)
        assertEquals(line, result)
        assertEquals(74, line.encodeToByteArray().size)
    }

    @Test
    fun foldingAccountsForFoldPrefixSpace() {
        // After first fold, continuation lines start with space (1 octet)
        // So max content per continuation line is 74 octets
        val line = "A".repeat(200)
        val result = LineFolding.fold(line)

        val segments = result.split("\r\n")
        for (i in 1 until segments.size) {
            // Each continuation segment including the leading space should be <= 75 octets
            assertTrue(segments[i].encodeToByteArray().size <= 75)
        }
    }
}
