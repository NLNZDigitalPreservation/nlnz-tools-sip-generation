package nz.govt.natlib.tools.sip.utils

import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang.SystemUtils
import org.apache.groovy.util.SystemUtil
import org.junit.After
import org.junit.Before
import org.junit.Ignore

import java.util.regex.Pattern

import static org.hamcrest.core.Is.is
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue

import org.junit.Test

class FileUtilsTest {
    static final File ZERO_SEGMENT_FILE = new File("")
    static final File ZERO_SEGMENT_FILE_SLASH = new File(FilenameUtils.separatorsToSystem("/"))
    static final File ONE_SEGMENT_FILE = new File(FilenameUtils.separatorsToSystem("filename.txt"))
    static final File ONE_SEGMENT_FILE_SLASH = new File(FilenameUtils.separatorsToSystem("/filename.txt"))
    static final File TWO_SEGMENT_FILE = new File(FilenameUtils.separatorsToSystem("parent1/filename.txt"))
    static final File TWO_SEGMENT_FILE_SLASH = new File(FilenameUtils.separatorsToSystem("/parent1/filename.txt"))
    static final File THREE_SEGMENT_FILE = new File(FilenameUtils.separatorsToSystem("parent2/parent1/filename.txt"))
    static final File THREE_SEGMENT_FILE_SLASH = new File(FilenameUtils.separatorsToSystem("/parent2/parent1/filename.txt"))
    static final File FOUR_SEGMENT_FILE = new File(FilenameUtils.separatorsToSystem("parent3/parent2/parent1/filename.txt"))
    static final File FOUR_SEGMENT_FILE_SLASH = new File(FilenameUtils.separatorsToSystem("/parent3/parent2/parent1/filename.txt"))
    static final File FIVE_SEGMENT_FILE = new File(FilenameUtils.separatorsToSystem("parent4/parent3/parent2/parent1/filename.txt"))
    static final File FIVE_SEGMENT_FILE_SLASH = new File(FilenameUtils.separatorsToSystem("/parent4/parent3/parent2/parent1/filename.txt"))

    static final String SAMPLE_TEXT_FILE_NAME = "sample-text-file.txt"
    static final String SAMPLE_TEXT_FILE_PACKAGE_PATH = FilenameUtils.separatorsToSystem("nz/govt/natlib/tools/sip/utils")
    static final String SAMPLE_TEXT_FILE_CONTENTS = "This is a sample text file."

    List<File> filesToDelete

    @Before
    void setup() {
        filesToDelete = [ ]
    }

    @After
    void teardown() {
        filesToDelete.each { File file ->
            if (file.exists()) {
                if (file.isDirectory()) {
                    org.apache.commons.io.FileUtils.deleteDirectory(file)
                } else {
                    file.delete()
                }
            }
        }
    }

    @Test
    void convertsFilenamesProperly() {
        checkAndConvertFilename("/this/is/a/path", "_this_is_a_path")
        checkAndConvertFilename("\\this\\is\\a\\path", "_this_is_a_path")
        checkAndConvertFilename("C:\\this\\is\\a\\path", "C__this_is_a_path")
        checkAndConvertFilename("dollars/\$are\$/removed", "dollars_-are-_removed")
        checkAndConvertFilename("/asterisks*have-/dashes*instead", "_asterisks-have-_dashes-instead")
        checkAndConvertFilename("no more spaces either ", "no-more-spaces-either-")
    }

    static void checkAndConvertFilename(String filePath, String expectedConversion) {
        assertThat("Correctly coverts=${filePath} to ${expectedConversion}",
                FileUtils.fileNameAsSafeString(filePath), is(expectedConversion))
    }

