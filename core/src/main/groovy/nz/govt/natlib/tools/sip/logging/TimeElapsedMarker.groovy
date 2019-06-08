package nz.govt.natlib.tools.sip.logging

import groovy.time.TimeDuration
import groovy.transform.Canonical
import groovy.util.logging.Log4j2

@Canonical
@Log4j2
class TimeElapsedMarker {
    TimeDuration elapsedTime
    TimeDuration totalTime
    String marker
    String markerDescription

    TimeElapsedMarker(TimeDuration elapsedTime, TimeDuration totalTime, String marker, String markerDescription) {
        this.elapsedTime = elapsedTime
        this.totalTime = totalTime
        this.marker = marker
        this.markerDescription = markerDescription
    }
}
