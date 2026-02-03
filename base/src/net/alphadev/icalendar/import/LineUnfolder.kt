package net.alphadev.icalendar.import

object LineUnfolder {

    fun unfold(input: String) = buildList {
        val lineBuffer = StringBuilder()

        fun StringBuilder.flushLine() {
            if (isNotEmpty()) {
                add(toString())
                clear()
            }
        }

        var i = 0
        while (i < input.length) {
            val char = input[i]

            when {
                char == '\r' || char == '\n' -> {
                    val lineEndLength = if (char == '\r' && input.getOrNull(i + 1) == '\n') 2 else 1
                    i += lineEndLength

                    val nextChar = input.getOrNull(i)
                    if (nextChar == ' ' || nextChar == '\t') {
                        i++
                    } else {
                        lineBuffer.flushLine()
                    }
                }
                else -> {
                    lineBuffer.append(char)
                    i++
                }
            }
        }

        lineBuffer.flushLine()
    }
}