    @Test
    void verifyThatFilePathAsSafeStringWorksForDirectoryOnly() {
        int totalSegments = 1
        
        String expected = "filename.txt"
        String osPrefix = getOsPrefix()
        // Can't really test ZERO_SEGMENT_FILE, since it will automatically have its parent because of how File is constructed.
        // For ZERO_SEGMENT_FILE_SLASH, it's treated as the root folder, which means it is a blank
        assertThat("Full=${ZERO_SEGMENT_FILE_SLASH} with totalSegments=${totalSegments} is='${osPrefix}'",
                FileUtils.filePathAsSafeString(ZERO_SEGMENT_FILE_SLASH, totalSegments), is("${osPrefix}".toString()))
        assertThat("Full=${ONE_SEGMENT_FILE} with totalSegments=${totalSegments} is=filename.txt",
                FileUtils.filePathAsSafeString(ONE_SEGMENT_FILE, totalSegments), is("filename.txt"))
        assertThat("Full=${ONE_SEGMENT_FILE_SLASH} with totalSegments=${totalSegments} is=${expected}",
                FileUtils.filePathAsSafeString(ONE_SEGMENT_FILE_SLASH, totalSegments), is(expected))
        assertThat("Full=${TWO_SEGMENT_FILE} with totalSegments=${totalSegments} is=${expected}",
                FileUtils.filePathAsSafeString(TWO_SEGMENT_FILE, totalSegments), is(expected))
        assertThat("Full=${TWO_SEGMENT_FILE_SLASH} with totalSegments=${totalSegments} is=${expected}",
                FileUtils.filePathAsSafeString(TWO_SEGMENT_FILE_SLASH, totalSegments), is(expected))
        assertThat("Full=${THREE_SEGMENT_FILE} with totalSegments=${totalSegments} is=${expected}",
                FileUtils.filePathAsSafeString(THREE_SEGMENT_FILE, totalSegments), is(expected))
        assertThat("Full=${THREE_SEGMENT_FILE_SLASH} with totalSegments=${totalSegments} is=${expected}",
                FileUtils.filePathAsSafeString(THREE_SEGMENT_FILE_SLASH, totalSegments), is(expected))
        assertThat("Full=${FOUR_SEGMENT_FILE} with totalSegments=${totalSegments} is=${expected}",
                FileUtils.filePathAsSafeString(FOUR_SEGMENT_FILE, totalSegments), is(expected))
        assertThat("Full=${FOUR_SEGMENT_FILE_SLASH} with totalSegments=${totalSegments} is=${expected}",
                FileUtils.filePathAsSafeString(FOUR_SEGMENT_FILE_SLASH, totalSegments), is(expected))
        assertThat("Full=${FIVE_SEGMENT_FILE} with totalSegments=${totalSegments} is=${expected}",
                FileUtils.filePathAsSafeString(FIVE_SEGMENT_FILE, totalSegments), is(expected))
        assertThat("Full=${FIVE_SEGMENT_FILE_SLASH} with totalSegments=${totalSegments} is=${expected}",
                FileUtils.filePathAsSafeString(FIVE_SEGMENT_FILE_SLASH, totalSegments), is(expected))
    }

    @Test
    void verifyThatFilePathAsSafeStringWorksForDirectoryAndOneParent() {
        int totalSegments = 2

        String expected = "parent1_filename.txt"
        String osPrefix = getOsPrefix()
        assertTrue("Full=${ONE_SEGMENT_FILE} with totalSegments=${totalSegments} ends with=filename.txt, actual=${FileUtils.filePathAsSafeString(ONE_SEGMENT_FILE, totalSegments)}",
                FileUtils.filePathAsSafeString(ONE_SEGMENT_FILE, totalSegments).endsWith("filename.txt"))
        assertThat("Full=${ONE_SEGMENT_FILE_SLASH} with totalSegments=${totalSegments} is=_filename.txt",
                FileUtils.filePathAsSafeString(ONE_SEGMENT_FILE_SLASH, totalSegments), is("${osPrefix}_filename.txt".toString()))
        assertThat("Full=${TWO_SEGMENT_FILE} with totalSegments=${totalSegments} is=${expected}",
                FileUtils.filePathAsSafeString(TWO_SEGMENT_FILE, totalSegments), is(expected))
        assertThat("Full=${TWO_SEGMENT_FILE_SLASH} with totalSegments=${totalSegments} is=${expected}",
                FileUtils.filePathAsSafeString(TWO_SEGMENT_FILE_SLASH, totalSegments), is(expected))
        assertThat("Full=${THREE_SEGMENT_FILE} with totalSegments=${totalSegments} is=${expected}",
                FileUtils.filePathAsSafeString(THREE_SEGMENT_FILE, totalSegments), is(expected))
        assertThat("Full=${THREE_SEGMENT_FILE_SLASH} with totalSegments=${totalSegments} is=${expected}",
                FileUtils.filePathAsSafeString(THREE_SEGMENT_FILE_SLASH, totalSegments), is(expected))
        assertThat("Full=${FOUR_SEGMENT_FILE} with totalSegments=${totalSegments} is=${expected}",
                FileUtils.filePathAsSafeString(FOUR_SEGMENT_FILE, totalSegments), is(expected))
        assertThat("Full=${FOUR_SEGMENT_FILE_SLASH} with totalSegments=${totalSegments} is=${expected}",
                FileUtils.filePathAsSafeString(FOUR_SEGMENT_FILE_SLASH, totalSegments), is(expected))
        assertThat("Full=${FIVE_SEGMENT_FILE} with totalSegments=${totalSegments} is=${expected}",
                FileUtils.filePathAsSafeString(FIVE_SEGMENT_FILE, totalSegments), is(expected))
        assertThat("Full=${FIVE_SEGMENT_FILE_SLASH} with totalSegments=${totalSegments} is=${expected}",
                FileUtils.filePathAsSafeString(FIVE_SEGMENT_FILE_SLASH, totalSegments), is(expected))
    }

