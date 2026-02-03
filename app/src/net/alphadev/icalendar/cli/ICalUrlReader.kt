package net.alphadev.icalendar.cli

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import net.alphadev.icalendar.import.parseICalendar
import net.alphadev.icalendar.model.VCalendar

suspend fun readICalFromUrl(iCalUrl: String): List<VCalendar> {
    return try {
        val remoteContents = HttpClient()
            .get(iCalUrl)
            .bodyAsText()

        parseICalendar(remoteContents)
    } catch (ex: Throwable) {
        ex.printStackTrace()
        emptyList()
    }
}
