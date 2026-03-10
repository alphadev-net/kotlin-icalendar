package net.alphadev.icalendar.model.dsl

@ICalDsl
public class PropertyParamsBuilder {
    private val params = mutableMapOf<String, List<String>>()

    public fun value(key: String, value: String) {
        params[key] = listOf(value)
    }

    public fun value(key: String, values: List<String>) {
        params[key] = values
    }

    fun build(): Map<String, List<String>> {
        return params.toMap()
    }
}


