package nz.govt.natlib.tools.sip.files

import groovy.io.FileType
import groovy.util.logging.Slf4j

import java.nio.file.DirectoryStream
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.PathMatcher

/**
 * Finds files using various criteria.
 */
@Slf4j
class FilesFinder {
    static DirectoryStream.Filter<Path> getPathFilter(boolean isRegexNotGlob, boolean matchFilenameOnly,
                                                      boolean directoryOnly = false, String... patterns) {
        FileSystem fileSystem = FileSystems.getDefault()
        final List<PathMatcher> pathMatchers = [ ]
        String matcherType = isRegexNotGlob ? "regex:" : "glob:"
        patterns.each { String pattern ->
            pathMatchers.add(fileSystem.getPathMatcher(matcherType + pattern))
        }
        return new DirectoryStream.Filter<Path>() {
            @Override
            boolean accept(Path entry) throws IOException {
                return pathMatchers.any { PathMatcher pathMatcher ->
                    boolean isDirectory = Files.isDirectory(entry)
                    boolean matched = directoryOnly ? isDirectory : true
                    if (matched) {
                        if (matchFilenameOnly) {
                            matched = pathMatcher.matches(entry.getFileName())
                        } else {
                            matched = pathMatcher.matches(entry.normalize())
                        }
                    }
                    //log.info("getPathFilter matched=${matched}, path=${entry.toFile().getCanonicalPath()}")
                    matched
                }
            }
        }
    }

    static List<File> getMatchingFiles(Path filesPath, boolean isRegexNotGlob, boolean matchFilenameOnly,
                                       boolean sortFiles = true, String... patterns) {
        boolean includeSubdirectories = false
        boolean directoryOnly = false
        return getMatchingFilesFull(filesPath, isRegexNotGlob, matchFilenameOnly, sortFiles, includeSubdirectories,
                directoryOnly, patterns)
    }

    // TODO We may want to unit test this method, however it is composed mostly of java.nio.file methods.
    // If we do decide to write unit tests, use the following as a guide:
    // https://stackoverflow.com/questions/47101232/mocking-directorystreampath-without-mocking-iterator-possible
    static List<File> getMatchingFilesFull(Path filesPath, boolean isRegexNotGlob, boolean matchFilenameOnly,
                                           boolean sortFiles = true, boolean includeSubdirectories = false,
                                           boolean directoryOnly = false, String... patterns) {
        List<File> matchingFiles = [ ]
        DirectoryStream.Filter<Path> pathFilter = getPathFilter(isRegexNotGlob, matchFilenameOnly, directoryOnly, patterns)

        matchingFiles = getMatchingFilesWithFilter(filesPath, pathFilter)

        if (includeSubdirectories) {
            File parentFolder = filesPath.toFile()
            parentFolder.eachFileRecurse(FileType.DIRECTORIES) { File subdirectory ->
                matchingFiles.addAll(getMatchingFilesWithFilter(subdirectory.toPath(), pathFilter))
            }
        }

        if (sortFiles) {
            return matchingFiles.toSorted { File a, File b -> a.getCanonicalPath() <=> b.getCanonicalPath() }
        } else {
            return matchingFiles
        }
    }

    static List<File> getMatchingFilesWithFilter(Path filesPath, DirectoryStream.Filter<Path> pathFilter) {
        List<File> matchingFiles = [ ]

        // Note that the 'try with resources pattern does not work with this version of groovy (2.4.x). Will work in 2.5.x.
        // This means that we must have a finally block to close the resource
        DirectoryStream directoryStream = Files.newDirectoryStream(filesPath, pathFilter)
        try /*(DirectoryStream directoryStream = Files.newDirectoryStream(filesPath, pattern))*/ {
            for (Path path : directoryStream) {
                matchingFiles.add(path.toFile())
            }
        } catch (IOException e) {
            log.warn("Unexpected exception filtering '${filesPath}': ${e}")
            // we re-throw because the caller should handle
            throw e
        } finally {
            directoryStream.close()
        }

        return matchingFiles
    }
}
