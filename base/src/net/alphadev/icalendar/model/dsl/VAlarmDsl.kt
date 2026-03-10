package net.alphadev.icalendar.model.dsl

import net.alphadev.icalendar.model.AlarmAction
import net.alphadev.icalendar.model.ICalProperty
import net.alphadev.icalendar.model.VAlarm
import net.alphadev.icalendar.model.formatUtc
import kotlin.time.Duration
import kotlin.time.Instant

@ICalDsl
public class VAlarmBuilder {
    private val builderState = IComponentBuilder()

    public fun displayAction() {
        builderState.property("ACTION", "DISPLAY")
    }

    public fun audioAction() {
        builderState.property("ACTION", "AUDIO")
    }

    public fun emailAction() {
        builderState.property("ACTION", "EMAIL")
    }

    public fun action(value: AlarmAction) {
        builderState.property("ACTION", value.name)
    }

    public fun triggerBefore(duration: Duration) {
        builderState.property("TRIGGER", (-duration).toIsoString())
    }

    public fun triggerAfter(duration: Duration) {
        builderState.property("TRIGGER", duration.toIsoString())
    }

    public fun triggerBeforeEnd(duration: Duration) {
        builderState.property("TRIGGER", (-duration).toIsoString(), mapOf("RELATED" to listOf("END")))
    }

    public fun triggerAfterEnd(duration: Duration) {
        builderState.property("TRIGGER", duration.toIsoString(), mapOf("RELATED" to listOf("END")))
    }

    public fun triggerAt(instant: Instant) {
        builderState.properties.removeAll { it.name.equals("TRIGGER", ignoreCase = true) }
        builderState.properties.add(ICalProperty("TRIGGER", mapOf("VALUE" to listOf("DATE-TIME")), instant.formatUtc(), instant))
    }

    public fun description(value: String) {
        builderState.property("DESCRIPTION", value)
    }

    public fun summary(value: String) {
        builderState.property("SUMMARY", value)
    }

    public fun attendee(email: String) {
        builderState.properties.add(ICalProperty("ATTENDEE", emptyMap(), "mailto:$email"))
    }

    public fun attach(uri: String) {
        builderState.property("ATTACH", uri)
    }

    public fun repeat(count: Int, interval: Duration) {
        builderState.property("REPEAT", count.toString())
        builderState.property("DURATION", interval.toIsoString())
    }

    fun build(): VAlarm {
        return VAlarm(builderState.properties.toList())
    }
}
