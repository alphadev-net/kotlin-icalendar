package net.alphadev.icalendar.model

sealed interface ICalComponent {
 val properties: List<ICalProperty>
    val components: List<ICalComponent>
    val componentName: String
}
