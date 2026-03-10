package net.alphadev.icalendar.import

import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.readLine

/*internal*/ @Suppress("NO_EXPLICIT_VISIBILITY_IN_API_MODE_WARNING")
object LineUnfolder {
    fun unfold(source: Source) = buildList {
        val buffered = source.buffered()
        val lineBuffer = StringBuilder()

        fun StringBuilder.flushLine() {
            if (isNotEmpty()) {
                add(toString())
                clear()
            }
        }

        while (!buffered.exhausted()) {
            val line = buffered.readLine() ?: break
            when {
                (line.startsWith(' ') || line.startsWith('\t')) && lineBuffer.isNotEmpty() ->
                    lineBuffer.append(line.drop(1))
                else -> {
                    lineBuffer.flushLine()
                    lineBuffer.append(line)
                }
            }
        }
        lineBuffer.flushLine()
    }
}
