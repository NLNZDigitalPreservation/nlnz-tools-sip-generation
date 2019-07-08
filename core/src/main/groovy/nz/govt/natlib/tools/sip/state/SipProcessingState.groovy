package nz.govt.natlib.tools.sip.state

import groovy.transform.AutoClone
import groovy.transform.Canonical
import nz.govt.natlib.tools.sip.IEEntityType
import org.apache.commons.lang3.StringUtils

import java.nio.charset.StandardCharsets
import java.nio.file.Path

@Canonical
@AutoClone
class SipProcessingState {
    final static String SUCCESSFUL = "successful"
    final static String EMPTY_SIP_AS_XML = ""

    boolean complete = false
    String identifier
    List<SipProcessingException> exceptions = [ ]
    Path processingOutputPath

    List<Path> sipFiles = [ ]
    List<Path> thumbnailPageFiles = [ ]
    List<Path> validFiles = [ ]
    List<Path> invalidFiles = [ ]
    List<Path> ignoredFiles = [ ]
    List<Path> unrecognizedFiles = [ ]

    int totalFilesProcessed

    IEEntityType ieEntityType = IEEntityType.UNKNOWN

    String sipAsXml = EMPTY_SIP_AS_XML

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
        tempPath.withWriter(StandardCharsets.UTF_8.name()) { Writer writer ->
            writer.write(toString())
        }

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

        appendFileList("sipFiles", initialOffset, sipFiles, stringBuilder)

        appendFileList("thumbnailPageFiles", initialOffset, thumbnailPageFiles, stringBuilder)

        appendFileList("validFiles", initialOffset, validFiles, stringBuilder)

        appendFileList("invalidFiles", initialOffset, invalidFiles, stringBuilder)

        appendFileList("ignoredFiles", initialOffset, ignoredFiles, stringBuilder)

        appendFileList("unrecognizedFiles", initialOffset, unrecognizedFiles, stringBuilder)

        return stringBuilder.toString()
    }

    private void appendFileList(String typeTitle, String initialOffset, List<Path> files, StringBuilder stringBuilder) {
        stringBuilder.append("${initialOffset}    ${typeTitle}=${files.size()}")
        if (files.size() > 0) {
            stringBuilder.append(":")
        }
        stringBuilder.append(System.lineSeparator())
        files.each { Path file ->
            stringBuilder.append("${initialOffset}        ${file.normalize().toString()}")
            stringBuilder.append(System.lineSeparator())
        }
    }

    private void appendException(StringBuilder stringBuilder, int offset, SipProcessingException exception) {
        stringBuilder.append(exception.toString(offset))
    }
}
