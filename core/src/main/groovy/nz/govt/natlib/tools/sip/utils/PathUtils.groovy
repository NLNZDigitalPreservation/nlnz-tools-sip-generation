package nz.govt.natlib.tools.sip.utils

import groovy.io.FileType
import groovy.util.logging.Log4j2
import nz.govt.natlib.tools.sip.logging.DefaultTimekeeper
import nz.govt.natlib.tools.sip.logging.Timekeeper
import nz.govt.natlib.tools.sip.state.SipProcessingException
import org.apache.commons.io.FilenameUtils

import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Log4j2
class PathUtils {
    static final SimpleDateFormat FILE_TIMESTAMP_FORMATTER = new SimpleDateFormat('yyyy-MM-dd_HH-mm-ss-SSS')
    static final String MD5_HASH_ZERO_LENGTH_FILE = "d41d8cd98f00b204e9800998ecf8427e"
    static final String FILENAME_UNSAFE_CHARACTERS = ' *$'
    static final String REPLACEMENT_FILENAME_SAFE_CHARACTER = "-"
    static final String FILE_PATH_SEPARATORS = ':/\\'
    static final String REPLACEMENT_FILE_PATH_SEPARATOR = "_"

    @Log4j2
    static class AtomicDirectoryMoveFileVisitor implements FileVisitor<Path> {
        boolean move
        Path sourceRootDirectory
        Path targetRootDirectory
        boolean useAtomicOption
        boolean includeDetailedTimings
        Timekeeper atomicTimekeeper

        AtomicDirectoryMoveFileVisitor(boolean move, Path sourceRootDirectory, Path targetRootDirectory,
                                       boolean useAtomicOption, boolean includeDetailedTimings = false,
                                       Timekeeper atomicTimekeeper = null) {
            this.move = move
            this.sourceRootDirectory = sourceRootDirectory
            this.targetRootDirectory = targetRootDirectory
            this.useAtomicOption = useAtomicOption
            this.includeDetailedTimings = includeDetailedTimings
            this.atomicTimekeeper = atomicTimekeeper
        }

        @Override
        FileVisitResult preVisitDirectory(Path dirPath, BasicFileAttributes attrs) throws IOException {
            Path relativePath = sourceRootDirectory.relativize(dirPath)
            Path targetDirectory = targetRootDirectory.resolve(relativePath)
            if (Files.notExists(targetDirectory)) {
                Files.createDirectories(targetDirectory)
            }
            return FileVisitResult.CONTINUE
        }

        @Override
        FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {
            // Given a file
            Path relativePath = sourceRootDirectory.relativize(filePath)
            Path targetFile = targetRootDirectory.resolve(relativePath)
            boolean moveCopyResult = atomicMoveOrCopy(move, filePath, targetFile, useAtomicOption,
                    includeDetailedTimings, atomicTimekeeper)

            if (!moveCopyResult) {
                String message = "Failed to move file=${filePath} from source=${sourceRootDirectory} to target=${targetRootDirectory}"
                throw new IOException(message)
            }
            return FileVisitResult.CONTINUE
        }

        @Override
        FileVisitResult visitFileFailed(Path filePath, IOException exc) throws IOException {
            return FileVisitResult.TERMINATE
        }

        @Override
        FileVisitResult postVisitDirectory(Path dirPath, IOException exc) throws IOException {
            if (move) {
                if (Files.isDirectory(dirPath) && dirPath.toFile().list().length == 0) {
                    Files.deleteIfExists(dirPath)
                } else {
                    String message = "After move, directory not empty, failed to delete directory=${dirPath} from source=${sourceRootDirectory} to target=${targetRootDirectory}"
                    throw new IOException(message)
                }
            }
            return FileVisitResult.CONTINUE
        }
    }

    static List<String> asSegments(Path path) {
        path.iterator().collect { Path subPath ->
            subPath.toString()
        }
    }

