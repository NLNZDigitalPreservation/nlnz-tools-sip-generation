package nz.govt.natlib.tools.sip.logging

import groovy.time.TimeDuration

import java.text.DateFormat
import java.text.DecimalFormat
import java.text.SimpleDateFormat

interface Timekeeper {
    static final DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z")
    static final DecimalFormat PROCESSING_RATE_FORMAT = new DecimalFormat("#,###,###,###.000")
    static final DecimalFormat PROCESSED = new DecimalFormat("###,###,###,###")
    static final int MILLISECONDS_PER_SECOND = 1000
    static final int SECONDS_PER_MINUTE = 60
    static final int MINUTES_PER_HOUR = 60
    static final int MILLISECONDS_PER_MINUTE = MILLISECONDS_PER_SECOND * SECONDS_PER_MINUTE
    static final int MILLISECONDS_PER_HOUR = MILLISECONDS_PER_MINUTE * MINUTES_PER_HOUR

    void start()

    void stop()

    void reset()

    void stopAndReset()

    TimeDuration elapsedSinceLastElapsed()

    TimeDuration totalTime()

    void markElapsed(String marker, String markerDescription)

    void listMarkers()

    void listMarkers(boolean useDebug)

    void logElapsed()

    void logElapsed(boolean useDebug)

    void logElapsed(boolean useDebug, long currentCount)

    void logElapsed(boolean useDebug, long currentCount, boolean showRate)

    String processingRate(long processedCount, TimeDuration elapsedTime)

}
