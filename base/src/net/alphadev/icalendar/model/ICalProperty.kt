package net.alphadev.icalendar.model

import kotlin.time.Instant

data class ICalProperty(
    val name: String,
    val parameters: Map<String, List<String>> = emptyMap(),
    val value: String,
    val instant: Instant? = null
) {
    fun parameter(name: String): String? = parameters[name]?.firstOrNull()
    val isExtension: Boolean get() = name.startsWith("X-", ignoreCase = true)
    val valueType: String? get() = parameter("VALUE")
    val tzid: String? get() = parameter("TZID")
}
