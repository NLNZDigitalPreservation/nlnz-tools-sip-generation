package nz.govt.natlib.tools.sip

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

import static org.hamcrest.core.Is.is
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertThat

class SipTestHelper {
    public static final String SIP_TITLE = "test-title"

    static final Integer SIP_YEAR = 2018
    static final Integer SIP_MONTH = 11
    static final Integer SIP_DAY_OF_MONTH = 23

    static final String SIP_IE_ENTITY_TYPE = "test-ie-entity-type"
    static final String SIP_OBJECT_IDENTIFIER_TYPE = "test-object-identifier-type"
    static final String SIP_OBJECT_IDENTIFIER_VALUE = "test-object-identifier-value"

    static final String SIP_POLICY_ID = "test-policy-id"
    static final String SIP_PRESERVATION_TYPE = "test-preservation-type"
    static final String SIP_USAGE_TYPE = "test-usage-type"
    static final Boolean SIP_DIGITAL_ORIGINAL = true
    static final Integer SIP_REVISION_NUMBER = 1

    static final String SIP_FILE_MIME_TYPE = "test-file-mime-type"
    static final String SIP_FILE_BASE = "/path/to/file/test-file-original-name-"

    static final String SIP_FILE_ORIGINAL_PATH_BASE = "test-file-original-path-"
    static final String SIP_FILE_ORIGINAL_NAME_BASE = "test-file-original-name-"

    static final String SIP_FILE_LABEL_BASE = "test-file-label-"
    static final LocalDateTime SIP_FILE_CREATION_DATE = LocalDateTime.of(LocalDate.of(2018, 11, 23),
            LocalTime.of(1, 2, 3, 0))
    static final LocalDateTime SIP_FILE_MODIFICATION_DATE = LocalDateTime.of(LocalDate.of(2018, 11, 23),
            LocalTime.of(2, 3, 4, 0))
    static final Long SIP_FILE_SIZE_BYTES = 123456L

    static final String SIP_FILE_FIXITY_TYPE = "test-file-fixity-type"
    static final String SIP_FILE_FIXITY_VALUE = "test-file-fixity-value"

    // Actual files
    static final String RESOURCES_FOLDER = "nz/govt/natlib/tools/sip"

    static final String TEST_SIP_JSON_1_FILENAME = "test-sip-1.json"
    static final String TEST_SIP_JSON_ACHN_ACTUAL_FILENAME = "test-sip-auckland-city-harbour-news-2015-07-29-actual.json"
    static final String TEST_SIP_JSON_ACHN_EXPECTED_FILENAME = "test-sip-auckland-city-harbour-news-2015-07-29-expected.json"

    static final String TEST_SIP_XML_1_FILENAME = "test-sip-1.xml"
    static final String TEST_SIP_XML_ACHN_ACTUAL_FILENAME = "test-sip-auckland-city-harbour-news-2015-07-29-actual.xml"
    static final String TEST_SIP_XML_ACHN_EXPECTED_FILENAME = "test-sip-auckland-city-harbour-news-2015-07-29-actual.xml"

    static final String TEST_PDF_FILE_1_FILENAME = "test-pdf-1.pdf"
    static final String TEST_PDF_FILE_1_MD5_HASH = "b8b673eeaa076ff19501318a27f85e9c"
    static final Long TEST_PDF_FILE_1_SIZE = 11438L

    /**
     * Returns the contents of the file from the given filename and resources folder.
     * Make an attempt to open the file as a resource.
     * If that fails, try to open the file with the path resourcesFolder/filename. This should be relative
     * to the current working directory if the the resourcesFolder is a relative path.
     *
     * @param filename
     * @param resourcesFolder
     * @return
     */
    static String getTextFromResourceOrFile(String filename, String resourcesFolder = RESOURCES_FOLDER) {
        String resourcePath = "${resourcesFolder}/${filename}"
        String localPath = "src/test/resources/${resourcePath}"

        String text
        InputStream inputStream = SipTestHelper.class.getResourceAsStream(filename)
        if (inputStream == null) {
            File inputFile = new File(localPath)
            if (!inputFile.exists()) {
                inputFile = new File(new File(""), localPath)
            }
            text = inputFile.text
        } else {
            text = inputStream.text
        }
        return text
    }

