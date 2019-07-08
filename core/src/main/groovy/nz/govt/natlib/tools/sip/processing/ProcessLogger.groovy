package nz.govt.natlib.tools.sip.processing

import groovy.util.logging.Log4j2
import org.apache.commons.io.output.TeeOutputStream

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.text.SimpleDateFormat

/**
 * Provides directed logging of console output.
 */
@Log4j2
class ProcessLogger {
    static final String TEMP_FILE_PREFIX = "ProcessLogger"
    static final String TEMP_FILE_SUFFIX = ".log"
    static final SimpleDateFormat TEMP_FILE_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS")

    Path splitFile
    OutputStream tempFileOutputStream
    PrintStream originalSystemOut = System.out
    PrintStream originalSystemErr = System.err
    TeeOutputStream outTeeOutputStream
    TeeOutputStream errTeeOutputStream

    void startSplit() {
        String prefix = "${TEMP_FILE_PREFIX}_${TEMP_FILE_DATE_FORMATTER.format(new Date())}"
        splitFile = File.createTempFile(prefix, TEMP_FILE_SUFFIX).toPath()
        splitFile.toFile().deleteOnExit()
        tempFileOutputStream = Files.newOutputStream(splitFile)
        outTeeOutputStream = new TeeOutputStream(originalSystemOut, tempFileOutputStream)
        errTeeOutputStream = new TeeOutputStream(originalSystemErr, tempFileOutputStream)
        PrintStream outTeePrintStream = new PrintStream(outTeeOutputStream)
        PrintStream errTeePrintStream = new PrintStream(errTeeOutputStream)
        System.setOut(outTeePrintStream)
        System.setErr(errTeePrintStream)
    }

    void copySplit(Path targetLocation, String targetFilenamePrefix = null, boolean resetStreams = true) {
        if (splitFile != null && Files.exists(splitFile)) {
            String targetName = targetFilenamePrefix == null ? splitFile.fileName.toString() :
                    "${targetFilenamePrefix}_${splitFile.fileName.toString()}"
            Path targetFile = targetLocation.resolve(targetName)
            Files.copy(splitFile, targetFile, StandardCopyOption.COPY_ATTRIBUTES)
        }
        if (resetStreams) {
            reset()
        }
    }

    void reset() {
        System.setOut(originalSystemOut)
        System.setErr(originalSystemErr)
        outTeeOutputStream.close()
        errTeeOutputStream.close()
        outTeeOutputStream = null
        errTeeOutputStream = null
    }
}
