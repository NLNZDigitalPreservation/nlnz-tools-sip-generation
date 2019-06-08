package nz.govt.natlib.tools.sip.logging

import groovy.time.TimeCategory
import groovy.time.TimeDuration
import groovy.util.logging.Log4j2

import java.text.DateFormat
import java.text.DecimalFormat
import java.text.SimpleDateFormat

@Log4j2
class Timekeeper {
    static final DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z")
    static final DecimalFormat PROCESSING_RATE_FORMAT = new DecimalFormat("#,###,###,###.000")
    static final DecimalFormat PROCESSED = new DecimalFormat("###,###,###,###")
    int MILLISECONDS_PER_SECOND = 1000
    int SECONDS_PER_MINUTE = 60
    int MINUTES_PER_HOUR = 60
    int MILLISECONDS_PER_MINUTE = MILLISECONDS_PER_SECOND * SECONDS_PER_MINUTE
    int MILLISECONDS_PER_HOUR = MILLISECONDS_PER_MINUTE * MINUTES_PER_HOUR

    Date startDateTime = null
    Date stopDateTime = null
    Date lastElapsed = null
    long lastCount = 0L
    List<TimeElapsedMarker> timeElapsedMarkers = [ ]

    Timekeeper() {
        startDateTime = new Date()
    }

    void start() {
        startDateTime = new Date()
    }

    void stop() {
        stopDateTime = new Date()
    }

    TimeDuration elapsedSinceLastElapsed() {
        // We don't increment after we stop
        Date currentDate = stopDateTime == null ? new Date() : stopDateTime
        TimeDuration elapsed = TimeCategory.minus(currentDate, lastElapsed == null ? startDateTime : lastElapsed)
        lastElapsed = currentDate
        return elapsed
    }

    TimeDuration totalTime() {
        return TimeCategory.minus(stopDateTime == null ? new Date() : stopDateTime, startDateTime)
    }

    void markElapsed(String marker, String markerDescription = null) {
        TimeElapsedMarker timeElapsedMarker = new TimeElapsedMarker(elapsedSinceLastElapsed(), totalTime(), marker,
                markerDescription)
        timeElapsedMarkers.add(timeElapsedMarker)
    }

    void listMarkers(boolean useDebug = false) {
        timeElapsedMarkers.each { TimeElapsedMarker timeElapsedMarker ->
            String message = "${timeElapsedMarker.marker}: elapsedTime=${timeElapsedMarker.elapsedTime}, " +
                    "totalTime=${timeElapsedMarker.totalTime}"
            if (timeElapsedMarker.markerDescription != null && !timeElapsedMarker.markerDescription.isEmpty()) {
                message += " - ${timeElapsedMarker.markerDescription}"
            }
            if (useDebug) {
                log.debug(message)
            } else {
                log.info(message)
            }
        }
    }

    void logElapsed(boolean useDebug = false, long currentCount = 0L, boolean showRate = false) {
        TimeDuration elapsedTime = elapsedSinceLastElapsed()
        TimeDuration totalTime = totalTime()
        String message = "elapsedTime=${elapsedTime}, totalTime=${totalTime}, " +
                "startTime=${TIMESTAMP_FORMAT.format(startDateTime)} [currentTime=${TIMESTAMP_FORMAT.format(new Date())}]"
        if (showRate) {
            String totalProcessingRate = processingRate(currentCount, totalTime)
            String incrementalProcessingRate = processingRate(currentCount - lastCount, elapsedTime)
            String processingRateMessage = ", processed=${PROCESSED.format(currentCount)}, " +
                    "incremental rate=${incrementalProcessingRate}, total rate=${totalProcessingRate}"
            message = "${message}${processingRateMessage}"
        }
        if (useDebug) {
            log.debug(message)
        } else {
            log.info(message)
        }
        lastCount = currentCount
    }

    String processingRate(long processedCount, TimeDuration elapsedTime) {
        long totalMilliseconds = elapsedTime.toMilliseconds()
        // We probably want a 2 to 3 digit rate
        if (elapsedTime.hours > 0) {
            double ratePerHour = (processedCount * MILLISECONDS_PER_HOUR) / totalMilliseconds
            if (ratePerHour < 59) {
                String rate = PROCESSING_RATE_FORMAT.format(ratePerHour)
                return "${rate}/hour"
            }
        }
        if (elapsedTime.minutes > 0) {
            double ratePerMinute = (processedCount * MILLISECONDS_PER_MINUTE) / totalMilliseconds
            if (ratePerMinute < 59) {
                String rate = PROCESSING_RATE_FORMAT.format(ratePerMinute)
                return "${rate}/minute"
            }
        }
        if (elapsedTime.millis > 0) {
            double ratePerSecond = (processedCount * MILLISECONDS_PER_SECOND) / totalMilliseconds
            String rate = PROCESSING_RATE_FORMAT.format(ratePerSecond)
            return "${rate}/second"
        }
        return "<rate too low to measure>"
    }

}