    /**
     * Returns the file from the given filename and resources folder.
     * Make an attempt to open the file as a resource.
     * If that fails, try to open the file with the path resourcesFolder/filename. This should be relative
     * to the current working directory if the resourcesFolder is a relative path.
     *
     * @param filename
     * @param resourcesFolder
     * @return
     */
    static File getFileFromResourceOrFile(String filename, String resourcesFolder = RESOURCES_FOLDER) {
        String resourcePath = "${resourcesFolder}/${filename}"
        String localPath = "src/test/resources/${resourcePath}"

        URL resourceURL = SipTestHelper.class.getResource(filename)
        File resourceFile
        if (resourceURL != null) {
            resourceFile = new File(resourceURL.getFile())
        }
        if (resourceFile != null && (resourceFile.isFile() || resourceFile.isDirectory())) {
            return resourceFile
        } else {
            File returnFile = new File(localPath)
            return returnFile
        }
    }

    static Sip sipOneWithoutFiles() {
        Sip sip = new Sip()

        sip.title = SIP_TITLE

        sip.year = SIP_YEAR
        sip.month = SIP_MONTH
        sip.dayOfMonth = SIP_DAY_OF_MONTH

        sip.ieEntityType = SIP_IE_ENTITY_TYPE
        sip.objectIdentifierType = SIP_OBJECT_IDENTIFIER_TYPE
        sip.objectIdentifierValue = SIP_OBJECT_IDENTIFIER_VALUE
        sip.policyId = SIP_POLICY_ID

        sip.preservationType = SIP_PRESERVATION_TYPE
        sip.usageType = SIP_USAGE_TYPE
        sip.digitalOriginal = SIP_DIGITAL_ORIGINAL
        sip.revisionNumber = SIP_REVISION_NUMBER

        sip.fileWrappers = [ ]

        return sip
    }

    static Sip sipOne() {
        Sip sip = sipOneWithoutFiles()

        int fileIndex = 1
        while (fileIndex < 4) {
            Sip.FileWrapper fileWrapper = new Sip.FileWrapper()
            fileWrapper.mimeType = SIP_FILE_MIME_TYPE
            fileWrapper.file = new File("${SIP_FILE_BASE}${fileIndex}")
            fileWrapper.fileOriginalPath = "${SIP_FILE_ORIGINAL_PATH_BASE}${fileIndex}"
            fileWrapper.fileOriginalName = "${SIP_FILE_ORIGINAL_NAME_BASE}${fileIndex}"
            fileWrapper.label = "${SIP_FILE_LABEL_BASE}${fileIndex}"
            fileWrapper.creationDate = SIP_FILE_CREATION_DATE
            fileWrapper.modificationDate = SIP_FILE_MODIFICATION_DATE
            fileWrapper.fileSizeBytes = SIP_FILE_SIZE_BYTES
            fileWrapper.fixityType = SIP_FILE_FIXITY_TYPE
            fileWrapper.fixityValue = SIP_FILE_FIXITY_VALUE

            sip.fileWrappers.add(fileWrapper)
            fileIndex += 1
        }

        return sip
    }

