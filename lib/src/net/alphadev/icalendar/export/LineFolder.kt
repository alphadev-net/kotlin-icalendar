package net.alphadev.icalendar.export

internal object LineFolding {

    private const val MAX_LINE_OCTETS = 75
    private const val CRLF = "\r\n"
    private const val FOLD_PREFIX = " "

    fun fold(line: String): String {
        if (line.encodeToByteArray().size <= MAX_LINE_OCTETS) return line
        return foldByOctets(line)
    }

    private fun foldByOctets(line: String): String {
        val result = StringBuilder()
        var currentLineOctets = 0

        for (char in line) {
            val charOctetCount = char.toString().encodeToByteArray().size

            if (currentLineOctets + charOctetCount > MAX_LINE_OCTETS) {
                result.append(CRLF)
                result.append(FOLD_PREFIX)
                currentLineOctets = 1 // The space counts
            }

            result.append(char)
            currentLineOctets += charOctetCount
        }

        return result.toString()
    }
}
