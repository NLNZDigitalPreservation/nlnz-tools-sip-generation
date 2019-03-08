package nz.govt.natlib.tools.sip.processing

import groovy.util.logging.Slf4j
import org.apache.commons.io.output.TeeOutputStream

import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.text.SimpleDateFormat

/**
 * Provides directed logging of console output.
 */
@Slf4j
class ProcessLogger {
    static final String TEMP_FILE_PREFIX = "ProcessLogger"
    static final String TEMP_FILE_SUFFIX = ".log"
    static final SimpleDateFormat TEMP_FILE_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS")

    File splitFile
    FileOutputStream tempFileOutputStream
    PrintStream originalSystemOut = System.out
    PrintStream originalSystemErr = System.err
    TeeOutputStream outTeeOutputStream
    TeeOutputStream errTeeOutputStream

    void startSplit() {
        String prefix = "${TEMP_FILE_PREFIX}_${TEMP_FILE_DATE_FORMATTER.format(new Date())}"
        splitFile = File.createTempFile(prefix, TEMP_FILE_SUFFIX)
        splitFile.deleteOnExit()
        tempFileOutputStream = new FileOutputStream(splitFile)
        outTeeOutputStream = new TeeOutputStream(originalSystemOut, tempFileOutputStream)
        errTeeOutputStream = new TeeOutputStream(originalSystemErr, tempFileOutputStream)
        PrintStream outTeePrintStream = new PrintStream(outTeeOutputStream)
        PrintStream errTeePrintStream = new PrintStream(errTeeOutputStream)
        System.setOut(outTeePrintStream)
        System.setErr(errTeePrintStream)
    }

    void copySplit(File targetLocation, String targetFilenamePrefix = null, boolean resetStreams = true) {
        if (splitFile != null && splitFile.exists()) {
            String targetName = targetFilenamePrefix == null ? splitFile.getName() :
                    "${targetFilenamePrefix}_${splitFile.getName()}"
            File targetFile = new File(targetLocation, targetName)
            Files.copy(splitFile.toPath(), targetFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES)
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