    static String fileNameAsSafeString(String stringWithUnsafeCharacters) {
        String safeString = stringWithUnsafeCharacters
        FILENAME_UNSAFE_CHARACTERS.each { String unsafeCharacter ->
            safeString = safeString.replace(unsafeCharacter, REPLACEMENT_FILENAME_SAFE_CHARACTER)
        }
        FILE_PATH_SEPARATORS.each { String pathCharacter ->
            safeString = safeString.replace(pathCharacter, REPLACEMENT_FILE_PATH_SEPARATOR)
        }
        return safeString
    }

    static String filePathAsSafeString(Path filePath, int totalSegments) {
        List<String> segments = asSegments(filePath.normalize())
        int pathLength = segments.size()
        int numberSegments = totalSegments <= 0 ? pathLength : totalSegments
        int startingIndex = pathLength - numberSegments > 0 ? pathLength - numberSegments : 0
        StringBuilder unsafeBuilder = new StringBuilder()
        for (int pathIndex = startingIndex; pathIndex <=  pathLength - 1; pathIndex++) {
            unsafeBuilder.append(segments.get(pathIndex))
            if (pathIndex < pathLength - 1) {
                unsafeBuilder.append(File.separator)
            }
        }
        return fileNameAsSafeString(unsafeBuilder.toString())
    }

    // Note that this method depends on the format of a given directory matching dates in the given range
    static List<Path> allSubdirectoriesInDateRange(Path rootDirectory, LocalDate startingDate, LocalDate endingDate,
                                                   DateTimeFormatter localDateFormatter = GeneralUtils.DATE_YYYYMMDD_FORMATTER,
                                                   boolean sort = true) {
        List<Path> allSubdirectories = [ ]
        List<String> datesInRange = GeneralUtils.datesAsStringsInRange(startingDate, endingDate, localDateFormatter)
        rootDirectory.traverse(type: FileType.DIRECTORIES) { Path directory ->
            String normalizedPath = directory.normalize().toString()
            boolean directoryMatches = datesInRange.any { String date ->
                normalizedPath.contains(date)
            }
            if (directoryMatches) {
                allSubdirectories.add(directory)
            }
        }
        // We could possibly sort by the date contained in the directory path, but we will assume some kind of ordering
        // that already exists in the directory structure. Otherwise we would create a map by date with a list of
        // directories and then go through each date and add the sorted list to the list of allSubdirectories.
        if (sort) {
            return allSubdirectories.sort() { Path file1, Path file2 ->
                file1.normalize() <=> file2.normalize()
            }
        } else {
            return allSubdirectories
        }
    }

    static List<Path> allSubdirectories(Path rootDirectory, boolean sort = true) {
        List<Path> allSubdirectories = [ ]
        rootDirectory.traverse(type: FileType.DIRECTORIES) { Path directory ->
            allSubdirectories.add(directory)
        }
        if (sort) {
            return allSubdirectories.sort() { Path file1, Path file2 ->
                file1.normalize() <=> file2.normalize()
            }
        } else {
            return allSubdirectories
        }
    }

    static List<Path> findFiles(String localPath, boolean isRegexNotGlob, boolean matchFilenameOnly,
                                boolean sortFiles, String pattern, Timekeeper timekeeper = null,
                                boolean includeSubdirectories = true, boolean useDebug = false) {
        List<Path> filesList = [ ]
        Path filesPath = Paths.get(localPath)
        if (!Files.exists(filesPath) || Files.isRegularFile(filesPath)) {
            log.warn("Path '${filesPath}' does not exist is not a directory. Returning empty file list.")
            return filesList
        }

        String message = "Finding files for path=${filesPath.normalize()} and pattern=${pattern}"
        if (useDebug) {
            log.debug(message)
        } else {
            log.info(message)
        }
        if (timekeeper != null) {
            timekeeper.logElapsed(useDebug)
        }

        boolean directoryOnly = false
        filesList = FilesFinder.getMatchingFilesFull(filesPath, isRegexNotGlob, matchFilenameOnly, sortFiles,
                includeSubdirectories, directoryOnly, pattern)

        message = "Found total files=${GeneralUtils.TOTAL_FORMAT.format(filesList.size())} for path=${filesPath.normalize()}"
        if (useDebug) {
            log.debug(message)
        } else {
            log.info(message)
        }
        if (timekeeper != null) {
            timekeeper.logElapsed(useDebug)
        }

        return filesList
    }

