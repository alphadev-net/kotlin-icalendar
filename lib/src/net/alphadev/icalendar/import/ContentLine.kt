package net.alphadev.icalendar.import

internal data class ContentLine(
    val name: String,
    val parameters: Map<String, List<String>>,
    val value: String
) {
    val isBegin: Boolean get() = name.equals("BEGIN", ignoreCase = true)
    val isEnd: Boolean get() = name.equals("END", ignoreCase = true)
    val componentName: String get() = value.uppercase()
}

internal object ContentLineParser {

    fun parse(line: String): ContentLine {
        val (nameAndParams, value) = splitOnValue(line)
        val (name, parameters) = parseNameAndParams(nameAndParams)
        return ContentLine(name, parameters, value)
    }

    private fun splitOnValue(line: String): Pair<String, String> {
        var inQuotes = false
        for (i in line.indices) {
            when (line[i]) {
                '"' -> inQuotes = !inQuotes
                ':' -> if (!inQuotes) {
                    return line.substring(0, i) to line.substring(i + 1)
                }
            }
        }
        return line to ""
    }

    private fun parseNameAndParams(input: String): Pair<String, Map<String, List<String>>> {
        val parts = splitOnUnquotedSemicolons(input)
        val name = parts.first().uppercase()
        val params = mutableMapOf<String, MutableList<String>>()

        for (i in 1 until parts.size) {
            val (paramName, paramValue) = parseParameter(parts[i])
            params.getOrPut(paramName.uppercase()) { mutableListOf() }.addAll(paramValue)
        }

        return name to params
    }

    private fun splitOnUnquotedSemicolons(input: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false

        for (char in input) {
            when {
                char == '"' -> { inQuotes = !inQuotes; current.append(char) }
                char == ';' && !inQuotes -> { result.add(current.toString()); current.clear() }
                else -> current.append(char)
            }
        }
        result.add(current.toString())
        return result
    }

    private fun parseParameter(param: String): Pair<String, List<String>> {
        val eqIndex = param.indexOf('=')
        if (eqIndex == -1) return param to listOf("true")

        val paramName = param.substring(0, eqIndex)
        val paramValueRaw = param.substring(eqIndex + 1)

        val paramValue = if (paramValueRaw.startsWith("\"") && paramValueRaw.endsWith("\"")) {
            listOf(paramValueRaw.substring(1, paramValueRaw.length - 1))
        } else {
            paramValueRaw.split(",").map { it.trim() }
        }

        return paramName to paramValue
    }
}
