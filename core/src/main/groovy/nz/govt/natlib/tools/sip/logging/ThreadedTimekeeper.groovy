package nz.govt.natlib.tools.sip.logging

import groovy.time.TimeDuration

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock

class ThreadedTimekeeper implements Timekeeper {
    // Since we're always accessing this map via a ReentrantLock, it's probably no necessary to have a ConcurrentHashMap
    static final Map<String, Timekeeper> TIMEKEEPER_BY_THREAD = new ConcurrentHashMap<>()
    static ReentrantLock operationLock = new ReentrantLock()

    static Timekeeper forCurrentThread(boolean createIfNull = true) {
        operationLock.lock()
        String threadName = Thread.currentThread().getName()
        Timekeeper currentThreadTimekeeper = TIMEKEEPER_BY_THREAD.get(threadName)
        if (currentThreadTimekeeper == null && createIfNull) {
            currentThreadTimekeeper = new ThreadedTimekeeper()
            TIMEKEEPER_BY_THREAD.put(threadName, currentThreadTimekeeper)
        }
        operationLock.unlock()
        return currentThreadTimekeeper
    }

    Timekeeper realTimekeeper

    private ThreadedTimekeeper() {
        realTimekeeper = new DefaultTimekeeper()
    }

    @Override
    void start() {
        realTimekeeper.start()
    }

    @Override
    void stop() {
        realTimekeeper.stop()
    }

    @Override
    void reset() {
        operationLock.lock()
        if (realTimekeeper != null) {
            realTimekeeper.reset()
            String threadName = Thread.currentThread().getName()
            TIMEKEEPER_BY_THREAD.put(threadName, null)
        }
        operationLock.unlock()
    }

    @Override
    void stopAndReset() {
        stop()
        reset()
    }

    @Override
    TimeDuration elapsedSinceLastElapsed() {
        return realTimekeeper.elapsedSinceLastElapsed()
    }

    @Override
    TimeDuration totalTime() {
        return realTimekeeper.totalTime()
    }

    @Override
    void markElapsed(String marker, String markerDescription = null) {
        realTimekeeper.markElapsed(marker, markerDescription)
    }

    @Override
    void listMarkers(boolean useDebug = false) {
        realTimekeeper.listMarkers(useDebug)
    }

    @Override
    void logElapsed(boolean useDebug = false, long currentCount = 0L, boolean showRate = false) {
        realTimekeeper.logElapsed(useDebug, currentCount, showRate)
    }

    @Override
    String processingRate(long processedCount, TimeDuration elapsedTime) {
        return realTimekeeper.processingRate(processedCount, elapsedTime)
    }
}
