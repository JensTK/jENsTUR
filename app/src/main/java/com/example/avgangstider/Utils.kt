package com.example.avgangstider

import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun getDurationString(duration: Duration): String {
    var retString = ""
    if (duration.toHours() >= 1) {
        retString += "${duration.toHoursPart()}t "
    }
    if (duration.toMinutes() >= 1) {
        retString += "${duration.toMinutesPart()}m "
    }
    return retString + "${duration.toSecondsPart()}s"
}

fun getInstantString(instant: Instant): String {
    val dateTime = LocalDateTime.ofInstant(instant, ZonedDateTime.now().zone)
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    return dateTime.format(formatter)
}