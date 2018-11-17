package nz.govt.natlib.tools.sip

import nz.govt.natlib.tools.sip.generation.MD5Generator
import nz.govt.natlib.tools.sip.generation.MediaMimeType
import org.apache.commons.io.FilenameUtils

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
import java.time.LocalDateTime
import java.time.ZoneOffset

class SipFileWrapperFactory {

    static Sip.FileWrapper generate(File file, boolean useFilenameOnly = false, boolean generateMD5Hash = false) {
        Objects.requireNonNull(file)

        Sip.FileWrapper fileWrapper = new Sip.FileWrapper()
        fileWrapper.file = file
        fileWrapper.fileOriginalName = file.name
        fileWrapper.fileOriginalPath = useFilenameOnly ? file.name : file.parentFile.canonicalPath
        if (file.exists()) {
            if (generateMD5Hash) {
                fileWrapper.fixityType = MD5Generator.MD5_ALGORITHM
                fileWrapper.fixityValue = MD5Generator.calculateMd5Hash(file)
            }
            fileWrapper.fileSizeBytes = file.length()
            Path path = file.toPath()
            BasicFileAttributes basicFileAttributes = Files.readAttributes(path, BasicFileAttributes.class)
            FileTime fileCreationTime = basicFileAttributes.creationTime()
            // TODO Do we want to use UTC? Is that the standard?
            fileWrapper.creationDate = LocalDateTime.ofInstant(fileCreationTime.toInstant(), ZoneOffset.UTC)
            FileTime fileModificationTime = basicFileAttributes.lastModifiedTime()
            fileWrapper.modificationDate = LocalDateTime.ofInstant(fileModificationTime.toInstant(), ZoneOffset.UTC)
        }
        String extension = FilenameUtils.getExtension(file.getName())
        fileWrapper.mimeType = MediaMimeType.forExtension(extension).mediaType.toString()

        return fileWrapper
    }
}
