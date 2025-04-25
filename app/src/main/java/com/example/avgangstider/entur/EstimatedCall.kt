package com.example.avgangstider.entur

import com.example.avgangstider.getDurationString
import com.example.avgangstider.getInstantString
import java.time.Duration
import java.time.Instant

data class EstimatedCall(
    val expectedArrivalTime: Instant,
    val expectedDepartureTime: Instant,
    val realtime: Boolean,
    val predictionInaccurate: Boolean,
    val cancellation: Boolean,
    val destinationDisplay: DestinationDisplay
) {
    fun getDepartureString(timeFrom: Instant): String {
        return getInstantString(expectedDepartureTime) + " - " + getDurationString(Duration.between(timeFrom, expectedDepartureTime)) + " (${destinationDisplay.frontText})"
    }
}
