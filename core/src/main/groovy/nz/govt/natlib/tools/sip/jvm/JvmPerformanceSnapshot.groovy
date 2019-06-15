package nz.govt.natlib.tools.sip.jvm

import groovy.transform.Canonical
import groovy.util.logging.Log4j2

import java.lang.management.GarbageCollectorMXBean
import java.lang.management.ManagementFactory
import java.lang.management.MemoryMXBean
import java.lang.management.MemoryPoolMXBean
import java.lang.management.MemoryUsage
import java.lang.management.OperatingSystemMXBean
import java.lang.management.ThreadMXBean

@Canonical
@Log4j2
class JvmPerformanceSnapshot {
    static class ThreadState {
        String id
        long threadCpuTime
        long threadUserTime

        ThreadState(String id, long threadCpuTime, long threadUserTime) {
            this.id = id
            this.threadCpuTime = threadCpuTime
            this.threadUserTime = threadUserTime
        }
    }

    static class MemoryState {
        long init
        long used
        long committed
        long max

        MemoryState(MemoryUsage usage) {
            this.init = usage.init
            this.used = usage.used
            this.committed = usage.committed
            this.max = usage.max
        }
    }

    static class GarbageCollectionState {
        String name
        List<String> memoryPoolNames
        long collectionCount
        long collectionTime

        GarbageCollectionState(GarbageCollectorMXBean garbageBean) {
            this.name = garbageBean.name
            this.memoryPoolNames = garbageBean.memoryPoolNames.toList()
            this.collectionCount = garbageBean.collectionCount
            this.collectionTime = garbageBean.collectionTime
        }
    }

    Date timestamp

    // Operating system
    String operatingSystemName
    String operatingSystemArchitecture
    String operatingSystemVersion
    int availableProcessors

    // System
    double systemLoadAverage

    // Process state
    long processCpuTime
    double processCpuLoad
    long totalPhysicalMemorySize
    long committedVirtualMemorySize
    long freePhysicalMemorySize
    long freeSwapSpaceSize
    long totalSwapSpaceSize

    // Thread state
    boolean threadCpuTimeEnabled
    boolean currentThreadCpuTimeSupported
    int threadCount
    int daemonThreadCount
    long totalStartedThreadCount
    int peakThreadCount
    ThreadState currentThreadState
    List<ThreadState> allThreadStates = [ ]

    // Memory state
    MemoryState heap
    MemoryState nonHeap
    List<String> memoryPools = [ ]
    List<GarbageCollectionState> garbageCollectionStates = [ ]

    boolean operatingSystemCollected
    boolean processCollected
    boolean currentThreadCollected
    boolean allThreadsCollected
    boolean memoryCollected
    boolean garbageCollectionCollected

    static now(boolean operatingSystem = true, process = true, currentThread = true, allThreads = false, memory = true,
               garbageCollection = true) {
        return new JvmPerformanceSnapshot(operatingSystem, process, currentThread, allThreads, memory, garbageCollection)
    }

    JvmPerformanceSnapshot(boolean operatingSystem = true, process = true, currentThread = true, allThreads = false,
                           memory = true, garbageCollection = true) {
        timestamp = new Date()
        operatingSystemCollected = operatingSystem
        processCollected = process
        currentThreadCollected = currentThread
        allThreadsCollected = allThreads
        memoryCollected = memory
        garbageCollectionCollected = garbageCollection

        collectOperatingSystem()
        collectProcess()
        collectCurrentThread()
        collectAllThreads()
        collectMemory()
        collectGarbageCollection()
    }

    private collectOperatingSystem() {
        if (operatingSystemCollected) {
            OperatingSystemMXBean mxBean = ManagementFactory.operatingSystemMXBean
            operatingSystemName = mxBean.name
            operatingSystemArchitecture = mxBean.arch
            operatingSystemVersion = mxBean.version
            availableProcessors = mxBean.availableProcessors
            systemLoadAverage = mxBean.systemLoadAverage
        }
    }

    private collectProcess() {
        if (processCollected) {
            Class beanClass = com.sun.management.OperatingSystemMXBean.class
            com.sun.management.OperatingSystemMXBean sunMxBean = ManagementFactory.getPlatformMXBean(beanClass)
            if (sunMxBean == null) {
                log.warn("Unable to retrieve management bean class=${beanClass.getName()}")
            } else {
                processCpuTime = sunMxBean.processCpuTime
                processCpuLoad = sunMxBean.processCpuLoad
                totalPhysicalMemorySize = sunMxBean.totalPhysicalMemorySize
                committedVirtualMemorySize = sunMxBean.committedVirtualMemorySize
                freePhysicalMemorySize = sunMxBean.freePhysicalMemorySize
                freeSwapSpaceSize = sunMxBean.freeSwapSpaceSize
                totalSwapSpaceSize = sunMxBean.totalSwapSpaceSize
            }
        }
    }

    private collectCurrentThread() {
        if (currentThreadCollected) {
            ThreadMXBean threadMXBean = ManagementFactory.threadMXBean
            if (threadMXBean == null) {
                log.warn("Unable to retrieve bean class=${ThreadMXBean.class.getName()}")
            } else {
                threadCpuTimeEnabled = threadMXBean.threadCpuTimeEnabled
                currentThreadCpuTimeSupported = threadMXBean.currentThreadCpuTimeSupported
                if (threadMXBean.currentThreadCpuTimeSupported && threadMXBean.threadCpuTimeEnabled) {
                    currentThreadState = new ThreadState("current", threadMXBean.currentThreadCpuTime, threadMXBean.currentThreadUserTime)
                } else {
                    log.warn("Thread No CPU times available: threadCpuTimeEnabled=${threadMXBean.threadCpuTimeEnabled}, " +
                            "currentThreadCpuTimeSupported=${threadMXBean.currentThreadCpuTimeSupported}")
                }
                threadCount = threadMXBean.threadCount
                daemonThreadCount = threadMXBean.daemonThreadCount
                totalStartedThreadCount = threadMXBean.totalStartedThreadCount
                peakThreadCount = threadMXBean.peakThreadCount
            }
        }
    }

    private collectAllThreads() {
        if (allThreadsCollected) {
            log.warn("Currently snapshot collection of all threads is not supported.")
        }
    }

    private collectMemory() {
        if (memoryCollected) {
            MemoryMXBean memoryMXBean = ManagementFactory.memoryMXBean
            heap = new MemoryState(memoryMXBean.heapMemoryUsage)
            nonHeap = new MemoryState(memoryMXBean.nonHeapMemoryUsage)

            List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.memoryPoolMXBeans
            memoryPoolMXBeans.each { MemoryPoolMXBean memoryBean ->
                memoryPools.add(memoryBean.usage.toString())
            }
        }
    }

    private collectGarbageCollection() {
        if (garbageCollectionCollected) {
            List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.garbageCollectorMXBeans
            garbageCollectorMXBeans.each { GarbageCollectorMXBean garbageBean ->
                garbageCollectionStates.add(new GarbageCollectionState(garbageBean))
            }
        }
    }
}
