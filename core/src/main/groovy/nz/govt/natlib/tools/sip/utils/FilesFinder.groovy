package nz.govt.natlib.tools.sip.utils

import groovy.io.FileType
import groovy.util.logging.Log4j2

import java.nio.file.DirectoryStream
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.PathMatcher


/**
 * Finds files using various criteria.
 */
@Log4j2
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
                    //log.info("getPathFilter matched=${matched}, path=${entry}")
                    matched
                }
            }
        }
    }

    /**
     * Returns a filter that matches all paths that <em>don't</em> match the given patterns.
     */
    static DirectoryStream.Filter<Path> getNonMatchPathFilter(boolean isRegexNotGlob, boolean matchFilenameOnly,
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
                            matched = !pathMatcher.matches(entry.getFileName())
                        } else {
                            matched = !pathMatcher.matches(entry.normalize())
                        }
                    }
                    //log.info("getPathFilter matched=${matched}, path=${entry}")
                    matched
                }
            }
        }
    }

    static List<Path> getMatchingFiles(Path filesPath, boolean isRegexNotGlob, boolean matchFilenameOnly,
                                       boolean sortFiles = true, String... patterns) {
        boolean includeSubdirectories = false
        boolean directoryOnly = false
        return getMatchingFilesFull(filesPath, isRegexNotGlob, matchFilenameOnly, sortFiles, includeSubdirectories,
                directoryOnly, patterns)
    }

    // TODO We may want to unit test this method, however it is composed mostly of java.nio.file methods.
    // If we do decide to write unit tests, use the following as a guide:
    // https://stackoverflow.com/questions/47101232/mocking-directorystreampath-without-mocking-iterator-possible
    static List<Path> getMatchingFilesFull(Path filesPath, boolean isRegexNotGlob, boolean matchFilenameOnly,
                                           boolean sortFiles = true, boolean includeSubdirectories = false,
                                           boolean directoryOnly = false, String... patterns) {
        List<Path> matchingFiles = [ ]
        DirectoryStream.Filter<Path> pathFilter = getPathFilter(isRegexNotGlob, matchFilenameOnly, directoryOnly, patterns)

        matchingFiles = getMatchingFilesWithFilter(filesPath, pathFilter)

        if (includeSubdirectories) {
            filesPath.eachFileRecurse(FileType.DIRECTORIES) { Path subdirectory ->
                matchingFiles.addAll(getMatchingFilesWithFilter(subdirectory, pathFilter))
            }
        }

        if (sortFiles) {
            return matchingFiles.toSorted { Path a, Path b -> a.normalize() <=> b.normalize() }
        } else {
            return matchingFiles
        }
    }

    static List<Path> getMatchingFilesWithFilter(Path filesPath, DirectoryStream.Filter<Path> pathFilter) {
        List<Path> matchingFiles = [ ]

        // Note that the 'try with resources pattern does not work with this version of groovy (2.4.x). Will work in 2.5.x.
        // This means that we must have a finally block to close the resource
        DirectoryStream directoryStream = Files.newDirectoryStream(filesPath, pathFilter)
        try /*(DirectoryStream directoryStream = Files.newDirectoryStream(filesPath, pattern))*/ {
            for (Path path : directoryStream) {
                matchingFiles.add(path)
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

    static List<Path> getNonMatchingFiles(Path filesPath, boolean isRegexNotGlob, boolean matchFilenameOnly,
                                          boolean sortFiles = true, String... patterns) {
        boolean includeSubdirectories = false
        boolean directoryOnly = false
        return getNonMatchingFilesFull(filesPath, isRegexNotGlob, matchFilenameOnly, sortFiles, includeSubdirectories,
                directoryOnly, patterns)
    }

    // TODO We may want to unit test this method, however it is composed mostly of java.nio.file methods.
    // If we do decide to write unit tests, use the following as a guide:
    // https://stackoverflow.com/questions/47101232/mocking-directorystreampath-without-mocking-iterator-possible
    static List<Path> getNonMatchingFilesFull(Path filesPath, boolean isRegexNotGlob, boolean matchFilenameOnly,
                                           boolean sortFiles = true, boolean includeSubdirectories = false,
                                           boolean directoryOnly = false, String... patterns) {
        List<Path> nonMatchingFiles = [ ]
        DirectoryStream.Filter<Path> pathFilter = getNonMatchPathFilter(isRegexNotGlob, matchFilenameOnly,
                directoryOnly, patterns)

        nonMatchingFiles = getMatchingFilesWithFilter(filesPath, pathFilter)

        if (includeSubdirectories) {
            filesPath.eachFileRecurse(FileType.DIRECTORIES) { Path subdirectory ->
                nonMatchingFiles.addAll(getMatchingFilesWithFilter(subdirectory, pathFilter))
            }
        }

        if (sortFiles) {
            return nonMatchingFiles.toSorted { Path a, Path b -> a.normalize() <=> b.normalize() }
        } else {
            return nonMatchingFiles
        }
    }

}
