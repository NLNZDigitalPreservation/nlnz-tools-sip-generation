package nz.govt.natlib.tools.sip.generation

import groovy.util.logging.Slf4j
import nz.govt.natlib.tools.sip.state.SipProcessingException
import org.apache.commons.io.FilenameUtils

import java.security.MessageDigest

@Slf4j
class MD5Generator {
    static int DEFAULT_MD5_DIGEST_BUFFER_LENGTH = 4096
    static String MD5_ALGORITHM = "MD5"

    static String generateOrReadMD5HashFile(File sourceFile, boolean overwriteIfAlreadyExists) throws SipProcessingException {
        String md5Hash
        File md5File = determineMd5File(sourceFile)
        boolean alreadyExists = md5File.exists()
        if (alreadyExists && overwriteIfAlreadyExists) {
            log.info("MD5 file=${sourceFile.getCanonicalPath()} already exists, but will recreate.")
        }
        if (!alreadyExists || (alreadyExists && overwriteIfAlreadyExists)) {
            md5Hash = calculateMd5Hash(sourceFile)
            writeMD5ToFile(md5File, md5Hash)
        } else {
            md5Hash = readMD5Hash(md5File)
        }
        log.info("Source file=${sourceFile.getCanonicalPath()} has MD5-Hash=${md5Hash}")
        return md5Hash
    }

    static String readMD5Hash(File md5SourceFile) throws SipProcessingException {
        String md5Hash
        if (md5SourceFile.exists()) {
            md5SourceFile.withReader { BufferedReader reader ->
                md5Hash = reader.readLine()
            }
        } else {
            String message = "MD5 source file=${md5SourceFile.getCanonicalPath()} does not exist. Unable to read MD5 hash."
            log.error(message)
            throw new SipProcessingException(message)
        }
        return md5Hash
    }

    static String calculateMd5Hash(File file) throws SipProcessingException {
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

    static boolean isValidSourceFile(File file) throws SipProcessingException {
        if (!file.exists()) {
            throw new SipProcessingException("File=${file.getAbsolutePath()} does not exist. Cannot generate MD5 from file.")
        }
        if (!file.isFile()) {
            throw new SipProcessingException("File=${file.getAbsolutePath()} is not a file. Cannot generate MD5 from file.")
        }
        return true
    }

    static File determineMd5File(File sourceFile) throws SipProcessingException {
        File parentFolder = sourceFile.getCanonicalFile().getParentFile()
        String sourceFilename = sourceFile.getName()
        String sourceFileWithoutExtension = FilenameUtils.removeExtension(sourceFilename)
        File md5File = new File(parentFolder, "${sourceFileWithoutExtension}.md5")
        return md5File
    }

    static void writeMD5ToFile(File destinationFile, String md5Hash) {
        destinationFile.withWriter('UTF-8') { BufferedWriter writer ->
            writer.write(md5Hash)
            writer.flush()
        }
        log.info("MD5-Hash=${md5Hash} written to file=${destinationFile.getCanonicalPath()}")
    }
}
