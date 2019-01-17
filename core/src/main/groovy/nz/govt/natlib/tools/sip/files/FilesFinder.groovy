package nz.govt.natlib.tools.sip.files

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
    static DirectoryStream.Filter<Path> getPathFilter(boolean isRegexNotGlob, boolean matchFilenameOnly, String... patterns) {
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
                    if (matchFilenameOnly) {
                        pathMatcher.matches(entry.getFileName())
                    } else {
                        pathMatcher.matches(entry.normalize())
                    }
                }
            }
        }
    }

    // TODO We may want to unit test this method, however it is composed mostly of java.nio.file methods.
    // If we do decide to write unit tests, use the following as a guide:
    // https://stackoverflow.com/questions/47101232/mocking-directorystreampath-without-mocking-iterator-possible
    static List<File> getMatchingFiles(Path filesPath, boolean isRegexNotGlob, boolean matchFilenameOnly, String... patterns) {
        List<File> matchingFiles = [ ]
        DirectoryStream.Filter<Path> pathFilter = getPathFilter(isRegexNotGlob, matchFilenameOnly, patterns)

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
