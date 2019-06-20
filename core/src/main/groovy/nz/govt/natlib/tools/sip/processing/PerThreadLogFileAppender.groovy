package nz.govt.natlib.tools.sip.processing

import groovy.util.logging.Log4j2
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.ThreadContext
import org.apache.logging.log4j.core.Appender
import org.apache.logging.log4j.core.Filter
import org.apache.logging.log4j.core.Layout
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.appender.FileAppender
import org.apache.logging.log4j.core.config.AbstractConfiguration
import org.apache.logging.log4j.core.config.Configuration
import org.apache.logging.log4j.core.filter.ThreadContextMapFilter
import org.apache.logging.log4j.core.layout.PatternLayout
import org.apache.logging.log4j.core.util.KeyValuePair

import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.concurrent.locks.ReentrantLock

/**
 * Logs messages to a specific file for a specific thread.
 */
@Log4j2
class PerThreadLogFileAppender {
    private static final long DEFAULT_START_WAIT_MILLISECONDS = 50L
    private static final long DEFAULT_STOP_WAIT_MILLISECONDS = 50L
    private static final ReentrantLock OPERATIONAL_LOCK = new ReentrantLock()

    // c{precision} or logger{precision} - logger{1} is just class name
    // d{pattern} or date{pattern} - date with %date{DEFAULT_MICROS} as 2012-11-02 14:34:02,123456
    // ex|exception|throwable - exception with stack trace
    // p|level - logging level
    // L|line - line number (does increase overhead)
    // m|msg|message - message
    // M|method - method
    // n - the platform-dependent line separator
    // t|tn|thread|threadName - name of the thread
    // %-5level means the logging level is left justified to a width of 5 characters
    static final String DEFAULT_PATTERN = "%date{DEFAULT_MICROS} [%threadName] %-5level %logger{1}: %message%n%throwable"
    static final String DEFAULT_PATTERN_WITH_LINE_NUMBERS = "%date{DEFAULT_MICROS} [%threadName] %-5level %logger{1}.%method:%line - %message%n%throwable"

    static final String LOG_APPENDER_NAME_KEY = "PerThreadLogFileAppender_Name"
    static final String FILE_SUFFIX = ".log"
    static final SimpleDateFormat LOG_FILE_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS")

    /**
     * This must be called on the thread to start logging.
     * The uniqueID means that the appender name is unique and not re-used.
     */
    static File startWithGeneratedFilename(File sourceFolder, String filenamePrefix, UUID uniqueId = null) {
        OPERATIONAL_LOCK.lock()
        File logFile
        try {
            String postFix = "${LOG_FILE_DATE_FORMATTER.format(new Date())}${FILE_SUFFIX}"
            String filename = "${filenamePrefix}_${postFix}"

            logFile = start(sourceFolder, filename, uniqueId)
        } finally {
            OPERATIONAL_LOCK.unlock()
        }
        return logFile
    }

    /**
     * This must be called on the thread to start logging.
     * The uniqueID means that the appender name is unique and not re-used.
     */
    static File start(File sourceFolder, String filename, UUID uniqueID = null) {
        File logFile = new File(sourceFolder, filename)
        OPERATIONAL_LOCK.lock()

        try {
            String logAppenderName = logAppenderName(uniqueID)
            ThreadContext.put(LOG_APPENDER_NAME_KEY, logAppenderName)

            setupAppenderForFile(logFile, logAppenderName)
        } finally {
            OPERATIONAL_LOCK.unlock()
        }
        return logFile
    }

    /**
     * This must be called on the thread to stop logging.
     * The uniqueID means that the appender name is unique and not re-used.
     * If you see errors logged to the effect of:
     *     'Attempted to append to non-started appender'
     * it could mean that the ignoreExceptions on the appender has somehow been set to false
     * (otherwise errors logging to a stopped appender will be ignored).
     */
    static void stopAndRemove(UUID uniqueID = null) {
        OPERATIONAL_LOCK.lock()
        try {
            final LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false)
            final AbstractConfiguration configuration = (AbstractConfiguration) loggerContext.getConfiguration()

            String appenderName = logAppenderName(uniqueID)
            Appender retrievedAppender = configuration.getAppender(appenderName)
            if (retrievedAppender == null) {
                log.warn("Unable to retrieve expected appender=${appenderName}")
            } else {
                log.info("Removing logger appender=${appenderName}")
                configuration.removeAppender(appenderName)
                loggerContext.getRootLogger().removeAppender(retrievedAppender)
                loggerContext.updateLoggers()
                retrievedAppender.stop()
                while (!retrievedAppender.isStopped()) {
                    sleep(DEFAULT_STOP_WAIT_MILLISECONDS)
                }
                retrievedAppender = configuration.getAppender(appenderName)
                if (retrievedAppender != null) {
                    log.warn("Log appender=${appenderName} not null when re-retrieved after removal, retrieved=${retrievedAppender}")
                }
                log.info("Removed logger appender=${appenderName}")
            }
            ThreadContext.remove(LOG_APPENDER_NAME_KEY)
        } finally {
            OPERATIONAL_LOCK.unlock()
        }
    }

    static void setupAppenderForFile(File logFile, String logAppenderName) {
        OPERATIONAL_LOCK.lock()
        try {
            final LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false)
            final Configuration configuration = loggerContext.getConfiguration()

            Appender retrievedAppender = configuration.getAppender(logAppenderName)
            if (retrievedAppender != null) {
                log.warn("Attempting to setup appender with name=${logAppenderName}, file=${logFile}, but it already exists=${retrievedAppender}")
                log.warn("Stopping and removing existing appender and then creating a new one.")
                stopAndRemove()
            }

            Layout layout = PatternLayout.newBuilder().
                    withPattern(DEFAULT_PATTERN).
                    withConfiguration(configuration).
                    withCharset(StandardCharsets.UTF_8).
                    build()

            KeyValuePair threadNamePair = KeyValuePair.newBuilder()
                    .setKey(LOG_APPENDER_NAME_KEY)
                    .setValue(logAppenderName)
                    .build()
            KeyValuePair[] filterKeyValuePairs = [threadNamePair]
            Filter threadContextMapFilter = ThreadContextMapFilter.createFilter(filterKeyValuePairs, "or",
                    Filter.Result.ACCEPT, Filter.Result.DENY)

            Appender appender = FileAppender.newBuilder()
                    .setName(logAppenderName)
                    .withFileName(logFile.getCanonicalPath())
                    .withAppend(true)
                    .withLocking(false)
                    .withImmediateFlush(true)
                    .withBufferedIo(false)
                    //.withBufferSize(4000)
                    .setLayout(layout)
                    .setFilter(threadContextMapFilter)
                    .setConfiguration(configuration)
                    // After the appender is stopped we don't want it throwing exceptions if there are attempts to log
                    // through it.
                    .setIgnoreExceptions(true)
                    .withAdvertiseUri(null)
                    .build()

            log.info("Adding logger appender=${logAppenderName}")
            appender.start()
            while (!appender.isStarted()) {
                sleep(DEFAULT_START_WAIT_MILLISECONDS)
            }
            configuration.addAppender(appender)
            loggerContext.getRootLogger().addAppender(appender)
            loggerContext.updateLoggers()
            log.info("Added logger appender=${logAppenderName}")
        } finally {
            OPERATIONAL_LOCK.unlock()
        }
    }

    static String logAppenderName(UUID uuid) {
        String threadName = Thread.currentThread().getName()

        return uuid == null ? "FILE-${threadName}" : "FILE-${threadName}-${uuid}"
    }
}
