package nz.govt.natlib.tools.sip.pdf

import nz.govt.natlib.tools.sip.state.SipProcessingException
import nz.govt.natlib.tools.sip.state.SipProcessingExceptionReason
import nz.govt.natlib.tools.sip.state.SipProcessingExceptionReasonType
import org.apache.commons.io.FilenameUtils
import org.junit.Before
import org.junit.Test

import java.nio.file.Path

import static org.hamcrest.core.Is.is
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertTrue

class PdfValidatorJhoveTest {
    static final String SPLIT_MARK = "SPLIT_MARK"
    static final String TEST_FILE_LOCATION = FilenameUtils.separatorsToSystem("src/test/resources/nz/govt/natlib/tools/sip/pdf/jhove/")
    static final Path TEST_FILE_LOCATION_PATH = Path.of(TEST_FILE_LOCATION)

    static final String PDF_MINIMAL_VALID = "T00_000_minimal-valid.pdf"
    static final String PDF_MINIMAL_VALID_FAIRFAX_SCENARIOS = "minimal-valid-used-for-fairfax-scenarios.pdf"
    static final String PDF_ROTATED_90_VALID = "A3-portrait-dimensioned-rotated-90.pdf"

    static final String PDF_HEADER_INVALID_MAJOR_VERSION = "T01_001_header-invalid-major-version.pdf"
    static final String PDF_HEADER_INVALID_MAJOR_VERSION_REASON_1_PATH = FilenameUtils.separatorsToSystem("src/test/resources/nz/govt/natlib/tools/sip/pdf/jhove/T01_001_header-invalid-major-version.pdf")
    static final String PDF_HEADER_INVALID_MAJOR_VERSION_REASON_1 =
            "The given file=${SPLIT_MARK}${PDF_HEADER_INVALID_MAJOR_VERSION_REASON_1_PATH} is an invalid PDF. Validation failure(s)=error=PDF not well formed, messages=No PDF header.".toString()
    static final String PDF_HEADER_INVALID_MAJOR_VERSION_REASON_2_PATH = FilenameUtils.separatorsToSystem("src/test/resources/nz/govt/natlib/tools/sip/pdf/jhove/T01_001_header-invalid-major-version.pdf")
    static final String PDF_HEADER_INVALID_MAJOR_VERSION_REASON_2 =
            "The given file=${SPLIT_MARK}${PDF_HEADER_INVALID_MAJOR_VERSION_REASON_2_PATH} is an invalid PDF. Validation failure(s)=error=PDF not valid, messages=No PDF header.".toString()

    static final String PDF_DOCUMENT_CATALOG_INCORRECT_PAGES_REFERENCE = "T02-01_004_document-catalog-incorrect-pages-reference.pdf"
    static final String PDF_DOCUMENT_CATALOG_INCORRECT_PAGES_REFERENCE_REASON_1_PATH = FilenameUtils.separatorsToSystem("src/test/resources/nz/govt/natlib/tools/sip/pdf/jhove/T02-01_004_document-catalog-incorrect-pages-reference.pdf")
    static final String PDF_DOCUMENT_CATALOG_INCORRECT_PAGES_REFERENCE_REASON_1 =
            "The given file=${SPLIT_MARK}${PDF_DOCUMENT_CATALOG_INCORRECT_PAGES_REFERENCE_REASON_1_PATH} is an invalid PDF. Validation failure(s)=error=PDF not well formed, messages=Document page tree not found.".toString()
    static final String PDF_DOCUMENT_CATALOG_INCORRECT_PAGES_REFERENCE_REASON_2_PATH = FilenameUtils.separatorsToSystem("src/test/resources/nz/govt/natlib/tools/sip/pdf/jhove/T02-01_004_document-catalog-incorrect-pages-reference.pdf").toString()
    static final String PDF_DOCUMENT_CATALOG_INCORRECT_PAGES_REFERENCE_REASON_2 =
            "The given file=${SPLIT_MARK}${PDF_DOCUMENT_CATALOG_INCORRECT_PAGES_REFERENCE_REASON_2_PATH} is an invalid PDF. Validation failure(s)=error=PDF not valid, messages=Document page tree not found.".toString()

    static final String PDF_PAGE_TREE_NON_EXISTING_OBJECT_AS_KID = "T02-02_004_page-tree-non-existing-object-as-kid.pdf"
    static final String PDF_PAGE_TREE_NON_EXISTING_OBJECT_AS_KID_REASON_1_PATH = FilenameUtils.separatorsToSystem("src/test/resources/nz/govt/natlib/tools/sip/pdf/jhove/T02-02_004_page-tree-non-existing-object-as-kid.pdf")
    static final String PDF_PAGE_TREE_NON_EXISTING_OBJECT_AS_KID_REASON_1 =
            "The given file=${SPLIT_MARK}${PDF_PAGE_TREE_NON_EXISTING_OBJECT_AS_KID_REASON_1_PATH} is an invalid PDF. Validation failure(s)=error=PDF not valid, messages=Page tree node not found. | Page information is not displayed; to display remove param value of p from the config file.".toString()

    PdfValidatorJhove underTest

    @Before
    void setup() {
        underTest = new PdfValidatorJhove()
    }

