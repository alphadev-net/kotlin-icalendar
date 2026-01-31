package net.alphadev.icalendar.model

data class ICalProperty(
    val name: String,
    val parameters: Map<String, List<String>> = emptyMap(),
    val value: String
) {
    fun parameter(name: String): String? = parameters[name]?.firstOrNull()
    val isExtension: Boolean get() = name.startsWith("X-", ignoreCase = true)
    val valueType: String? get() = parameter("VALUE")
    val tzid: String? get() = parameter("TZID")
}
