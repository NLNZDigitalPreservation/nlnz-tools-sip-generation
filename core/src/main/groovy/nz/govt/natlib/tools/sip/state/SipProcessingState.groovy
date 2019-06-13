package nz.govt.natlib.tools.sip.state

import groovy.transform.AutoClone
import groovy.transform.Canonical
import nz.govt.natlib.tools.sip.IEEntityType
import org.apache.commons.lang3.StringUtils

import java.nio.file.Path

@Canonical
@AutoClone
class SipProcessingState {
    final static String SUCCESSFUL = "successful"

    boolean complete = false
    String identifier
    List<SipProcessingException> exceptions = [ ]
    Path processingOutputPath
    List<File> validFiles = [ ]
    List<File> invalidFiles = [ ]
    List<File> ignoredFiles = [ ]
    List<File> unrecognizedFiles = [ ]
    int totalFilesProcessed
    IEEntityType ieEntityType = IEEntityType.UNKNOWN

    boolean hasExceptions() {
        return exceptions.size() > 0
    }

    boolean isSuccessful() {
        return complete && exceptions.size() == 0
    }

    void addException(SipProcessingException sipProcessingException) {
        this.exceptions.add(sipProcessingException)
    }

    Path toTempFile(String filePrefix = "SipProcessingState-", String fileSuffix = ".txt",
                    boolean deleteOnExit = false) {
        File tempFile = File.createTempFile(filePrefix, fileSuffix)
        Path tempPath = tempFile.toPath()
        if (deleteOnExit) {
            tempFile.deleteOnExit()
        }
        tempPath.write(toString())

        return tempPath
    }

    String getFailureReasonSummary() {
        if (successful) {
            return SUCCESSFUL
        } else {
            String reasonSummary = SipProcessingException.DEFAULT_REASON_SUMMARY
            if (!exceptions.isEmpty()) {
                reasonSummary = exceptions.first().reasonSummary
            }
            return reasonSummary
        }
    }

    String toString() {
        return toString(0)
    }

    String toString(int offset) {
        String initialOffset = StringUtils.repeat(' ', offset)
        StringBuilder stringBuilder = new StringBuilder(initialOffset)
        stringBuilder.append(this.getClass().getName())
        stringBuilder.append(" (identifier=${identifier})")
        stringBuilder.append(": ")
        stringBuilder.append(complete ? "Complete" : "NOT Complete")
        stringBuilder.append(isSuccessful() ? ", Successful" : ", NOT Successful")
        stringBuilder.append(':')
        stringBuilder.append(System.lineSeparator())
        if (this.exceptions.size() > 1) {
            stringBuilder.append("${initialOffset}    Exceptions:")
            boolean first = true
            this.exceptions.each { SipProcessingException exception ->
                if (first) {
                    first = false
                } else {
                    stringBuilder.append(System.lineSeparator())
                }
                appendException(stringBuilder, offset + 8, exception)
            }
            stringBuilder.append(System.lineSeparator())
        } else if (this.exceptions.size() == 1) {
            appendException(stringBuilder, offset + 4, this.exceptions.first())
            stringBuilder.append(System.lineSeparator())
        }

        stringBuilder.append("${initialOffset}    totalFilesProcessed=${totalFilesProcessed}")
        stringBuilder.append(System.lineSeparator())
        stringBuilder.append("${initialOffset}    validFiles=${validFiles.size()}")
        if (validFiles.size() > 0) {
            stringBuilder.append(":")
        }
        stringBuilder.append(System.lineSeparator())
        validFiles.each { File file ->
            stringBuilder.append("${initialOffset}        ${file.getCanonicalPath()}")
            stringBuilder.append(System.lineSeparator())
        }
        stringBuilder.append("${initialOffset}    invalidFiles=${invalidFiles.size()}")
        if (invalidFiles.size() > 0) {
            stringBuilder.append(":")
        }
        stringBuilder.append(System.lineSeparator())
        invalidFiles.each { File file ->
            stringBuilder.append("${initialOffset}        ${file.getCanonicalPath()}")
            stringBuilder.append(System.lineSeparator())
        }
        stringBuilder.append("${initialOffset}    ignoredFiles=${ignoredFiles.size()}")
        if (ignoredFiles.size() > 0) {
            stringBuilder.append(":")
        }
        stringBuilder.append(System.lineSeparator())
        ignoredFiles.each { File file ->
            stringBuilder.append("${initialOffset}        ${file.getCanonicalPath()}")
            stringBuilder.append(System.lineSeparator())
        }
        stringBuilder.append("${initialOffset}    unrecognizedFiles=${unrecognizedFiles.size()}")
        if (unrecognizedFiles.size() > 0) {
            stringBuilder.append(":")
        }
        stringBuilder.append(System.lineSeparator())
        unrecognizedFiles.each { File file ->
            stringBuilder.append("${initialOffset}        ${file.getCanonicalPath()}")
            stringBuilder.append(System.lineSeparator())
        }
        return stringBuilder.toString()
    }

    private void appendException(StringBuilder stringBuilder, int offset, SipProcessingException exception) {
        stringBuilder.append(exception.toString(offset))
    }
}
