package nz.govt.natlib.tools.sip

import static org.hamcrest.core.Is.is
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue

import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

/**
 * Tests {@link SipFileWrapperFactory}.
 * Note that {@code SipFileWrapperFactory} uses a {@link java.nio.Files}, which is a final class and very
 * difficult and/or convoluted to mock. For this reason, it's far easier to:
 * <ol>
 *     <li>Test the class with an existing file.</li>
 *     <li>Assume things like file creation and modification time are retrieved correctly by {@code Files}</li>
 * </ol>
 */
class SipFileWrapperFactoryTest {

    File testFile

    Sip.FileWrapper testFileWrapper

    @Before
    void setup() {
        testFile = SipTestHelper.getFileFromResourceOrFile(SipTestHelper.TEST_PDF_FILE_1_FILENAME)
    }

    @Test
    void correctlyPopulatesFileWrapperFromFile() {
        testFileWrapper = SipFileWrapperFactory.generate(testFile, false, true)

        assertThat("testFileWrapper.mimeType=${testFileWrapper.mimeType}",
                testFileWrapper.mimeType, is("application/pdf"))
        assertThat("testFileWrapper.file=${testFileWrapper.file}", testFileWrapper.file, is(testFile))

        // Support Windows testing where the file path has backslashes.
        String adjustedFileOriginalPath = testFileWrapper.fileOriginalPath.replace("\\", "/")
        assertTrue("testFileWrapper.fileOriginalPath=${testFileWrapper.fileOriginalPath}",
                adjustedFileOriginalPath.endsWith("nz/govt/natlib/tools/sip"))
        assertThat("testFileWrapper.fileOriginalName=${testFileWrapper.fileOriginalName}",
                testFileWrapper.fileOriginalName, is(SipTestHelper.TEST_PDF_FILE_1_FILENAME))
        assertNull("testFileWrapper.label=${testFileWrapper.label}", testFileWrapper.label)
        // Since the creation date and time of the class is based on when the test file was created in the file system
        // we simply check that it's a non-null instance of LocalDateTime
        assertTrue("testFileWrapper.creationDate=${testFileWrapper.creationDate}",
                testFileWrapper.creationDate in LocalDateTime)
        assertTrue("testFileWrapper.modificationDate=${testFileWrapper.modificationDate}",
                testFileWrapper.modificationDate in LocalDateTime)
        assertThat("testFileWrapper.fileSizeBytes=${testFileWrapper.fileSizeBytes}",
                testFileWrapper.fileSizeBytes, is(SipTestHelper.TEST_PDF_FILE_1_SIZE))
        assertThat("testFileWrapper.fixityType=${testFileWrapper.fixityType}",
                testFileWrapper.fixityType, is("MD5"))
        assertThat("testFileWrapper.fixityValue=${testFileWrapper.fixityValue}",
                testFileWrapper.fixityValue, is(SipTestHelper.TEST_PDF_FILE_1_MD5_HASH))
    }

    @Test
    void correctlyPopulatesFileWrapperFromFileWithFilenameOnly() {
        testFileWrapper = SipFileWrapperFactory.generate(testFile, true, true)

        assertThat("testFileWrapper.mimeType=${testFileWrapper.mimeType}",
                testFileWrapper.mimeType, is("application/pdf"))
        assertThat("testFileWrapper.file=${testFileWrapper.file}", testFileWrapper.file, is(testFile))
        assertThat("testFileWrapper.fileOriginalPath=${testFileWrapper.fileOriginalPath}",
                testFileWrapper.fileOriginalPath, is(SipTestHelper.TEST_PDF_FILE_1_FILENAME))
        assertThat("testFileWrapper.fileOriginalName=${testFileWrapper.fileOriginalName}",
                testFileWrapper.fileOriginalName, is(SipTestHelper.TEST_PDF_FILE_1_FILENAME))
        assertNull("testFileWrapper.label=${testFileWrapper.label}", testFileWrapper.label)
        // Since the creation date and time of the class is based on when the test file was created in the file system
        // we simply check that it's a non-null instance of LocalDateTime
        assertTrue("testFileWrapper.creationDate=${testFileWrapper.creationDate}",
                testFileWrapper.creationDate in LocalDateTime)
        assertTrue("testFileWrapper.modificationDate=${testFileWrapper.modificationDate}",
                testFileWrapper.modificationDate in LocalDateTime)
        assertThat("testFileWrapper.fileSizeBytes=${testFileWrapper.fileSizeBytes}",
                testFileWrapper.fileSizeBytes, is(SipTestHelper.TEST_PDF_FILE_1_SIZE))
        assertThat("testFileWrapper.fixityType=${testFileWrapper.fixityType}",
                testFileWrapper.fixityType, is("MD5"))
        assertThat("testFileWrapper.fixityValue=${testFileWrapper.fixityValue}",
                testFileWrapper.fixityValue, is(SipTestHelper.TEST_PDF_FILE_1_MD5_HASH))
    }

    @Test
    void correctlyPopulatesFileWrapperFromFileWithoutMD5Hash() {
        testFileWrapper = SipFileWrapperFactory.generate(testFile, false, false)

        assertThat("testFileWrapper.mimeType=${testFileWrapper.mimeType}",
                testFileWrapper.mimeType, is("application/pdf"))
        assertThat("testFileWrapper.file=${testFileWrapper.file}", testFileWrapper.file, is(testFile))

        // Support Windows testing where the file path has backslashes.
        String adjustedFileOriginalPath = testFileWrapper.fileOriginalPath.replace("\\", "/")
        assertTrue("testFileWrapper.fileOriginalPath=${testFileWrapper.fileOriginalPath}",
                adjustedFileOriginalPath.endsWith("nz/govt/natlib/tools/sip"))
        assertThat("testFileWrapper.fileOriginalName=${testFileWrapper.fileOriginalName}",
                testFileWrapper.fileOriginalName, is(SipTestHelper.TEST_PDF_FILE_1_FILENAME))
        assertNull("testFileWrapper.label=${testFileWrapper.label}", testFileWrapper.label)
        // Since the creation date and time of the class is based on when the test file was created in the file system
        // we simply check that it's a non-null instance of LocalDateTime
        assertTrue("testFileWrapper.creationDate=${testFileWrapper.creationDate}",
                testFileWrapper.creationDate in LocalDateTime)
        assertTrue("testFileWrapper.modificationDate=${testFileWrapper.modificationDate}",
                testFileWrapper.modificationDate in LocalDateTime)
        assertThat("testFileWrapper.fileSizeBytes=${testFileWrapper.fileSizeBytes}",
                testFileWrapper.fileSizeBytes, is(SipTestHelper.TEST_PDF_FILE_1_SIZE))
        assertNull("testFileWrapper.fixityType=${testFileWrapper.fixityType}", testFileWrapper.fixityType)
        assertNull("testFileWrapper.fixityValue=${testFileWrapper.fixityValue}", testFileWrapper.fixityValue)
    }
}
