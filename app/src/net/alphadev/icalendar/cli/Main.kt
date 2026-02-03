package net.alphadev.icalendar.cli

import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands

fun main(args: Array<String>) = iCalendar()
    .subcommands(Transform())
    .main(args)