    static List<Path> findNonMatchingFiles(String localPath, boolean isRegexNotGlob, boolean matchFilenameOnly,
                                           boolean sortFiles, String pattern, Timekeeper timekeeper) {
        List<Path> filesList = [ ]
        Path filesPath = Paths.get(localPath)
        if (!Files.exists(filesPath) || !Files.isDirectory(filesPath)) {
            log.warn("Path '${filesPath}' does not exist is not a directory. Returning empty file list.")
            return filesList
        }

        log.info("Finding files for path=${filesPath.normalize()} and pattern=${pattern}")
        timekeeper.logElapsed()
        filesList = FilesFinder.getNonMatchingFiles(filesPath, isRegexNotGlob, matchFilenameOnly, sortFiles, pattern)
        log.info("Found total files=${GeneralUtils.TOTAL_FORMAT.format(filesList.size())} for path=${filesPath.normalize()}")
        timekeeper.logElapsed()

        return filesList
    }

    static List<Path> matchFiles(List<Path> allFiles, String pattern) {
        List<Path> matchedFiles = [ ]
        allFiles.each { Path file ->
            if (file.getFileName() ==~ /${pattern}/) {
                matchedFiles.add(file)
            }
        }

        return matchedFiles
    }

    static void copyOrMoveFiles(boolean moveFiles, List<Path> sourceFiles, Path destination) {
        sourceFiles.each { Path sourceFile ->
            Path destinationFile = destination.resolve(sourceFile.getFileName())
            atomicMoveOrCopy(moveFiles, sourceFile, destinationFile)
        }
    }

