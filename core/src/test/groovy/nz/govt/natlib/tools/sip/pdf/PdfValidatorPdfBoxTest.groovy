package nz.govt.natlib.tools.sip.pdf

import nz.govt.natlib.tools.sip.state.SipProcessingException
import nz.govt.natlib.tools.sip.state.SipProcessingExceptionReason
import nz.govt.natlib.tools.sip.state.SipProcessingExceptionReasonType
import org.apache.commons.io.FilenameUtils
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import java.nio.file.Path

import static org.hamcrest.core.Is.is
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue

/**
 * Files are borrowed from Apache PDFBox (https://github.com/apache/pdfbox).
 */
class PdfValidatorPdfBoxTest {
    static final String SPLIT_MARK = "SPLIT_MARK"
    static final String TEST_FILE_LOCATION = FilenameUtils.separatorsToSystem("src/test/resources/nz/govt/natlib/tools/sip/pdf/pdfbox/")
    static final Path TEST_FILE_LOCATION_PATH = Path.of(TEST_FILE_LOCATION)

    static final String PDF_MIDDLE_CONTROL_CHAR = "PDFAMetaDataValidationTestMiddleControlChar.pdf"
    static final String PDF_MIDDLE_CONTROL_CHAR_REASON_PATH = FilenameUtils.separatorsToSystem("src/test/resources/nz/govt/natlib/tools/sip/pdf/pdfbox/PDFAMetaDataValidationTestMiddleControlChar.pdf")
    static final String PDF_MIDDLE_CONTROL_CHAR_REASON =
            "The given file=${SPLIT_MARK}${PDF_MIDDLE_CONTROL_CHAR_REASON_PATH} is an invalid PDF. Validation failure(s)=errorCode=7.2, details=Error on MetaData, Author present in the document catalog dictionary doesn\'t match with XMP information."

    static final String PDF_MIDDLE_NULL = "PDFAMetaDataValidationTestMiddleNul.pdf"
    static final String PDF_MIDDLE_NULL_REASON_PATH = FilenameUtils.separatorsToSystem("src/test/resources/nz/govt/natlib/tools/sip/pdf/pdfbox/PDFAMetaDataValidationTestMiddleNul.pdf")
    static final String PDF_MIDDLE_NULL_REASON =
            "The given file=${SPLIT_MARK}${PDF_MIDDLE_NULL_REASON_PATH} is an invalid PDF. Validation failure(s)=errorCode=7.2, details=Error on MetaData, Author present in the document catalog dictionary doesn\'t match with XMP information.".toString()

    static final String PDF_TRAILING_CONTROL_CHAR = "PDFAMetaDataValidationTestTrailingControlChar.pdf"
    static final String PDF_TRAILING_CONTROL_CHAR_REASON_PATH = FilenameUtils.separatorsToSystem("src/test/resources/nz/govt/natlib/tools/sip/pdf/pdfbox/PDFAMetaDataValidationTestTrailingControlChar.pdf")
    static final String PDF_TRAILING_CONTROL_CHAR_REASON =
            "The given file=${SPLIT_MARK}${PDF_TRAILING_CONTROL_CHAR_REASON_PATH} is an invalid PDF. Validation failure(s)=errorCode=7.2, details=Error on MetaData, Author present in the document catalog dictionary doesn\'t match with XMP information.".toString()

    static final String PDF_TRAILING_NULL = "PDFAMetaDataValidationTestTrailingNul.pdf"
    static final String PDF_ROTATED_90_VALID = "A3-portrait-dimensioned-rotated-90.pdf"

    static final String PDF_TRAILING_SPACES = "PDFAMetaDataValidationTestTrailingSpaces.pdf"
    static final String PDF_TRAILING_SPACES_REASON_PATH = FilenameUtils.separatorsToSystem("src/test/resources/nz/govt/natlib/tools/sip/pdf/pdfbox/PDFAMetaDataValidationTestTrailingSpaces.pdf")
    static final String PDF_TRAILING_SPACES_REASON =
            "The given file=${SPLIT_MARK}${PDF_TRAILING_SPACES_REASON_PATH} is an invalid PDF. Validation failure(s)=errorCode=7.2, details=Error on MetaData, Author present in the document catalog dictionary doesn\'t match with XMP information.".toString()

    static final String PDF_MINIMAL_VALID_FAIRFAX_SCENARIOS = "minimal-valid-used-for-fairfax-scenarios.pdf"

    PdfValidatorPdfBox underTest

    @Before
    void setup() {
        underTest = new PdfValidatorPdfBox()
    }

    @Test
    void pdfFactoryCorrectlyInstantiatesPdfValidatorPdfBox() {
        PdfValidator pdfValidator = PdfValidatorFactory.getValidator(PdfValidatorType.PDF_BOX_VALIDATOR)

        assertThat("PdfValidator instance is PdfValidatorPdfBox", pdfValidator.class, is(PdfValidatorPdfBox.class))
    }

