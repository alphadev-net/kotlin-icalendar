package net.alphadev.icalendar.model.dsl

import net.alphadev.icalendar.model.AlarmAction
import net.alphadev.icalendar.model.ICalProperty
import net.alphadev.icalendar.model.VAlarm
import net.alphadev.icalendar.model.formatUtc
import kotlin.time.Duration
import kotlin.time.Instant

@ICalDsl
public class VAlarmBuilder: IComponentBuilder() {

    public fun displayAction() {
        property("ACTION", "DISPLAY")
    }

    public fun audioAction() {
        property("ACTION", "AUDIO")
    }

    public fun emailAction() {
        property("ACTION", "EMAIL")
    }

    public fun action(value: AlarmAction) {
        property("ACTION", value.name)
    }

    public fun triggerBefore(duration: Duration) {
        property("TRIGGER", (-duration).toIsoString())
    }

    public fun triggerAfter(duration: Duration) {
        property("TRIGGER", duration.toIsoString())
    }

    public fun triggerBeforeEnd(duration: Duration) {
        property("TRIGGER", (-duration).toIsoString(), mapOf("RELATED" to listOf("END")))
    }

    public fun triggerAfterEnd(duration: Duration) {
        property("TRIGGER", duration.toIsoString(), mapOf("RELATED" to listOf("END")))
    }

    public fun triggerAt(instant: Instant) {
        properties.removeAll { it.name.equals("TRIGGER", ignoreCase = true) }
        properties.add(ICalProperty("TRIGGER", mapOf("VALUE" to listOf("DATE-TIME")), instant.formatUtc(), instant))
    }

    public fun description(value: String) {
        property("DESCRIPTION", value)
    }

    public fun summary(value: String) {
        property("SUMMARY", value)
    }

    public fun attendee(email: String) {
        properties.add(ICalProperty("ATTENDEE", emptyMap(), "mailto:$email"))
    }

    public fun attach(uri: String) {
        property("ATTACH", uri)
    }

    public fun repeat(count: Int, interval: Duration) {
        property("REPEAT", count.toString())
        property("DURATION", interval.toIsoString())
    }

    public fun build(): VAlarm {
        return VAlarm(properties.toList())
    }
}
