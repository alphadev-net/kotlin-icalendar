package net.alphadev.icalendar.cli.sources

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import net.alphadev.icalendar.import.parseICalendar
import net.alphadev.icalendar.model.VCalendar

fun readICalFromFile(file: Path): List<VCalendar> {
    return try {
        val fileSource = SystemFileSystem.source(file)
            .buffered()

        parseICalendar(fileSource)
    } catch (ex: Throwable) {
        ex.printStackTrace()
        emptyList()
    }
}