    static boolean atomicMoveOrCopyDirectory(boolean moveDirectory, Path sourceDirectory, Path targetDirectory,
                                             boolean useAtomicOption, boolean includeDetailedTimings = false,
                                             Timekeeper atomicTimekeeper = null) {
        if (Files.isRegularFile(sourceDirectory)) {
            throw new SipProcessingException("atomicMoveOrCopyDirectory is for directories, not files. sourceDirectory=${sourceDirectory.normalize()} is a file. Use atomicMoveOrCopy instead.")
        }
        // Handle the case of being interrupted by copying/moving to the destination file (which leads to a bunch
        // partial copies -- especially in a multithreaded case -- that need to be manually checked to verify that
        // they are incomplete versions).
        // Instead, copy/move the file to a temporary-named file and then rename the file when the copy is complete.
        //
        // We only do a 'move' if we can do an atomic move, otherwise we do the following:
        // 1. Copy the file across with a '.tmpcopy' extension.
        // 2. Rename the file to the targetFile name.
        // 3. Delete the sourceFile.
        // This guarantees that we never delete the source file until the file has been copied and renamed.
        if (Files.exists(sourceDirectory) && Files.exists(targetDirectory) &&
                Files.isSameFile(sourceDirectory, targetDirectory)) {
            log.warn("atomicMoveOrCopyDirectory: NO move/copy -- source and target are the same PHYSICAL directory!")
            log.warn("    sourceDirectory=${sourceDirectory.normalize()}")
            log.warn("    targetDirectory=${targetDirectory.normalize()}")
            return false
        }
        Timekeeper theTimekeeper = atomicTimekeeper
        if (includeDetailedTimings && atomicTimekeeper == null) {
            theTimekeeper = new DefaultTimekeeper()
            theTimekeeper.start()
        }
        boolean deleteSourceDirectory = moveDirectory && !useAtomicOption // because atomic move will automatically delete sourceDirectory
        boolean doCopy = !moveDirectory
        boolean renameSuccessful = false
        Path temporaryTargetDirectory = nonDuplicateFile(targetDirectory, true, "-",
                true, ".tmpcopy")
        if (includeDetailedTimings) {
            GeneralUtils.markElapsed(theTimekeeper, "sourceDirectory=${sourceDirectory.fileName}",
                    "Establish non-duplicate directory for sourceDirectory path=" + sourceDirectory.normalize())
        }
        if (moveDirectory) {
            // The only valid move option is StandardCopyOption.REPLACE_EXISTING, which we don't want to do
            if (useAtomicOption) {
                try {
                    Path resultingPath = Files.move(sourceDirectory, targetDirectory, StandardCopyOption.ATOMIC_MOVE)
                    GeneralUtils.markElapsed(theTimekeeper, "sourceDirectory=${sourceDirectory.fileName}",
                            "Atomic move completed")
                    renameSuccessful = true
                } catch (IOException e) { // AtomicMoveNotSupportedException but also IOException
                    log.debug("Attempt at atomic file move file sourceDirectory=${sourceDirectory.fileName} to " +
                            "targetFile=${targetDirectory.normalize()} failed, trying a non-atomic move approach.", e)
                    renameSuccessful = atomicMoveOrCopyDirectory(moveDirectory, sourceDirectory, targetDirectory, false,
                            false, theTimekeeper)
                    GeneralUtils.markElapsed(theTimekeeper, "sourceDirectory=${sourceDirectory.fileName}",
                            "Non-atomic move completed")
                }
            } else {
                doCopy = true
            }
        }
        if (doCopy) {
            AtomicDirectoryMoveFileVisitor atomicFileMover = new AtomicDirectoryMoveFileVisitor(moveDirectory,
                    sourceDirectory, temporaryTargetDirectory, useAtomicOption, includeDetailedTimings, atomicTimekeeper)
            Files.walkFileTree(sourceDirectory, atomicFileMover)
            GeneralUtils.markElapsed(theTimekeeper,
                    "sourceDirectory=${sourceDirectory.fileName}, temporaryTargetDirectory=${temporaryTargetDirectory}",
                    "Copy completed")
            renameSuccessful = temporaryTargetDirectory.toFile().renameTo(targetDirectory.toFile())
            GeneralUtils.markElapsed(theTimekeeper,
                    "sourceDirectory=${sourceDirectory.fileName}, targetDirectory=${targetDirectory}",
                    "Rename completed")
            if (renameSuccessful) {
                if (moveDirectory) {
                    // Note that the sourceDirectory should be empty by now (but also should have been deleted by the FileVisitor)
                    if (Files.exists(sourceDirectory)) {
                        log.warn("sourceDirectory=${sourceDirectory.normalize()} should have been deleted by AtomicDirectoryMoveFileVisitor")
                        org.apache.commons.io.FileUtils.forceDelete(sourceDirectory.toFile())
                        GeneralUtils.markElapsed(theTimekeeper, "sourceDirectory=${sourceDirectory.fileName}",
                                "Delete completed")
                    }
                }
            } else {
                GeneralUtils.printAndFlush("\n")
                log.error("Unable to rename temporaryTargetDirectory=${temporaryTargetDirectory.normalize()} " +
                        "to targetDirectory=${targetDirectory.normalize()}")
                if (deleteSourceDirectory) {
                    log.error("Not deleting sourceDirectory=${sourceDirectory.normalize()}, as rename was NOT successful")
                }
            }
        }

        if (includeDetailedTimings && theTimekeeper != null) {
            GeneralUtils.markElapsed(theTimekeeper, "sourceDirectory=${sourceDirectory.fileName}, targetDirectory=${targetDirectory}",
                    "Operation completed.")
            theTimekeeper.listMarkers()
        }
        return renameSuccessful
    }