    @Test
    void verifyThatFilePathAsSafeStringWorksForDirectoryAndTwoParents() {
        int totalSegments = 3

        String expected = "parent2_parent1_filename.txt"
        String osPrefix = getOsPrefix()
        assertTrue("Full=${ONE_SEGMENT_FILE} with totalSegments=${totalSegments} ends with=filename.txt, actual=${FileUtils.filePathAsSafeString(ONE_SEGMENT_FILE, totalSegments)}",
                FileUtils.filePathAsSafeString(ONE_SEGMENT_FILE, totalSegments).endsWith("filename.txt"))
        assertThat("Full=${ONE_SEGMENT_FILE_SLASH} with totalSegments=${totalSegments} is=${osPrefix}_filename.txt",
                FileUtils.filePathAsSafeString(ONE_SEGMENT_FILE_SLASH, totalSegments), is("${osPrefix}_filename.txt".toString()))
        assertTrue("Full=${TWO_SEGMENT_FILE} with totalSegments=${totalSegments} ends with=parent1_filename.txt, actual=${FileUtils.filePathAsSafeString(TWO_SEGMENT_FILE, totalSegments)}",
                FileUtils.filePathAsSafeString(TWO_SEGMENT_FILE, totalSegments).endsWith("parent1_filename.txt"))
        assertThat("Full=${TWO_SEGMENT_FILE_SLASH} with totalSegments=${totalSegments} is=${osPrefix}_parent1_filename.txt",
                FileUtils.filePathAsSafeString(TWO_SEGMENT_FILE_SLASH, totalSegments), is("${osPrefix}_parent1_filename.txt".toString()))
        assertThat("Full=${THREE_SEGMENT_FILE} with totalSegments=${totalSegments} is=${expected}",
                FileUtils.filePathAsSafeString(THREE_SEGMENT_FILE, totalSegments), is(expected))
        assertThat("Full=${THREE_SEGMENT_FILE_SLASH} with totalSegments=${totalSegments} is=${expected}",
                FileUtils.filePathAsSafeString(THREE_SEGMENT_FILE_SLASH, totalSegments), is(expected))
        assertThat("Full=${FOUR_SEGMENT_FILE} with totalSegments=${totalSegments} is=${expected}",
                FileUtils.filePathAsSafeString(FOUR_SEGMENT_FILE, totalSegments), is(expected))
        assertThat("Full=${FOUR_SEGMENT_FILE_SLASH} with totalSegments=${totalSegments} is=${expected}",
                FileUtils.filePathAsSafeString(FOUR_SEGMENT_FILE_SLASH, totalSegments), is(expected))
        assertThat("Full=${FIVE_SEGMENT_FILE} with totalSegments=${totalSegments} is=${expected}",
                FileUtils.filePathAsSafeString(FIVE_SEGMENT_FILE, totalSegments), is(expected))
        assertThat("Full=${FIVE_SEGMENT_FILE_SLASH} with totalSegments=${totalSegments} is=${expected}",
                FileUtils.filePathAsSafeString(FIVE_SEGMENT_FILE_SLASH, totalSegments), is(expected))
    }

