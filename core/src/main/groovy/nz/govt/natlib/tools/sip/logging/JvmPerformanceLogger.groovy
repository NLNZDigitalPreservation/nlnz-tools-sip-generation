package nz.govt.natlib.tools.sip.logging

import nz.govt.natlib.tools.sip.jvm.JvmFormattedPerformanceSnapshot
import nz.govt.natlib.tools.sip.jvm.JvmPerformanceSnapshot

import groovy.util.logging.Log4j2

@Log4j2
class JvmPerformanceLogger {
    JvmPerformanceSnapshot snapshot
    JvmFormattedPerformanceSnapshot formatted
    String message
    
    static JvmPerformanceLogger logState(String message, boolean operatingSystem = true, process = true, currentThread = true,
                    allThreads = false, memory = true, memoryPools = false, garbageCollection = true) {
        JvmPerformanceSnapshot snapshot = JvmPerformanceSnapshot.now(operatingSystem, process, currentThread, allThreads,
                memory, garbageCollection)
        JvmPerformanceLogger logger = new JvmPerformanceLogger(snapshot, message)
        if (operatingSystem) {
            logger.logOperatingSystem()
        }
        if (process) {
            logger.logProcessState()
        }
        if (currentThread) {
            logger.logCurrentThreadState()
        }
        if (allThreads) {
            logger.logAllThreads()
        }
        if (memory) {
            logger.logMemoryState()
        }
        if (memoryPools) {
            logger.logMemoryPools()
        }
        if (garbageCollection) {
            logger.logGarbageCollection()
        }
        return logger
    }

    JvmPerformanceLogger(JvmPerformanceSnapshot snapshot, String message) {
        this.snapshot = snapshot
        this.formatted = JvmFormattedPerformanceSnapshot.from(snapshot)
        this.message = formatMessage(message)
    }
    
    void logOperatingSystem() {
        log.info("${message}general operatingSystemName=${formatted.operatingSystemName}, arch=${formatted.operatingSystemArchitecture}, " +
                "version=${formatted.operatingSystemVersion}, availableProcessors=${formatted.availableProcessors}")
        log.info("${message}general systemLoadAverage=${formatted.systemLoadAverage}")
    }

    void logProcessState() {
        log.info("${message}CPU processCpuTime=${formatted.processCpuTime}")
        log.info("${message}CPU processCpuLoad=${formatted.processCpuLoad}")
        log.info("${message}memory totalPhysicalMemorySize=${formatted.totalPhysicalMemorySize}")
        log.info("${message}memory committedVirtualMemorySize=${formatted.committedVirtualMemorySize}")
        log.info("${message}memory freePhysicalMemorySize=${formatted.freePhysicalMemorySize}")
        log.info("${message}swap freeSwapSpaceSize=${formatted.freeSwapSpaceSize}")
        log.info("${message}swap totalSwapSpaceSize=${formatted.totalSwapSpaceSize}")
    }

    void logCurrentThreadState() {
        log.info("${message}thread threadCount=${formatted.threadCount}")
        log.info("${message}thread daemonThreadCount=${formatted.daemonThreadCount}")
        log.info("${message}thread totalStartedThreadCount=${formatted.totalStartedThreadCount}")
        log.info("${message}thread peakThreadCount=${formatted.peakThreadCount}")
        if (snapshot.currentThreadCpuTimeSupported && snapshot.threadCpuTimeEnabled) {
            log.info("${message}thread currentThreadCpuTime=${formatted.currentThreadState.threadCpuTime}")
            log.info("${message}thread currentThreadUserTime=${formatted.currentThreadState.threadUserTime}")
        } else {
            log.info("${message}thread currentThreadCpuTimeSupported=${formatted.currentThreadCpuTimeSupported}")
            log.info("${message}thread threadCpuTimeEnabled=${formatted.threadCpuTimeEnabled}")
        }
    }

    void logAllThreads() {
        formatted.allThreadStates.each { JvmFormattedPerformanceSnapshot.FormattedThreadState threadState ->
            log.info("${message}thread id=${threadState.id}")
            log.info("${message}thread threadCpuTime=${threadState.threadCpuTime}")
            log.info("${message}thread threadUserTime=${threadState.threadUserTime}")
        }
    }


    void logMemoryState() {
        log.info("${message}memory heap init=${formatted.heap.init}, used=${formatted.heap.used}, committed=${formatted.heap.committed}, max=${formatted.heap.max}")
        log.info("${message}memory non-heap init=${formatted.nonHeap.init}, used=${formatted.nonHeap.used}, committed=${formatted.nonHeap.committed}, max=${formatted.nonHeap.max}")

        formatted.memoryPools.each { String pool ->
            log.info("${message}memory pool usage=${pool}")
        }
    }

    void logMemoryPools() {
        formatted.memoryPools.each { String pool ->
            log.info("${message}memory pool usage=${pool}")
        }
    }

    void logGarbageCollection() {
        formatted.garbageCollectionStates.each { JvmFormattedPerformanceSnapshot.FormattedGarbageCollectionState garbageState ->
            log.info("${message} garbage name=${garbageState.name}, collectionCount=${garbageState.collectionCount}, collectionTime=${garbageState.collectionTime}, memoryPoolNames=${garbageState.memoryPoolNames}")
        }
    }

    static String formatMessage(String message = "") {
        String messageString = message == null ? "" : message.strip() + ": "

        return messageString
    }
}