    static void verifySipOne(Sip sip, boolean checkFileWrapperFiles = true) {
        assertThat("title", sip.title, is(SIP_TITLE))
        assertThat("year", sip.year, is(SIP_YEAR))
        assertThat("month", sip.month, is(SIP_MONTH))
        assertThat("dayOfMonth", sip.dayOfMonth, is(SIP_DAY_OF_MONTH))
        assertThat("ieEntityType", sip.ieEntityType, is(SIP_IE_ENTITY_TYPE))
        assertThat("objectIdentifierType", sip.objectIdentifierType, is(SIP_OBJECT_IDENTIFIER_TYPE))
        assertThat("objectIdentifierValue", sip.objectIdentifierValue, is(SIP_OBJECT_IDENTIFIER_VALUE))
        assertThat("policyId", sip.policyId, is(SIP_POLICY_ID))
        assertThat("preservationType", sip.preservationType, is(SIP_PRESERVATION_TYPE))
        assertThat("usageType", sip.usageType, is(SIP_USAGE_TYPE))
        assertThat("digitalOriginal", sip.digitalOriginal, is(SIP_DIGITAL_ORIGINAL))
        assertThat("revisionNumber", sip.revisionNumber, is(SIP_REVISION_NUMBER))

        assertThat("fileWrappers size", new Integer(sip.fileWrappers.size()), is(new Integer(3)))

        Sip.FileWrapper fileWrapper1 = sip.fileWrappers.get(0)
        assertThat("fileWrapper1.creationDate", fileWrapper1.creationDate, is(SIP_FILE_CREATION_DATE))
        if (checkFileWrapperFiles) {
            assertThat("fileWrapper1.file", fileWrapper1.file.getCanonicalPath(), is(new File((String) "${SIP_FILE_BASE}1").getCanonicalPath()))
        }
        assertThat("fileWrapper1.fileOriginalName", fileWrapper1.fileOriginalName, is((String) "${SIP_FILE_ORIGINAL_NAME_BASE}1"))
        assertThat("fileWrapper1.fileOriginalPath", fileWrapper1.fileOriginalPath, is((String) "${SIP_FILE_ORIGINAL_PATH_BASE}1"))
        assertThat("fileWrapper1.fileSizeBytes", fileWrapper1.fileSizeBytes, is(SIP_FILE_SIZE_BYTES))
        assertThat("fileWrapper1.fixityType", fileWrapper1.fixityType, is(SIP_FILE_FIXITY_TYPE))
        assertThat("fileWrapper1.fixityValue", fileWrapper1.fixityValue, is(SIP_FILE_FIXITY_VALUE))
        assertThat("fileWrapper1.label", fileWrapper1.label, is((String) "${SIP_FILE_LABEL_BASE}1"))
        assertThat("fileWrapper1.mimeType", fileWrapper1.mimeType, is(SIP_FILE_MIME_TYPE))
        assertThat("fileWrapper1.modificationDate", fileWrapper1.modificationDate, is(SIP_FILE_MODIFICATION_DATE))

        Sip.FileWrapper fileWrapper3 = sip.fileWrappers.get(2)
        assertThat("fileWrapper3.creationDate", fileWrapper3.creationDate, is(SIP_FILE_CREATION_DATE))
        if (checkFileWrapperFiles) {
            assertThat("fileWrapper3.file", fileWrapper3.file.getCanonicalPath(), is(new File((String) "${SIP_FILE_BASE}3").getCanonicalPath()))
        }
        assertThat("fileWrapper3.fileOriginalName", fileWrapper3.fileOriginalName, is((String) "${SIP_FILE_ORIGINAL_NAME_BASE}3"))
        assertThat("fileWrapper3.fileOriginalPath", fileWrapper3.fileOriginalPath, is((String) "${SIP_FILE_ORIGINAL_PATH_BASE}3"))
        assertThat("fileWrapper3.fileSizeBytes", fileWrapper3.fileSizeBytes, is(SIP_FILE_SIZE_BYTES))
        assertThat("fileWrapper3.fixityType", fileWrapper3.fixityType, is(SIP_FILE_FIXITY_TYPE))
        assertThat("fileWrapper3.fixityValue", fileWrapper3.fixityValue, is(SIP_FILE_FIXITY_VALUE))
        assertThat("fileWrapper3.label", fileWrapper3.label, is((String) "${SIP_FILE_LABEL_BASE}3"))
        assertThat("fileWrapper3.mimeType", fileWrapper3.mimeType, is(SIP_FILE_MIME_TYPE))
        assertThat("fileWrapper3.modificationDate", fileWrapper3.modificationDate, is(SIP_FILE_MODIFICATION_DATE))
    }

