package nz.govt.natlib.tools.sip.generation

import java.nio.file.Path

import static org.hamcrest.core.Is.is

import nz.govt.natlib.tools.sip.SipTestHelper
import org.junit.Test

import static org.junit.Assert.assertThat

/**
 * Tests the {@link MD5Generator}.
 *
 */
class MD5GeneratorTest {
    static final String STRING_TEXT_1 = "This is a test file with some text."
    static final String STRING_TEXT_1_MD5_HASH = "b228c7ecb9547359f81ed0bcfc74aaa7"
    static final String RESOURCES_FOLDER = "nz/govt/natlib/tools/sip/generation"
    static final String TSTPB1_20181123_001_PDF_FILENAME = "SAMPLE_PDF_1.pdf"
    static final String TSTPB1_20181123_001_PDF_MD5_HASH = "b8b673eeaa076ff19501318a27f85e9c"

    @Test
    void calculatesMD5FileHashCorrectly() {
        Path pdfFile = SipTestHelper.getFileFromResourceOrFile(TSTPB1_20181123_001_PDF_FILENAME, RESOURCES_FOLDER)
        String calculatedMD5Hash = MD5Generator.calculateMd5Hash(pdfFile)

        assertThat("${TSTPB1_20181123_001_PDF_FILENAME} MD5 hash matches", calculatedMD5Hash,
                is(TSTPB1_20181123_001_PDF_MD5_HASH))
    }

    @Test
    void calculatesMD5StringHashCorrectly() {
        String testString = STRING_TEXT_1
        String calculatedMD5Hash = MD5Generator.calculateMd5Hash(STRING_TEXT_1)

        assertThat("'${STRING_TEXT_1}' MD5 hash matches", calculatedMD5Hash,
                is(STRING_TEXT_1_MD5_HASH))
    }
}
