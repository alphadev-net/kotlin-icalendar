package net.alphadev.icalendar.model.dsl

import net.alphadev.icalendar.model.AlarmAction
import net.alphadev.icalendar.model.ICalProperty
import net.alphadev.icalendar.model.VAlarm
import net.alphadev.icalendar.model.formatUtc
import kotlin.time.Duration
import kotlin.time.Instant

@ICalDsl
class VAlarmBuilder: IComponentBuilder() {

    fun displayAction() = property("ACTION", "DISPLAY")
    fun audioAction() = property("ACTION", "AUDIO")
    fun emailAction() = property("ACTION", "EMAIL")
    fun action(value: AlarmAction) = property("ACTION", value.name)

    fun triggerBefore(duration: Duration) {
        property("TRIGGER", (-duration).toIsoString())
    }

    fun triggerAfter(duration: Duration) {
        property("TRIGGER", duration.toIsoString())
    }

    fun triggerBeforeEnd(duration: Duration) {
        property("TRIGGER", (-duration).toIsoString(), mapOf("RELATED" to listOf("END")))
    }

    fun triggerAfterEnd(duration: Duration) {
        property("TRIGGER", duration.toIsoString(), mapOf("RELATED" to listOf("END")))
    }

    fun triggerAt(instant: Instant) {
        properties.removeAll { it.name.equals("TRIGGER", ignoreCase = true) }
        properties.add(ICalProperty("TRIGGER", mapOf("VALUE" to listOf("DATE-TIME")), instant.formatUtc(), instant))
    }

    fun description(value: String) = property("DESCRIPTION", value)
    fun summary(value: String) = property("SUMMARY", value)

    fun attendee(email: String) {
        properties.add(ICalProperty("ATTENDEE", emptyMap(), "mailto:$email"))
    }

    fun attach(uri: String) = property("ATTACH", uri)

    fun repeat(count: Int, interval: Duration) {
        property("REPEAT", count.toString())
        property("DURATION", interval.toIsoString())
    }

    fun build(): VAlarm = VAlarm(properties.toList())
}