    static void verifyAucklandCityHarbourNews(Sip sip) {
        assertThat("title", sip.title, is("Auckland City Harbour News"))
        assertThat("year", sip.year, is(2015))
        assertThat("month", sip.month, is(7))
        assertThat("dayOfMonth", sip.dayOfMonth, is(29))
        assertThat("ieEntityType", sip.ieEntityType, is("NewspaperIE"))
        assertThat("objectIdentifierType", sip.objectIdentifierType, is("ALMAMMS"))
        assertThat("objectIdentifierValue", sip.objectIdentifierValue, is("9917982663502836"))
        assertThat("policyId", sip.policyId, is("200"))
        assertThat("preservationType", sip.preservationType, is("PRESERVATION_MASTER"))
        assertThat("usageType", sip.usageType, is("VIEW"))
        assertThat("digitalOriginal", sip.digitalOriginal, is(true))
        assertThat("revisionNumber", sip.revisionNumber, is(1))

        assertThat("fileWrappers size", new Integer(sip.fileWrappers.size()), is(new Integer(12)))

        // Note that FileWrapper files are null
        Sip.FileWrapper fileWrapper1 = sip.fileWrappers.get(0)
        assertThat("fileWrapper1.creationDate", fileWrapper1.creationDate, is(LocalDateTime.of(
                LocalDate.of(2015, 7, 29),
                LocalTime.of(0, 0, 0, 0))))
        assertThat("fileWrapper1.fileOriginalName", fileWrapper1.fileOriginalName, is("AHNED1-20150729-001.pdf"))
        assertThat("fileWrapper1.fileOriginalPath", fileWrapper1.fileOriginalPath, is("AHNED1-20150729-001.pdf"))
        assertThat("fileWrapper1.fileSizeBytes", fileWrapper1.fileSizeBytes, is(1036040L))
        assertThat("fileWrapper1.fixityType", fileWrapper1.fixityType, is("MD5"))
        assertThat("fileWrapper1.fixityValue", fileWrapper1.fixityValue, is("e9c6a70cd194cb38c11e56499beee282"))
        assertThat("fileWrapper1.label", fileWrapper1.label, is("001"))
        assertNull("fileWrapper1.mimeType", fileWrapper1.mimeType)
        assertThat("fileWrapper1.modificationDate", fileWrapper1.modificationDate, is(LocalDateTime.of(
                LocalDate.of(2015, 7, 29),
                LocalTime.of(0, 0, 0, 0))))

        Sip.FileWrapper fileWrapper12 = sip.fileWrappers.get(11)
        assertThat("fileWrapper12.creationDate", fileWrapper12.creationDate, is(LocalDateTime.of(
                LocalDate.of(2015, 7, 29),
                LocalTime.of(0, 0, 0, 0))))
        assertThat("fileWrapper12.fileOriginalName", fileWrapper12.fileOriginalName, is("AHNED1-20150729-012.pdf"))
        assertThat("fileWrapper12.fileOriginalPath", fileWrapper12.fileOriginalPath, is("AHNED1-20150729-012.pdf"))
        assertThat("fileWrapper12.fileSizeBytes", fileWrapper12.fileSizeBytes, is(2410038L))
        assertThat("fileWrapper12.fixityType", fileWrapper12.fixityType, is("MD5"))
        assertThat("fileWrapper12.fixityValue", fileWrapper12.fixityValue, is("1f2d11ca06e302b7c1295815a20c86cd"))
        assertThat("fileWrapper12.label", fileWrapper12.label, is("012"))
        assertNull("fileWrapper12.mimeType", fileWrapper12.mimeType)
        assertThat("fileWrapper12.modificationDate", fileWrapper12.modificationDate, is(LocalDateTime.of(
                LocalDate.of(2015, 7, 29),
                LocalTime.of(0, 0, 0, 0))))
    }
}
