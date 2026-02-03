package net.alphadev.icalendar.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.groups.required
import com.github.ajalt.clikt.parameters.groups.single
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import net.alphadev.icalendar.cli.destination.writeICalFile
import net.alphadev.icalendar.cli.sources.readICalFromFile
import net.alphadev.icalendar.cli.sources.readICalFromUrl
import net.alphadev.icalendar.export.toICalString
import kotlin.time.measureTime

fun main(args: Array<String>) = iCalendar().main(args)

private sealed class Source {
    data class Url(val url: String): Source()
    data class File(val path: Path): Source()
}

internal class iCalendar: CliktCommand(name = "icalendar") {
    override val printHelpOnEmptyArgs = true

    private val source: Source by mutuallyExclusiveOptions<Source>(
        option("--file", "-f", metavar = "FILE").convert { Source.File(Path(it.toString())) },
        option("--url", "-i").convert { Source.Url(it) },
        name = "Source"
    ).single().required()

    private val output: Path? by option(
        "--output", "-o", metavar = "FILE",
        help = "If not specified, the output will be printed to STDOUT")
        .file(mustExist = false, canBeDir = false, mustBeWritable = true)
        .convert { Path(it.toString()) }

    override fun run() {
        val duration = measureTime {
            val iCalContents = when (val source = source) {
                is Source.File -> readICalFromFile(source.path)
                is Source.Url -> runBlocking { readICalFromUrl(source.url) }
            }

            when (val output = output) {
                null -> println(iCalContents.toICalString())
                else -> iCalContents.writeICalFile(output)
            }
        }

        println(" in $duration")
    }
}
