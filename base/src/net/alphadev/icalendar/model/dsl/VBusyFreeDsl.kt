package net.alphadev.icalendar.model.dsl

import net.alphadev.icalendar.model.ICalProperty
import net.alphadev.icalendar.model.VFreeBusy
import net.alphadev.icalendar.model.formatUtc
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

@ICalDsl
public class VFreeBusyBuilder {

    private val builderState = IComponentBuilder()

    init {
        val now = Clock.System.now()
        builderState.propertyWithInstant("DTSTAMP", now.formatUtc(), instant = now)
        builderState.property("UID", Uuid.random().toString())
    }

    public fun uid(value: String) {
        builderState.property("UID", value)
    }

    public fun organizer(email: String, name: String? = null) {
        val params = if (name != null) mapOf("CN" to listOf(name)) else emptyMap()
        builderState.property("ORGANIZER", "mailto:$email", params)
    }

    public fun attendee(email: String, name: String? = null, params: Map<String, List<String>> = emptyMap()) {
        val combinedParams = if (name != null) params + ("CN" to listOf(name)) else params
        builderState.properties.add(ICalProperty("ATTENDEE", combinedParams, "mailto:$email"))
    }

    public fun dtStart(value: Instant) {
        builderState.propertyWithInstant("DTSTART", value.formatUtc(), instant = value)
    }

    public fun dtEnd(value: Instant) {
        builderState.propertyWithInstant("DTEND", value.formatUtc(), instant = value)
    }

    public fun url(value: String) {
        builderState.property("URL", value)
    }

    public fun freeBusy(period: String, fbType: String = "BUSY") {
        builderState.properties.add(ICalProperty("FREEBUSY", mapOf("FBTYPE" to listOf(fbType)), period))
    }

    fun build(): VFreeBusy = VFreeBusy(builderState.properties.toList(), builderState.components.toList())
}
