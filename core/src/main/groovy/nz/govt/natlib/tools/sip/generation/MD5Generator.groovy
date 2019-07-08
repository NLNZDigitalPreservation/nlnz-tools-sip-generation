package nz.govt.natlib.tools.sip.generation

import groovy.util.logging.Log4j2
import nz.govt.natlib.tools.sip.state.SipProcessingException
import org.apache.commons.io.FilenameUtils

import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

@Log4j2
class MD5Generator {
    static int DEFAULT_MD5_DIGEST_BUFFER_LENGTH = 4096
    static String MD5_ALGORITHM = "MD5"

    static String generateOrReadMD5HashFile(Path sourceFile, boolean overwriteIfAlreadyExists) throws SipProcessingException {
        String md5Hash
        Path md5File = determineMd5File(sourceFile)
        boolean alreadyExists = Files.exists(md5File)
        if (alreadyExists && overwriteIfAlreadyExists) {
            log.info("MD5 file=${sourceFile.normalize().toString()} already exists, but will recreate.")
        }
        if (!alreadyExists || (alreadyExists && overwriteIfAlreadyExists)) {
            md5Hash = calculateMd5Hash(sourceFile)
            writeMD5ToFile(md5File, md5Hash)
        } else {
            md5Hash = readMD5Hash(md5File)
        }
        log.info("Source file=${sourceFile.normalize().toString()} has MD5-Hash=${md5Hash}")
        return md5Hash
    }

    static String readMD5Hash(Path md5SourceFile) throws SipProcessingException {
        String md5Hash
        if (Files.exists(md5SourceFile)) {
            md5SourceFile.withReader { Reader reader ->
                md5Hash = reader.readLine()
            }
        } else {
            String message = "MD5 source file=${md5SourceFile.normalize().toString()} does not exist. Unable to read MD5 hash."
            log.error(message)
            throw new SipProcessingException(message)
        }
        return md5Hash
    }

    static String calculateMd5Hash(Path file) throws SipProcessingException {
        if (isValidSourceFile(file)) {
            MessageDigest messageDigest = MessageDigest.getInstance(MD5_ALGORITHM)
            file.eachByte(DEFAULT_MD5_DIGEST_BUFFER_LENGTH) { byte[] buffer, Integer length ->
                messageDigest.update(buffer, 0, length)
            }
            return messageDigest.digest().encodeHex() as String
        }
        return null
    }

    static String calculateMd5Hash(String string) throws SipProcessingException {
        if (string != null && string.length() > 0) {
            MessageDigest messageDigest = MessageDigest.getInstance(MD5_ALGORITHM)
            messageDigest.update(string.getBytes())
            return messageDigest.digest().encodeHex() as String
        }
        return null
    }

    static boolean isValidSourceFile(Path file) throws SipProcessingException {
        if (!Files.exists(file)) {
            throw new SipProcessingException("File=${file.normalize().toString()} does not exist. Cannot generate MD5 from file.")
        }
        if (!Files.isRegularFile(file)) {
            throw new SipProcessingException("File=${file.normalize().toString()} is not a regular file. Cannot generate MD5 from file.")
        }
        return true
    }

    static Path determineMd5File(Path sourceFile) throws SipProcessingException {
        Path parentFolder = sourceFile.parent
        String sourceFilename = sourceFile.fileName.toString()
        String sourceFileWithoutExtension = FilenameUtils.removeExtension(sourceFilename)
        Path md5File = parentFolder.resolve("${sourceFileWithoutExtension}.md5")
        return md5File
    }

    static void writeMD5ToFile(Path destinationFile, String md5Hash) {
        destinationFile.withWriter('UTF-8') { Writer writer ->
            writer.write(md5Hash)
            writer.flush()
        }
        log.info("MD5-Hash=${md5Hash} written to file=${destinationFile.normalize().toString()}")
    }
}
