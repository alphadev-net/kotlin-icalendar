package net.alphadev.icalendar.export

object LineFolding {
    private const val MAX_LINE_OCTETS = 75
    private const val CRLF = "\r\n"
    private const val FOLD_PREFIX = " "

    fun fold(line: String): String {
        if (line.encodeToByteArray().size <= MAX_LINE_OCTETS) return line
        return foldByOctets(line)
    }

    private fun foldByOctets(line: String) = buildString {
        var currentLineOctets = 0
        var index = 0

        while (index < line.length) {
            // Handle surrogate pairs properly
            val char = line[index]
            val substring = if (char.isHighSurrogate() && index + 1 < line.length) {
                // This is a surrogate pair, take both chars
                line.substring(index, index + 2)
            } else {
                // Single char (or unpaired surrogate, which we treat as single)
                char.toString()
            }

            val charOctetCount = substring.encodeToByteArray().size

            if (currentLineOctets + charOctetCount > MAX_LINE_OCTETS) {
                append(CRLF)
                append(FOLD_PREFIX)
                currentLineOctets = 1 // The space counts
            }
            append(substring)
            currentLineOctets += charOctetCount
            index += substring.length
        }
    }
}
