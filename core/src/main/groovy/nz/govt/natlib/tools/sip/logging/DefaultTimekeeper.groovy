package nz.govt.natlib.tools.sip.logging

import groovy.time.TimeCategory
import groovy.time.TimeDuration
import groovy.util.logging.Log4j2

import java.text.DateFormat
import java.text.DecimalFormat
import java.text.SimpleDateFormat

@Log4j2
class DefaultTimekeeper implements Timekeeper {
    Date startDateTime = null
    Date stopDateTime = null
    Date lastElapsed = null
    long lastCount = 0L
    List<TimeElapsedMarker> timeElapsedMarkers = [ ]

    DefaultTimekeeper() {
        startDateTime = new Date()
    }

    @Override
    void start() {
        startDateTime = new Date()
    }

    @Override
    void stop() {
        stopDateTime = new Date()
    }

    @Override
    void reset() {
        startDateTime = new Date()
        stopDateTime = null
        lastElapsed = null
        lastCount = 0L
        timeElapsedMarkers = [ ]
    }

    @Override
    void stopAndReset() {
        stop()
        reset()
    }

    @Override
    TimeDuration elapsedSinceLastElapsed() {
        // We don't increment after we stop
        Date currentDate = stopDateTime == null ? new Date() : stopDateTime
        TimeDuration elapsed = TimeCategory.minus(currentDate, lastElapsed == null ? startDateTime : lastElapsed)
        lastElapsed = currentDate
        return elapsed
    }

    @Override
    TimeDuration totalTime() {
        return TimeCategory.minus(stopDateTime == null ? new Date() : stopDateTime, startDateTime)
    }

    @Override
    void markElapsed(String marker, String markerDescription = null) {
        TimeElapsedMarker timeElapsedMarker = new TimeElapsedMarker(elapsedSinceLastElapsed(), totalTime(), marker,
                markerDescription)
        timeElapsedMarkers.add(timeElapsedMarker)
    }

    @Override
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

    @Override
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

    @Override
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