    static boolean atomicMoveOrCopy(boolean moveFile, Path sourceFile, Path targetFile,
                                    boolean useAtomicOption = true, boolean includeDetailedTimings = false,
                                    Timekeeper atomicTimekeeper = null) {
        if (Files.isDirectory(sourceFile)) {
            throw new SipProcessingException("atomicMoveOrCopy is for files, not directories. sourceFile=${sourceFile.normalize()} is a directory. Use atomicMoveOrCopyDirectory instead.")
        }
        // Handle the case of being interrupted by copying/moving to the destination file (which leads to a bunch
        // partial copies -- especially in a multithreaded case -- that need to be manually checked to verify that
        // they are incomplete versions).
        // Instead, copy/move the file to a temporary-named file and then rename the file when the copy is complete.
        //
        // We only do a 'move' if we can do an atomic move, otherwise we do the following:
        // 1. Copy the file across with a '.tmpcopy' extension.
        // 2. Rename the file to the targetFile name.
        // 3. Delete the sourceFile.
        // This guarantees that we never delete the source file until the file has been copied and renamed.
        if (Files.exists(sourceFile) && Files.exists(targetFile) && Files.isSameFile(sourceFile, targetFile)) {
            log.warn("atomicMoveOrCopy: NO move/copy -- source and target are the same PHYSICAL file!")
            log.warn("    sourceFile=${sourceFile.normalize()}")
            log.warn("    targetFile=${targetFile.normalize()}")
            return false
        }
        Timekeeper theTimekeeper = atomicTimekeeper
        if (includeDetailedTimings && atomicTimekeeper == null) {
            theTimekeeper = new DefaultTimekeeper()
            theTimekeeper.start()
        }
        boolean deleteSourceFile = moveFile && !useAtomicOption // because atomic move will automatically delete sourceFile
        boolean doCopy = !moveFile
        boolean renameSuccessful = false
        Path temporaryDestinationFile = nonDuplicateFile(targetFile, true, "-",
                true, ".tmpcopy")
        if (includeDetailedTimings) {
            GeneralUtils.markElapsed(theTimekeeper, "sourceFile=${sourceFile.fileName}",
                    "Establish non-duplicate file for sourceFile path=" + sourceFile.normalize())
        }
        if (moveFile) {
            // The only valid move option is StandardCopyOption.REPLACE_EXISTING, which we don't want to do
            if (useAtomicOption) {
                try {
                    Path resultingPath = Files.move(sourceFile, targetFile, StandardCopyOption.ATOMIC_MOVE)
                    GeneralUtils.markElapsed(theTimekeeper, "sourceFile=${sourceFile.fileName}",
                            "Atomic move completed")
                    renameSuccessful = true
                } catch (AtomicMoveNotSupportedException e) {
                    log.debug("Attempt at atomic file move file sourceFile=${sourceFile.normalize()} to " +
                            "targetFile=${targetFile.normalize()} failed, trying a non-atomic move approach.")
                    renameSuccessful = atomicMoveOrCopy(moveFile, sourceFile, targetFile, false,
                                                        false, theTimekeeper)
                    GeneralUtils.markElapsed(theTimekeeper, "sourceFile=${sourceFile.fileName}",
                            "Non-atomic move completed")
                }
            } else {
                doCopy = true
            }
        }
        if (doCopy) {
            Files.copy(sourceFile, temporaryDestinationFile, StandardCopyOption.COPY_ATTRIBUTES)
            GeneralUtils.markElapsed(theTimekeeper, "sourceFile=${sourceFile.fileName}", "Copy completed")
            renameSuccessful = temporaryDestinationFile.toFile().renameTo(targetFile.toFile())
            GeneralUtils.markElapsed(theTimekeeper, "sourceFile=${sourceFile.fileName}", "Rename completed")
            if (renameSuccessful) {
                if (deleteSourceFile) {
                    Files.delete(sourceFile)
                    GeneralUtils.markElapsed(theTimekeeper, "sourceFile=${sourceFile.fileName}", "Delete completed")
                }
            } else {
                GeneralUtils.printAndFlush("\n")
                log.error("Unable to rename temporaryDestinationFile=${temporaryDestinationFile.normalize()} " +
                        "to destinationFile=${targetFile.normalize()}")
                if (deleteSourceFile) {
                    log.error("Not deleting sourceFile=${sourceFile}, as rename was NOT successful")
                }
            }
        }

        if (includeDetailedTimings && theTimekeeper != null) {
            GeneralUtils.markElapsed(theTimekeeper, "sourceFile=${sourceFile.fileName}", "Operation completed.")
            theTimekeeper.listMarkers()
        }
        return renameSuccessful
    }

