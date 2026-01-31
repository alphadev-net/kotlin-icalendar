package net.alphadev.icalendar.import

internal object LineUnfolder {

    fun unfold(input: String): List<String> {
        if (input.isEmpty()) return emptyList()

        val result = mutableListOf<String>()
        val currentLine = StringBuilder()

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
                        if (currentLine.isNotEmpty()) {
                            result.add(currentLine.toString())
                            currentLine.clear()
                        }
                    }
                }
                else -> {
                    currentLine.append(char)
                    i++
                }
            }
        }

        if (currentLine.isNotEmpty()) {
            result.add(currentLine.toString())
        }

        return result
    }
}
