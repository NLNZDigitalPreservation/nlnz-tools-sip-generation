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

import java.text.SimpleDateFormat

/**
 * Logs messages to a specific file for a specific thread.
 *
 * Note that there is an implicit assumption that the thread name does not
 * change while a given appender is active.
 */
@Log4j2
class PerThreadLogFileAppender {

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

    static final String THREAD_CONTEXT_KEY = "threadName"
    static final String FILE_SUFFIX = ".log"
    static final SimpleDateFormat LOG_FILE_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS")

    static File startWithGeneratedFilename(File sourceFolder, String filenamePrefix) {
        String postFix = "${LOG_FILE_DATE_FORMATTER.format(new Date())}${FILE_SUFFIX}"
        String filename = "${filenamePrefix}_${postFix}"

        return start(sourceFolder, filename)
    }

    /**
     * This must be called on the thread to start logging.
     */
    static synchronized File start(File sourceFolder, String filename) {
        File logFile = new File(sourceFolder, filename)

        String threadName = Thread.currentThread().getName()
        ThreadContext.put(THREAD_CONTEXT_KEY, threadName)

        setupAppenderForFile(logFile)

        return logFile
    }

    static String currentThreadFileAppenderName() {
        return "FILE-${Thread.currentThread().getName()}".toString()
    }

    /**
     * This must be called on the thread to stop logging.
     */
    static synchronized void stopAndRemove() {
        final LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false)
        final AbstractConfiguration configuration = (AbstractConfiguration) loggerContext.getConfiguration()

        String appenderName = currentThreadFileAppenderName()
        Appender retrievedAppender = configuration.getAppender(appenderName)
        if (retrievedAppender == null) {
            log.warn("Unable to retrieve expected appender=${appenderName}")
        } else {
            configuration.removeAppender(appenderName)
            retrievedAppender.stop()
        }
        ThreadContext.remove(THREAD_CONTEXT_KEY)

        loggerContext.updateLoggers()
    }

    static synchronized void setupAppenderForFile(File logFile) {
        final LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false)
        final Configuration configuration = loggerContext.getConfiguration()

        Layout layout = PatternLayout.newBuilder().
                withPattern(DEFAULT_PATTERN).
                withConfiguration(configuration).
                build()

        KeyValuePair threadNamePair = KeyValuePair.newBuilder()
                .setKey(THREAD_CONTEXT_KEY)
                .setValue(Thread.currentThread().getName())
                .build()
        KeyValuePair[] filterKeyValuePairs = [ threadNamePair ]
        Filter threadContextMapFilter = ThreadContextMapFilter.createFilter(filterKeyValuePairs, "or",
                Filter.Result.ACCEPT, Filter.Result.DENY)

        Appender appender = FileAppender.newBuilder()
                .setName(currentThreadFileAppenderName())
                .withFileName(logFile.getCanonicalPath())
                .withAppend(true)
                .withLocking(false)
                .withImmediateFlush(true)
                .withBufferedIo(false)
                //.withBufferSize(4000)
                .setLayout(layout)
                .setFilter(threadContextMapFilter)
                .setConfiguration(configuration)
                .setIgnoreExceptions(false)
                .withAdvertiseUri(null)
                .build()

        configuration.addAppender(appender)
        loggerContext.getRootLogger().addAppender(appender)
        loggerContext.updateLoggers()
        appender.start()
    }


}