    @Test
    void verifyThatFilePathAsSafeStringWorksForDirectoryAndThreeParents() {
        int totalSegments = 4
        
        String expected = "parent3_parent2_parent1_filename.txt"
        String osPrefix = getOsPrefix()
        assertTrue("Full=${ONE_SEGMENT_FILE} with totalSegments=${totalSegments} ends with=filename.txt, actual=${FileUtils.filePathAsSafeString(ONE_SEGMENT_FILE, totalSegments)}",
                FileUtils.filePathAsSafeString(ONE_SEGMENT_FILE, totalSegments).endsWith("filename.txt"))
        assertThat("Full=${ONE_SEGMENT_FILE_SLASH} with totalSegments=${totalSegments} is=${osPrefix}_filename.txt",
                FileUtils.filePathAsSafeString(ONE_SEGMENT_FILE_SLASH, totalSegments), is("${osPrefix}_filename.txt".toString()))
        assertTrue("Full=${TWO_SEGMENT_FILE} with totalSegments=${totalSegments} ends with=parent1_filename.txt, actual=${FileUtils.filePathAsSafeString(TWO_SEGMENT_FILE, totalSegments)}",
                FileUtils.filePathAsSafeString(TWO_SEGMENT_FILE, totalSegments).endsWith("parent1_filename.txt"))
        assertThat("Full=${TWO_SEGMENT_FILE_SLASH} with totalSegments=${totalSegments} is=${osPrefix}_parent1_filename.txt",
                FileUtils.filePathAsSafeString(TWO_SEGMENT_FILE_SLASH, totalSegments), is("${osPrefix}_parent1_filename.txt".toString()))
        assertTrue("Full=${THREE_SEGMENT_FILE} with totalSegments=${totalSegments} ends with=parent2_parent1_filename.txt, actual=${FileUtils.filePathAsSafeString(THREE_SEGMENT_FILE, totalSegments)}",
                FileUtils.filePathAsSafeString(THREE_SEGMENT_FILE, totalSegments).endsWith("parent2_parent1_filename.txt"))
        assertThat("Full=${THREE_SEGMENT_FILE_SLASH} with totalSegments=${totalSegments} is=${osPrefix}_parent2_parent1_filename.txt}",
                FileUtils.filePathAsSafeString(THREE_SEGMENT_FILE_SLASH, totalSegments), is("${osPrefix}_parent2_parent1_filename.txt".toString()))
        assertThat("Full=${FOUR_SEGMENT_FILE} with totalSegments=${totalSegments} is=${expected}",
                FileUtils.filePathAsSafeString(FOUR_SEGMENT_FILE, totalSegments), is(expected))
        assertThat("Full=${FOUR_SEGMENT_FILE_SLASH} with totalSegments=${totalSegments} is=${expected}",
                FileUtils.filePathAsSafeString(FOUR_SEGMENT_FILE_SLASH, totalSegments), is(expected))
        assertThat("Full=${FIVE_SEGMENT_FILE} with totalSegments=${totalSegments} is=${expected}",
                FileUtils.filePathAsSafeString(FIVE_SEGMENT_FILE, totalSegments), is(expected))
        assertThat("Full=${FIVE_SEGMENT_FILE_SLASH} with totalSegments=${totalSegments} is=${expected}",
                FileUtils.filePathAsSafeString(FIVE_SEGMENT_FILE_SLASH, totalSegments), is(expected))
    }

