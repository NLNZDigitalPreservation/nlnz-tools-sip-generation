package nz.govt.natlib.tools.sip.pdf.thumbnail

import groovy.util.logging.Log4j2

import java.util.concurrent.Semaphore
import java.util.concurrent.locks.ReentrantLock

/**
 * Thumbnail page generation can consume a lot of resources (CPU/memory). In a processing environment with a high
 * number of threads, the number of threads that can process thumbnails could be much much less (for example, 100
 * threads processing, but CPU/memory can only handle about 5 concurrent threads generating thumbnails. This class
 * ensures that only a dynamically configurable number of threads can write a thumbnail page.
 */
@Log4j2
class ThreadedThumbnailGenerator {
    static final int DEFAULT_MAXIMUM_NUMBER_OF_THREADS = 1
    static final long WAIT_TO_COMPLETE_MILLISECONDS = 300L
    private static Semaphore THREADING_SEMAPHORE = new Semaphore(DEFAULT_MAXIMUM_NUMBER_OF_THREADS)
    private static final ReentrantLock CHANGE_NUMBER_OF_THREADS_LOCK = new ReentrantLock()

    // Note that there is still a chance that a thread will slip through while the number of threads is changing.
    // However, if the number of threads is set before any operations start (which would be normal operation)
    // then things should be fine.
    static boolean changeMaximumConcurrentThreads(int maximumConcurrentThreads) {
        CHANGE_NUMBER_OF_THREADS_LOCK.lock()
        try {
            log.info("Changing maximumConcurrentThreads from=${THREADING_SEMAPHORE.availablePermits()} to=${maximumConcurrentThreads}")
            // There's going to be some overhead waiting for all the currently running threads to finish before
            // we can change the count.
            while (THREADING_SEMAPHORE.queueLength > 0) {
                log.info("Waiting=${WAIT_TO_COMPLETE_MILLISECONDS}ms to change maximumConcurrentThreads from=${THREADING_SEMAPHORE.availablePermits()} to=${maximumConcurrentThreads}")
                sleep(WAIT_TO_COMPLETE_MILLISECONDS)
            }
            THREADING_SEMAPHORE = new Semaphore(maximumConcurrentThreads)
        } finally {
            CHANGE_NUMBER_OF_THREADS_LOCK.unlock()
        }
    }

    static void writeThumbnailPage(List<File> pdfFiles, ThumbnailParameters parameters, File thumbnailPageFile) {
        try {
            // If we're in the process of changing the number of threads, then wait until that lock is released.
            if (CHANGE_NUMBER_OF_THREADS_LOCK.locked) {
                CHANGE_NUMBER_OF_THREADS_LOCK.lock()
                // We immediately release the lock, we just wanted to wait until the number of threads has changed
                CHANGE_NUMBER_OF_THREADS_LOCK.unlock()
            }
            THREADING_SEMAPHORE.acquire()
            ThumbnailGenerator.writeThumbnailPage(pdfFiles, parameters, thumbnailPageFile)
        } finally {
            THREADING_SEMAPHORE.release()
        }
    }

    static int numberThreadsGeneratingThumbnails() {
        return THREADING_SEMAPHORE.queueLength
    }
}