    @Test
    void pdfFactoryCorrectlyInstantiatesPdfValidatorJhove() {
        PdfValidator pdfValidator = PdfValidatorFactory.getValidator(PdfValidatorType.JHOVE_VALIDATOR)

        assertThat("PdfValidator instance is PdfValidatorJhove", pdfValidator.class, is(PdfValidatorJhove.class))
    }

    @Test
    void pdfMinimalValidIsValid() {
        Path pdfFile = TEST_FILE_LOCATION_PATH.resolve(PDF_MINIMAL_VALID)
        SipProcessingException sipProcessingException = underTest.validatePdf(pdfFile)

        assertNull("PDF validation for file=${pdfFile} did not produce an exception", sipProcessingException)
    }

    @Test
    void pdfMinimalValidFairfaxScenariosIsValid() {
        Path pdfFile = TEST_FILE_LOCATION_PATH.resolve(PDF_MINIMAL_VALID_FAIRFAX_SCENARIOS)
        SipProcessingException sipProcessingException = underTest.validatePdf(pdfFile)

        assertNull("PDF validation for file=${pdfFile} did not produce an exception", sipProcessingException)
    }

    @Test
    void pdfA3PortraitRotated90IsValid() {
        Path pdfFile =  TEST_FILE_LOCATION_PATH.resolve(PDF_ROTATED_90_VALID)
        SipProcessingException sipProcessingException = underTest.validatePdf(pdfFile)

        assertNull("PDF validation did not produce an exception", sipProcessingException)
    }

    @Test
    void pdfHeaderInvalidMajorVersionIsInvalidWithCorrectMessage() {
        Path pdfFile = TEST_FILE_LOCATION_PATH.resolve(PDF_HEADER_INVALID_MAJOR_VERSION)
        SipProcessingException sipProcessingException = underTest.validatePdf(pdfFile)

        assertNotNull("PDF validation produced an exception", sipProcessingException)

        assertThat("sipProcessingException has correct number of reasons",
                sipProcessingException.reasons.size(), is((Integer) 2))

        SipProcessingExceptionReason reason1 = sipProcessingException.reasons.first()
        assertThat("reason1 is INVALID_PDF", reason1.reasonType, is(SipProcessingExceptionReasonType.INVALID_PDF))
        checkReasonMatches(reason1.toString(), PDF_HEADER_INVALID_MAJOR_VERSION_REASON_1, SPLIT_MARK)

        SipProcessingExceptionReason reason2 = sipProcessingException.reasons.get(1)
        assertThat("reason2 is INVALID_PDF", reason2.reasonType, is(SipProcessingExceptionReasonType.INVALID_PDF))
        checkReasonMatches(reason2.toString(), PDF_HEADER_INVALID_MAJOR_VERSION_REASON_2, SPLIT_MARK)
    }

    @Test
    void pdfDocumentCatalogIncorrectPagesReferenceIsInvalidWithCorrectMessage() {
        Path pdfFile = TEST_FILE_LOCATION_PATH.resolve(PDF_DOCUMENT_CATALOG_INCORRECT_PAGES_REFERENCE)
        SipProcessingException sipProcessingException = underTest.validatePdf(pdfFile)

        assertNotNull("PDF validation produced an exception", sipProcessingException)

        assertThat("sipProcessingException has correct number of reasons",
                sipProcessingException.reasons.size(), is((Integer) 2))

        SipProcessingExceptionReason reason1 = sipProcessingException.reasons.first()
        assertThat("reason1 is INVALID_PDF", reason1.reasonType, is(SipProcessingExceptionReasonType.INVALID_PDF))
        checkReasonMatches(reason1.toString(), PDF_DOCUMENT_CATALOG_INCORRECT_PAGES_REFERENCE_REASON_1, SPLIT_MARK)

        SipProcessingExceptionReason reason2 = sipProcessingException.reasons.get(1)
        assertThat("reason2 is INVALID_PDF", reason2.reasonType, is(SipProcessingExceptionReasonType.INVALID_PDF))
        checkReasonMatches(reason2.toString(), PDF_DOCUMENT_CATALOG_INCORRECT_PAGES_REFERENCE_REASON_2, SPLIT_MARK)
    }

    @Test
    void pdfPageTreeNonExistingObjectAsKidIsInvalidWithCorrectMessage() {
        Path pdfFile = TEST_FILE_LOCATION_PATH.resolve(PDF_PAGE_TREE_NON_EXISTING_OBJECT_AS_KID)
        SipProcessingException sipProcessingException = underTest.validatePdf(pdfFile)

        assertNotNull("PDF validation produced an exception", sipProcessingException)

        assertThat("sipProcessingException has correct number of reasons",
                sipProcessingException.reasons.size(), is((Integer) 1))

        SipProcessingExceptionReason reason1 = sipProcessingException.reasons.first()
        assertThat("reason1 is INVALID_PDF", reason1.reasonType, is(SipProcessingExceptionReasonType.INVALID_PDF))
        checkReasonMatches(reason1.toString(), PDF_PAGE_TREE_NON_EXISTING_OBJECT_AS_KID_REASON_1, SPLIT_MARK)
    }

    void checkReasonMatches(String actualReason, String expectedReason, String splitMark) {
        List<String> splits = expectedReason.split(splitMark)
        String prefix = splits.first()
        String suffix = splits.last()
        assertTrue("Reason starts with expected value", actualReason.startsWith(prefix))
        assertTrue("Reason ends with expected value", actualReason.endsWith(suffix))
    }

}