    @Test
    void verifyThatFilePathAsSafeStringWorksForFullPath() {
        int totalSegments = 0

        String osPrefix = getOsPrefix()
        assertTrue("Full=${ONE_SEGMENT_FILE} with totalSegments=${totalSegments} ends with=filename.txt, actual=${FileUtils.filePathAsSafeString(ONE_SEGMENT_FILE, totalSegments)}",
                FileUtils.filePathAsSafeString(ONE_SEGMENT_FILE, totalSegments).endsWith("filename.txt"))
        assertThat("Full=${ONE_SEGMENT_FILE_SLASH} with totalSegments=${totalSegments} is=${osPrefix}_filename.txt",
                FileUtils.filePathAsSafeString(ONE_SEGMENT_FILE_SLASH, totalSegments), is("${osPrefix}_filename.txt".toString()))
        assertTrue("Full=${TWO_SEGMENT_FILE} with totalSegments=${totalSegments} ends with=parent1_filename.txt, actual=${FileUtils.filePathAsSafeString(TWO_SEGMENT_FILE, totalSegments)}",
                FileUtils.filePathAsSafeString(TWO_SEGMENT_FILE, totalSegments).endsWith("parent1_filename.txt"))
        assertThat("Full=${TWO_SEGMENT_FILE_SLASH} with totalSegments=${totalSegments} is=${osPrefix}_parent1_filename.txt",
                FileUtils.filePathAsSafeString(TWO_SEGMENT_FILE_SLASH, totalSegments), is("${osPrefix}_parent1_filename.txt".toString()))
        assertTrue("Full=${THREE_SEGMENT_FILE} with totalSegments=${totalSegments} ends with=parent2_parent1_filename.txt, actual=${FileUtils.filePathAsSafeString(THREE_SEGMENT_FILE, totalSegments)}",
                FileUtils.filePathAsSafeString(THREE_SEGMENT_FILE, totalSegments).endsWith("parent2_parent1_filename.txt"))
        assertThat("Full=${THREE_SEGMENT_FILE_SLASH} with totalSegments=${totalSegments} is=${osPrefix}_parent2_parent1_filename.txt}",
                FileUtils.filePathAsSafeString(THREE_SEGMENT_FILE_SLASH, totalSegments), is("${osPrefix}_parent2_parent1_filename.txt".toString()))
        assertTrue("Full=${FOUR_SEGMENT_FILE} with totalSegments=${totalSegments} ends with=parent3_parent2_parent1_filename.txt, actual=${FileUtils.filePathAsSafeString(FOUR_SEGMENT_FILE, totalSegments)}",
                FileUtils.filePathAsSafeString(FOUR_SEGMENT_FILE, totalSegments).endsWith("parent3_parent2_parent1_filename.txt"))
        assertThat("Full=${FOUR_SEGMENT_FILE_SLASH} with totalSegments=${totalSegments} is=${osPrefix}_parent3_parent2_parent1_filename.txt}",
                FileUtils.filePathAsSafeString(FOUR_SEGMENT_FILE_SLASH, totalSegments), is("${osPrefix}_parent3_parent2_parent1_filename.txt".toString()))
        assertThat("Full=${FIVE_SEGMENT_FILE_SLASH} with totalSegments=${totalSegments} is=${osPrefix}_parent4_parent3_parent2_parent1_filename.txt}",
                FileUtils.filePathAsSafeString(FIVE_SEGMENT_FILE_SLASH, totalSegments), is("${osPrefix}_parent4_parent3_parent2_parent1_filename.txt".toString()))
    }

