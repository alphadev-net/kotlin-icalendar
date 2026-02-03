package net.alphadev.icalendar.cli

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import net.alphadev.icalendar.import.parseICalendar
import net.alphadev.icalendar.model.VCalendar

fun readICalFromFile(file: Path): List<VCalendar> {
    return try {
        val fileContents = SystemFileSystem.source(file)
            .buffered()
            .readString()

        parseICalendar(fileContents)
    } catch (ex: Throwable) {
        ex.printStackTrace()
        emptyList()
    }
}
