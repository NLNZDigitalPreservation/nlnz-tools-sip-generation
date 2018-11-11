package nz.govt.natlib.tools.sip.generation

import org.junit.Test

/**
 * Tests the {@code series-sequential} scenario.
 */
class SeriesMultipleFilesTest {
    static final String RESOURCES_FOLDER = "sip-creation-tests/scenario-series-sequential"

    @Test
    void correctlyAssembleSipFromFiles() {
    }

    List<File> getMatchingFiles(List<File> files, String pattern) {
        return files.findAll { file ->
            file.getCanonicalPath() ==~ /${pattern}/
        }
    }

    List<File> getResourceFiles(String folderResourcePath) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader()
        URL url = loader.getResource(folderResourcePath)
        String path = url.getPath()

        List<File> files = Arrays.asList(new File(path).listFiles())
        files.each { file ->
            println("folderResourcePath=${folderResourcePath} found file=${file.getCanonicalPath()}")
        }

        return files
    }
}
