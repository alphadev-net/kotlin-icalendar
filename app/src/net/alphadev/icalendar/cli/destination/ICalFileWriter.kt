package net.alphadev.icalendar.cli.destination

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString
import net.alphadev.icalendar.export.toICalString
import net.alphadev.icalendar.model.VCalendar

fun List<VCalendar>.writeICalFile(destFile: Path) {
    try {
        val fileContents = toICalString()

        SystemFileSystem.sink(destFile)
            .buffered()
            .use { it.writeString(fileContents, 0, fileContents.length) }

    } catch (ex: Throwable) {
        ex.printStackTrace()
    }
}
