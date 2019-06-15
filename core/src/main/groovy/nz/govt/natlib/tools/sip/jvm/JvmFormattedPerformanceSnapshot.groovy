package nz.govt.natlib.tools.sip.jvm

import groovy.transform.Canonical
import groovy.util.logging.Log4j2
import nz.govt.natlib.tools.sip.jvm.JvmPerformanceSnapshot.ThreadState
import nz.govt.natlib.tools.sip.utils.FormattingUtils

import java.text.SimpleDateFormat
import java.time.Duration

@Canonical
@Log4j2
class JvmFormattedPerformanceSnapshot {
    static final SimpleDateFormat TIMESTAMP_FORMATTER = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss.SSS')
    static final String NO_STATISTICS_AVAILABLE = "N/A"

    static class FormattedThreadState {
        String id
        String threadCpuTime = NO_STATISTICS_AVAILABLE
        String threadUserTime = NO_STATISTICS_AVAILABLE

        FormattedThreadState() {
            // Empty constructor for empty statistics
        }

        FormattedThreadState(String id, String threadCpuTime, String threadUserTime) {
            this.id = id
            this.threadCpuTime = threadCpuTime
            this.threadUserTime = threadUserTime
        }
    }

    static class FormattedMemoryState {
        String init = NO_STATISTICS_AVAILABLE
        String used = NO_STATISTICS_AVAILABLE
        String committed = NO_STATISTICS_AVAILABLE
        String max = NO_STATISTICS_AVAILABLE

        FormattedMemoryState() {
            // Empty constructor for empty statistics
        }

        FormattedMemoryState(JvmPerformanceSnapshot.MemoryState usage) {
            this.init = formatBytes(usage.init)
            this.used = formatBytes(usage.used)
            this.committed = formatBytes(usage.committed)
            this.max = formatBytes(usage.max)
        }
    }

    static class FormattedGarbageCollectionState {
        String name = NO_STATISTICS_AVAILABLE
        List<String> memoryPoolNames = [ NO_STATISTICS_AVAILABLE ]
        String collectionCount = NO_STATISTICS_AVAILABLE
        String collectionTime = NO_STATISTICS_AVAILABLE

        FormattedGarbageCollectionState() {
            // Empty constructor for empty statistics
        }

        FormattedGarbageCollectionState(JvmPerformanceSnapshot.GarbageCollectionState garbageState) {
            this.name = garbageState.name
            this.memoryPoolNames = garbageState.memoryPoolNames.toList()
            this.collectionCount = "${garbageState.collectionCount}"
            this.collectionTime = "${FormattingUtils.formatDuration(Duration.ofMillis(garbageState.collectionTime))}"
        }
    }

    private JvmPerformanceSnapshot snapshot

    String timestamp = NO_STATISTICS_AVAILABLE

    // Operating system
    String operatingSystemName = NO_STATISTICS_AVAILABLE
    String operatingSystemArchitecture = NO_STATISTICS_AVAILABLE
    String operatingSystemVersion = NO_STATISTICS_AVAILABLE
    String availableProcessors = NO_STATISTICS_AVAILABLE

    // System
    String systemLoadAverage

    // Process state
    String processCpuTime = NO_STATISTICS_AVAILABLE
    String processCpuLoad = NO_STATISTICS_AVAILABLE
    String totalPhysicalMemorySize = NO_STATISTICS_AVAILABLE
    String committedVirtualMemorySize = NO_STATISTICS_AVAILABLE
    String freePhysicalMemorySize = NO_STATISTICS_AVAILABLE
    String freeSwapSpaceSize = NO_STATISTICS_AVAILABLE
    String totalSwapSpaceSize = NO_STATISTICS_AVAILABLE