    // Hash the files to determine if they are the same file.
    static boolean isSameFile(Path file1, Path file2, allowZeroLengthFiles = false) {
        // Skip MD5 hash if the files are the same physical file. Note that this will skip the zero-length file check.
        if (Files.isSameFile(file1, file2)) {
            return true
        }
        String file1Md5Hash = generateMD5(file1)
        String file2Md5Hash = generateMD5(file2)

        if (!allowZeroLengthFiles) {
            boolean hasZeroLengthHashes = false
            String message = ""
            if (MD5_HASH_ZERO_LENGTH_FILE.equals(file1Md5Hash)) {
                hasZeroLengthHashes = true
                message += "file=${file1.normalize()} has a prohibited zero-length file MD5 hash=${file1Md5Hash}"
            }
            if (MD5_HASH_ZERO_LENGTH_FILE.equals(file2Md5Hash)) {
                hasZeroLengthHashes = true
                if (message.length() > 0) {
                    message += ", "
                }
                message += "file=${file2.normalize()} has a prohibited zero-length file MD5 hash=${file2Md5Hash}"
            }
            if (hasZeroLengthHashes) {
                throw new SipProcessingException(message)
            }
        }
        return file1Md5Hash.equals(file2Md5Hash)
    }

    static String generateMD5(Path file) {
        MessageDigest digest = MessageDigest.getInstance("MD5")
        file.eachByte(4096) { byte[] buffer, int length ->
            digest.update(buffer, 0, length)
        }
        return digest.digest().encodeHex() as String
    }

    static Path nonDuplicateFile(Path originalFile, boolean usePreIndex = true, String preIndexString = "-DUPLICATE-",
                                 boolean useAdditionalExtension = false, String additionalExtension = ".tmp") {
        String fileName = originalFile.fileName
        String baseName = FilenameUtils.getBaseName(fileName)
        String extension = FilenameUtils.getExtension(fileName)
        Path parentFile = originalFile.parent
        Path candidateFile = null
        boolean alreadyExists = true
        int duplicateIndexCount = 0
        while (alreadyExists) {
            String preDuplicateIndexString = usePreIndex ? preIndexString : ""
            String extraExtension = useAdditionalExtension ? additionalExtension : ""
            String candidateFileName = baseName + preDuplicateIndexString + duplicateIndexCount + "." +
                    extension + extraExtension
            candidateFile = parentFile.resolve(candidateFileName)
            alreadyExists = Files.exists(candidateFile)
            duplicateIndexCount += 1
        }
        return candidateFile
    }

    static Path writeResourceToTemporaryDirectory(String filename, String temporaryDirectoryPrefix, String resourcePath,
                                                  String resourceName, Path parentDirectory = null,
                                                  boolean deleteOnExit = true) {
        Path actualParentDirectory = parentDirectory == null ?
                org.apache.commons.io.FileUtils.tempDirectory.toPath() :
                parentDirectory

        Path temporaryDirectory = Files.createTempDirectory(actualParentDirectory, temporaryDirectoryPrefix)
        if (deleteOnExit) {
            temporaryDirectory.toFile().deleteOnExit()
        }
        Path tempFile = temporaryDirectory.resolve(filename)

        Path sourceFile = getResourceAsPath(resourcePath, resourceName)

        if (sourceFile == null) {
            log.warn("Unable to convert resourcePath=${resourcePath}, resourceName=${resourceName} to File.")
            log.warn("No return of temporary file=${tempFile.normalize()}, as resource cannot be copied into it.")
            tempFile = null
        } else {
            Files.copy(sourceFile, tempFile)
        }

        return tempFile
    }

    static Path getResourceAsPath(String resourcePath, String resourceName) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader()
        String adjustedPath = (resourcePath == null || resourcePath.strip().isEmpty()) ?
                resourceName : (resourcePath.endsWith("/") ?
                    "${resourcePath}${resourceName}" : "${resourcePath}/${resourceName}")
        URL url = loader.getResource(adjustedPath)

        Path resourceFile = url == null ? null : Path.of(url.toURI())

        return resourceFile
    }
}
