# Kotlin iCalendar

[![Build Status](https://github.com/alphadev-net/kotlin-icalendar/actions/workflows/unit-tests.yml/badge.svg)](https://github.com/alphadev-net/kotlin-icalendar/actions/workflows/unit-tests.yml)
[![GitHub License](https://img.shields.io/github/license/alphadev-net/kotlin-icalendar)](https://github.com/alphadev-net/kotlin-icalendar/blob/main/LICENSE)
[![GitHub Tag](https://img.shields.io/github/v/tag/alphadev-net/kotlin-icalendar)](https://github.com/alphadev-net/kotlin-icalendar/tags)

A standalone library that allows parsing and writing of iCalendar (`.ics`) files.

This library parses iCalendar files into its own strongly-typed data structures and can render those structures back into RFC-compliant iCalendar output.

## Usage

This library is intended to be embedded into other Kotlin projects. It does not provide a standalone CLI or tool for processing `.ics` files.

In addition to parsing existing iCalendar files, the library provides a Kotlin DSL for creating iCalendar data from scratch.

### Creating an event using the DSL

```kotlin
import net.alphadev.icalendar.model.dsl.vCalendar
import net.alphadev.icalendar.model.dsl.EventStatus
import net.alphadev.icalendar.model.dsl.Transparency
import kotlinx.datetime.LocalDateTime
import kotlin.time.Duration.Companion.minutes

val calendar = vCalendar {
    prodId("-//Example Corp//Calendar App//EN")
    calScale("GREGORIAN")

    event {
        summary("Team Sync Meeting")
        description("Weekly team sync to discuss progress and blockers")
        location("Conference Room A")
        status(EventStatus.CONFIRMED)
        transp(Transparency.OPAQUE)

        dtStart(LocalDateTime(2026, 2, 10, 10, 0))
        dtEnd(LocalDateTime(2026, 2, 10, 11, 0))

        organizer("alice@example.com", name = "Alice Example")

        alarm {
            displayAction()
            triggerBefore(15.minutes)
            description("Meeting starts in 15 minutes")
        }
    }
}
```

The resulting VCalendar instance can then be serialized into a valid iCalendar (.ics) file using the library’s writer.

# RFC Compliance
The library strives for compliance with the iCalendar specification (RFC 5545) and related extensions. The codebase is covered by a growing unit test suite (170+ tests at the time of writing).

Real-world calendar files are often imperfect or rely on undocumented or proprietary extensions. To support interoperability, the library deliberately retains unknown components and properties during parsing, allowing vendor-specific data (for example X-* properties or non-standard components) to round-trip without loss.

As such:

- Valid but uncommon constructs may not yet be fully understood, but are preserved
- Unknown or proprietary extensions are retained and re-emitted
- Some invalid files may parse successfully, others may not
- Output should be RFC-compliant, but compatibility bugs are still possible

# Bugs and Issues
If you encounter a (somewhat valid) iCalendar file that this library fails to process — or produces incorrect output for — please don’t hesitate to file an issue.

Including a minimal .ics example is greatly appreciated.
