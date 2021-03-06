package nz.govt.natlib.tools.sip.pdf.thumbnail

import groovy.util.logging.Log4j2
import nz.govt.natlib.m11n.tools.automation.shell.ShellCommand
import nz.govt.natlib.m11n.tools.automation.shell.ShellException
import nz.govt.natlib.tools.sip.state.SipProcessingException
import nz.govt.natlib.tools.sip.state.SipProcessingExceptionReason
import nz.govt.natlib.tools.sip.state.SipProcessingExceptionReasonType
import nz.govt.natlib.tools.sip.utils.PathUtils

import java.nio.file.Path
import java.util.regex.Pattern

@Log4j2
class CommandLinePdfToThumbnailFileGenerator {
    static final Pattern JPG_FILE_EXTENSION_PATTERN = ~/.*\.[jJ]{1}[pP]{1}[gG]{1}/
    static final Pattern JPEG_FILE_EXTENSION_PATTERN = ~/.*\.[jJ]{1}[pP]{1}[eE]{0,1}[gG]{1}/
    static final Pattern PNG_FILE_EXTENSION_PATTERN = ~/.*\.[pP]{1}[nN]{1}[gG]{1}/

    static List<Path> generateThumbnails(Path pdfFile, Path targetDirectory, String prefix, String suffix,
                                         ThumbnailParameters parameters, boolean wouldHaveCaption = true,
                                         boolean throwExceptionOnFailure = false) throws SipProcessingException {
        List<Path> thumbnailFiles = [ ]
        String commandString
        ShellCommand shellCommand
        String generatedSuffix
        if (isPngFilename(suffix)) {
            generatedSuffix = "png"
            commandString = createPdftoppmPngCommand(pdfFile, prefix, parameters, wouldHaveCaption)
            shellCommand = generate(pdfFile, targetDirectory, commandString, throwExceptionOnFailure)
        } else if (isJpegFilename(suffix)) {
            generatedSuffix = "jpg"
            commandString = createPdftoppmJpegCommand(pdfFile, prefix, parameters, wouldHaveCaption)
            shellCommand = generate(pdfFile, targetDirectory, commandString, throwExceptionOnFailure)
        } else {
            log.warn("prefix=${prefix}, suffix=${suffix} is neither JPEG or PNG. Cannot convert to thumbnail.")
        }
        int exitValue = shellCommand == null ? -1 : (int) shellCommand.exitValue
        if (exitValue == 0) {
            List<Path> generatedThumbnailFiles = findThumbnailFiles(targetDirectory, prefix, generatedSuffix)
            generatedThumbnailFiles.eachWithIndex { Path thumbnailFile, int index ->
                Path desiredFile = targetDirectory.resolve("${prefix}-${index + 1}${suffix}")
                thumbnailFile.renameTo(desiredFile.toUri())
                thumbnailFiles.add(desiredFile)
            }
        } else {
            log.warn("Unable to create prefix=${prefix}, suffix=${suffix} thumbnail files via pdftoppm in targetDirectory=${targetDirectory.canonicalPath}, exitValue=${shellCommand.exitValue}")
        }
        return thumbnailFiles
    }

    static ShellCommand generate(Path pdfFile, Path targetDirectory, String commandWithParameters,
                         boolean throwExceptionOnFailure = false) {
        ShellCommand shellCommand = new ShellCommand()
        shellCommand.showOutput = true
        shellCommand.clearOutputOnCommandCompletion = true
        ShellException shellException
        try {
            shellCommand.executeOnShellWithWorkingDirectory(commandWithParameters, targetDirectory.toFile())
        } catch (ShellException e) {
            shellException = e
        }
        if (shellException != null || shellCommand.hasError()) {
            String reason
            if (shellException != null) {
                reason = shellException.toString()
            } else {
                reason = "Error processing pdfFile=${pdfFile.normalize()}, shell command failure, " +
                        "exitValue=${shellCommand.exitValue} command='${commandWithParameters}'"
            }
            SipProcessingExceptionReason exceptionReason = new SipProcessingExceptionReason(
                    SipProcessingExceptionReasonType.SHELL_COMMAND_FAILURE, null,
                    reason)
            log.error(exceptionReason.toString())
            if (throwExceptionOnFailure) {
                SipProcessingException sipProcessingException = SipProcessingException.createWithReason(exceptionReason)
                throw sipProcessingException
            }
        }
        return shellCommand
    }

    static List<Path> findThumbnailFiles(Path targetDirectory, String targetFilePrefix, String targetFileSuffix) {
        String patternToMatch = targetFilePrefix + '-\\d+\\.' + targetFileSuffix
        List<Path> matchingFiles = PathUtils.findFiles(targetDirectory.normalize().toString(), true, true, true,
                patternToMatch, null, false, false)

        return matchingFiles
    }
    
    static String createPdftoppmPngCommand(Path pdfFile, String targetFilePrefix, ThumbnailParameters parameters,
                                           boolean wouldHaveCaption = true) {
        return "pdftoppm -f 1 -scale-to-y ${parameters.adjustedThumbnailHeight(wouldHaveCaption)} -scale-to-x -1 -png " +
                "\"${pdfFile.normalize()}\" \"${targetFilePrefix}\""
    }

    static String createPdftoppmJpegCommand(Path pdfFile, String targetFilePrefix, ThumbnailParameters parameters,
                                            boolean wouldHaveCaption = true) {
        int actualQuality = parameters.quality
        if (parameters.quality < 0 || parameters.quality > 100) {
            actualQuality = actualQuality < 0 ? 0 : actualQuality
            actualQuality = actualQuality > 100 ? 100 : actualQuality
        }
        return "pdftoppm -f 1 -scale-to-y ${parameters.adjustedThumbnailHeight(wouldHaveCaption)} " +
                "-scale-to-x -1 -jpeg -jpegopt \"quality=${actualQuality}\" \"${pdfFile.normalize()}\" \"${targetFilePrefix}\""
    }

    static boolean isPngFilename(String filename) {
        return filename =~ PNG_FILE_EXTENSION_PATTERN
    }

    static boolean isJpegFilename(String filename) {
        return filename =~ JPG_FILE_EXTENSION_PATTERN || filename =~ JPEG_FILE_EXTENSION_PATTERN
    }
}