    @Test
    void middleControlCharIsInvalidWithCorrectMessage() {
        Path pdfFile = TEST_FILE_LOCATION_PATH.resolve(PDF_MIDDLE_CONTROL_CHAR)
        SipProcessingException sipProcessingException = underTest.validatePdf(pdfFile)

        assertNotNull("PDF validation produced an exception", sipProcessingException)

        assertThat("sipProcessingException has correct number of reasons",
                sipProcessingException.reasons.size(), is((Integer) 1))

        SipProcessingExceptionReason reason1 = sipProcessingException.reasons.first()
        assertThat("reason1 is INVALID_PDF", reason1.reasonType, is(SipProcessingExceptionReasonType.INVALID_PDF))
        checkReasonMatches(reason1.toString(), PDF_MIDDLE_CONTROL_CHAR_REASON, SPLIT_MARK)
    }

    @Test
    void middleNullIsInvalidWithCorrectMessage() {
        Path pdfFile = TEST_FILE_LOCATION_PATH.resolve(PDF_MIDDLE_NULL)
        SipProcessingException sipProcessingException = underTest.validatePdf(pdfFile)

        assertNotNull("PDF validation produced an exception", sipProcessingException)

        assertThat("sipProcessingException has correct number of reasons",
                sipProcessingException.reasons.size(), is((Integer) 1))

        SipProcessingExceptionReason reason1 = sipProcessingException.reasons.first()
        assertThat("reason1 is INVALID_PDF", reason1.reasonType, is(SipProcessingExceptionReasonType.INVALID_PDF))
        checkReasonMatches(reason1.toString(), PDF_MIDDLE_NULL_REASON, SPLIT_MARK)
    }

    @Test
    void trailingControlCharIsInvalidWithCorrectMessage() {
        Path pdfFile = TEST_FILE_LOCATION_PATH.resolve(PDF_TRAILING_CONTROL_CHAR)
        SipProcessingException sipProcessingException = underTest.validatePdf(pdfFile)

        assertNotNull("PDF validation produced an exception", sipProcessingException)

        assertThat("sipProcessingException has correct number of reasons",
                sipProcessingException.reasons.size(), is((Integer) 1))

        SipProcessingExceptionReason reason1 = sipProcessingException.reasons.first()
        assertThat("reason1 is INVALID_PDF", reason1.reasonType, is(SipProcessingExceptionReasonType.INVALID_PDF))
        checkReasonMatches(reason1.toString(), PDF_TRAILING_CONTROL_CHAR_REASON, SPLIT_MARK)
    }

    @Test
    void trailingNullIsValid() {
        Path pdfFile = TEST_FILE_LOCATION_PATH.resolve(PDF_TRAILING_NULL)
        SipProcessingException sipProcessingException = underTest.validatePdf(pdfFile)

        assertNull("PDF validation for file=${pdfFile} did not produce an exception", sipProcessingException)
    }

    @Test
    void pdfMinimalValidFairfaxScenariosIsInvalid() {
        Path pdfFile = TEST_FILE_LOCATION_PATH.resolve(PDF_MINIMAL_VALID_FAIRFAX_SCENARIOS)
        SipProcessingException sipProcessingException = underTest.validatePdf(pdfFile)

        assertNotNull("PDF validation produced an exception", sipProcessingException)

        // This pdf does not consistently error between Linux and Windows, so we just verify that it is treated as
        // invalid, even though under Jhove it does validate.
        assertThat("sipProcessingException has correct number of reasons",
                sipProcessingException.reasons.size(), is((Integer) 1))
    }

    @Test
    @Ignore // This PDF, produced by LibreOffice and rotated by pdftk, is considered invalid by PdfBox, so we ignore
            // this test.
            // TODO Maybe we need a test of 'PDF is renderable', rather than valid/invalid.
    void a3PortraitRotated90IsValid() {
        Path pdfFile = TEST_FILE_LOCATION_PATH.resolve(PDF_ROTATED_90_VALID)
        SipProcessingException sipProcessingException = underTest.validatePdf(pdfFile)

        assertNull("PDF validation did not produce an exception", sipProcessingException)
    }

    @Test
    void trailingSpacesIsInvalidWithCorrectMessage() {
        Path pdfFile = TEST_FILE_LOCATION_PATH.resolve(PDF_TRAILING_SPACES)
        SipProcessingException sipProcessingException = underTest.validatePdf(pdfFile)

        assertNotNull("PDF validation produced an exception", sipProcessingException)

        assertThat("sipProcessingException has correct number of reasons",
                sipProcessingException.reasons.size(), is((Integer) 1))

        SipProcessingExceptionReason reason1 = sipProcessingException.reasons.first()
        assertThat("reason1 is INVALID_PDF", reason1.reasonType, is(SipProcessingExceptionReasonType.INVALID_PDF))
        checkReasonMatches(reason1.toString(), PDF_TRAILING_SPACES_REASON, SPLIT_MARK)
    }

    void checkReasonMatches(String actualReason, String expectedReason, String splitMark) {
        List<String> splits = expectedReason.split(splitMark)
        String prefix = splits.first()
        String suffix = splits.last()
        assertTrue("Reason starts with expected value actual=${actualReason} expected prefix=${prefix}", actualReason.startsWith(prefix))
        assertTrue("Reason ends with expected value actual=${actualReason} expected suffix=${suffix}", actualReason.endsWith(suffix))
    }
}