    // Thread state
    String threadCpuTimeEnabled = NO_STATISTICS_AVAILABLE
    String currentThreadCpuTimeSupported = NO_STATISTICS_AVAILABLE
    String threadCount = NO_STATISTICS_AVAILABLE
    String daemonThreadCount = NO_STATISTICS_AVAILABLE
    String totalStartedThreadCount = NO_STATISTICS_AVAILABLE
    String peakThreadCount = NO_STATISTICS_AVAILABLE
    FormattedThreadState currentThreadState = new FormattedThreadState()
    List<FormattedThreadState> allThreadStates = [ ]

    // Memory state
    FormattedMemoryState heap = new FormattedMemoryState()
    FormattedMemoryState nonHeap = new FormattedMemoryState()
    List<String> memoryPools = [ ]
    List<FormattedGarbageCollectionState> garbageCollectionStates = [ ]

    static String formatBytes(long numberBytes, boolean useSiUnits = true) {
        return FormattingUtils.formatByteCount(numberBytes, useSiUnits)
    }

    static from(JvmPerformanceSnapshot snapshot) {
        return new JvmFormattedPerformanceSnapshot(snapshot)
    }

    JvmFormattedPerformanceSnapshot(JvmPerformanceSnapshot snapshot) {
        this.snapshot = snapshot

        timestamp = TIMESTAMP_FORMATTER.format(snapshot.timestamp)

        formatOperatingSystem()
        formatProcess()
        formatCommonThreadStates()
        currentThreadState = formatThreadState(snapshot.currentThreadState)
        allThreadStates = snapshot.allThreadStates.collect { ThreadState threadState ->
            formatThreadState(threadState)
        }
        formatMemory()
        formatGarbageCollection()
    }

    private formatOperatingSystem() {
        operatingSystemName = snapshot.operatingSystemName
        operatingSystemArchitecture = snapshot.operatingSystemArchitecture
        operatingSystemVersion = snapshot.operatingSystemVersion
        availableProcessors = "${snapshot.availableProcessors}"
        systemLoadAverage = String.format("%01.4f", snapshot.systemLoadAverage)
    }

    private formatProcess() {
        processCpuTime = FormattingUtils.formatDuration(Duration.ofNanos(snapshot.processCpuTime))
        processCpuLoad = FormattingUtils.formatPercent(snapshot.processCpuLoad)
        totalPhysicalMemorySize = formatBytes(snapshot.totalPhysicalMemorySize)
        committedVirtualMemorySize = formatBytes(snapshot.committedVirtualMemorySize)
        freePhysicalMemorySize = formatBytes(snapshot.freePhysicalMemorySize)
        freeSwapSpaceSize = formatBytes(snapshot.freeSwapSpaceSize)
        totalSwapSpaceSize = formatBytes(snapshot.totalSwapSpaceSize)
    }

    private formatCommonThreadStates() {
        threadCpuTimeEnabled = "${snapshot.threadCpuTimeEnabled}"
        currentThreadCpuTimeSupported = "${snapshot.currentThreadCpuTimeSupported}"
        threadCount = "${snapshot.threadCount}"
        daemonThreadCount = "${snapshot.daemonThreadCount}"
        totalStartedThreadCount = "${snapshot.totalStartedThreadCount}"
        peakThreadCount = "${snapshot.peakThreadCount}"
    }

    private FormattedThreadState formatThreadState(ThreadState threadState) {
        return new FormattedThreadState( threadState.id,
                FormattingUtils.formatDuration(Duration.ofNanos(threadState.threadCpuTime)),
                FormattingUtils.formatDuration(Duration.ofNanos(threadState.threadUserTime)))
    }

    private formatMemory() {
        heap = new FormattedMemoryState(snapshot.heap)
        nonHeap = new FormattedMemoryState(snapshot.nonHeap)
        memoryPools = snapshot.memoryPools.collect { String stringPool ->
            stringPool
        }
    }

    private formatGarbageCollection() {
        garbageCollectionStates = snapshot.garbageCollectionStates.collect { JvmPerformanceSnapshot.GarbageCollectionState garbageState ->
            new FormattedGarbageCollectionState(garbageState)
        }
    }
}
