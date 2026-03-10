package net.alphadev.icalendar.transform

import net.alphadev.icalendar.model.ICalComponent
import net.alphadev.icalendar.model.VCalendar

public fun VCalendar.mapComponents(transform: (ICalComponent) -> ICalComponent?): VCalendar {
    return copy(components = components.mapNotNull(transform))
}

public fun VCalendar.flatMapComponents(transform: (ICalComponent) -> List<ICalComponent>): VCalendar {
    return copy(components = components.flatMap(transform))
}

public inline fun <reified Type : ICalComponent> VCalendar.mapType(crossinline transform: (Type) -> Type?): VCalendar {
    return mapComponents {
        if (Type::class.isInstance(it)) {
            transform(it as Type)
        } else {
            it
        }
    }
}

public inline fun <reified Type : ICalComponent> VCalendar.flatMapType(crossinline transform: (Type) -> List<Type>): VCalendar {
    return flatMapComponents {
        if (Type::class.isInstance(it)) {
            transform(it as Type)
        } else {
            listOf(it)
        }
    }
}