    @Test
    void verifyThatFilePathAsSafeStringWorksForNoOption() {
        int totalSegments = 0

        String osPrefix = getOsPrefix()
        assertTrue("Full=${ONE_SEGMENT_FILE} with totalSegments=${totalSegments} ends with=filename.txt, actual=${FileUtils.filePathAsSafeString(ONE_SEGMENT_FILE, totalSegments)}",
                FileUtils.filePathAsSafeString(ONE_SEGMENT_FILE, totalSegments).endsWith("filename.txt"))
        assertThat("Full=${ONE_SEGMENT_FILE_SLASH} with totalSegments=${totalSegments} is=${osPrefix}_filename.txt",
                FileUtils.filePathAsSafeString(ONE_SEGMENT_FILE_SLASH, totalSegments), is("${osPrefix}_filename.txt".toString()))
        assertTrue("Full=${TWO_SEGMENT_FILE} with totalSegments=${totalSegments} ends with=parent1_filename.txt, actual=${FileUtils.filePathAsSafeString(TWO_SEGMENT_FILE, totalSegments)}",
                FileUtils.filePathAsSafeString(TWO_SEGMENT_FILE, totalSegments).endsWith("parent1_filename.txt"))
        assertThat("Full=${TWO_SEGMENT_FILE_SLASH} with totalSegments=${totalSegments} is=${osPrefix}_parent1_filename.txt",
                FileUtils.filePathAsSafeString(TWO_SEGMENT_FILE_SLASH, totalSegments), is("${osPrefix}_parent1_filename.txt".toString()))
        assertTrue("Full=${THREE_SEGMENT_FILE} with totalSegments=${totalSegments} ends with=parent2_parent1_filename.txt, actual=${FileUtils.filePathAsSafeString(THREE_SEGMENT_FILE, totalSegments)}",
                FileUtils.filePathAsSafeString(THREE_SEGMENT_FILE, totalSegments).endsWith("parent2_parent1_filename.txt"))
        assertThat("Full=${THREE_SEGMENT_FILE_SLASH} with totalSegments=${totalSegments} is=${osPrefix}_parent2_parent1_filename.txt}",
                FileUtils.filePathAsSafeString(THREE_SEGMENT_FILE_SLASH, totalSegments), is("${osPrefix}_parent2_parent1_filename.txt".toString()))
        assertTrue("Full=${FOUR_SEGMENT_FILE} with totalSegments=${totalSegments} ends with=parent3_parent2_parent1_filename.txt, actual=${FileUtils.filePathAsSafeString(FOUR_SEGMENT_FILE, totalSegments)}",
                FileUtils.filePathAsSafeString(FOUR_SEGMENT_FILE, totalSegments).endsWith("parent3_parent2_parent1_filename.txt"))
        assertThat("Full=${FOUR_SEGMENT_FILE_SLASH} with totalSegments=${totalSegments} is=${osPrefix}_parent3_parent2_parent1_filename.txt}",
                FileUtils.filePathAsSafeString(FOUR_SEGMENT_FILE_SLASH, totalSegments), is("${osPrefix}_parent3_parent2_parent1_filename.txt".toString()))
        assertTrue("Full=${FIVE_SEGMENT_FILE} with totalSegments=${totalSegments} ends with=parent4_parent3_parent2_parent1_filename.txt, actual=${FileUtils.filePathAsSafeString(FIVE_SEGMENT_FILE, totalSegments)}",
                FileUtils.filePathAsSafeString(FIVE_SEGMENT_FILE, totalSegments).endsWith("parent4_parent3_parent2_parent1_filename.txt"))
        assertThat("Full=${FIVE_SEGMENT_FILE_SLASH} with totalSegments=${totalSegments} is=${osPrefix}_parent4_parent3_parent2_parent1_filename.txt}",
                FileUtils.filePathAsSafeString(FIVE_SEGMENT_FILE_SLASH, totalSegments), is("${osPrefix}_parent4_parent3_parent2_parent1_filename.txt".toString()))
    }

    @Test
    void canWritesSampleRootResourceFileToTempFolder() {
        File tempFile = FileUtils.writeResourceToTemporaryDirectory(SAMPLE_TEXT_FILE_NAME,
                "FileUtilsTest-unit-test_", "", SAMPLE_TEXT_FILE_NAME, null)
        filesToDelete.add(tempFile)

        String expectedContents = SAMPLE_TEXT_FILE_CONTENTS

        assertTrue("tempFile=${tempFile.canonicalPath} exists", tempFile.exists())

        String contents = tempFile.text
        assertThat("Temp file contents=${contents} matches=${expectedContents}", contents, is(expectedContents))

        tempFile.parentFile.delete()
    }

    @Test
    void canWritesSamplePackageResourceFileToTempFolder() {
        File tempFile = FileUtils.writeResourceToTemporaryDirectory(SAMPLE_TEXT_FILE_NAME,
                "FileUtilsTest-unit-test_", SAMPLE_TEXT_FILE_PACKAGE_PATH,
                SAMPLE_TEXT_FILE_NAME, null)
        filesToDelete.add(tempFile)

        String expectedContents = SAMPLE_TEXT_FILE_CONTENTS

        assertTrue("tempFile=${tempFile.canonicalPath} exists", tempFile.exists())

        String contents = tempFile.text
        assertThat("Temp file contents=${contents} matches=${expectedContents}", contents, is(expectedContents))
    }

    static String getOsPrefix() {
        String osPrefix
        if (SystemUtils.IS_OS_WINDOWS) {
            File testFile = new File("abc.txt")
            List<String> splitPath = testFile.getCanonicalPath().split(Pattern.quote(File.separator))
            osPrefix = splitPath.first().replace(":", "_")
        } else {
            osPrefix = ""
        }

        return osPrefix
    }
}
