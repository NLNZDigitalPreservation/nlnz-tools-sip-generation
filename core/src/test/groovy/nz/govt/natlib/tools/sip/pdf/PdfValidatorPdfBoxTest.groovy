package nz.govt.natlib.tools.sip.pdf

import nz.govt.natlib.tools.sip.state.SipProcessingException
import nz.govt.natlib.tools.sip.state.SipProcessingExceptionReason
import nz.govt.natlib.tools.sip.state.SipProcessingExceptionReasonType
import org.apache.commons.io.FilenameUtils
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

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
        File pdfFile = new File(TEST_FILE_LOCATION + PDF_MIDDLE_CONTROL_CHAR)
        SipProcessingException sipProcessingException = underTest.validatePdf(pdfFile.toPath())

        assertNotNull("PDF validation produced an exception", sipProcessingException)

        assertThat("sipProcessingException has correct number of reasons",
                sipProcessingException.reasons.size(), is((Integer) 1))

        SipProcessingExceptionReason reason1 = sipProcessingException.reasons.first()
        assertThat("reason1 is INVALID_PDF", reason1.reasonType, is(SipProcessingExceptionReasonType.INVALID_PDF))
        checkReasonMatches(reason1.toString(), PDF_MIDDLE_CONTROL_CHAR_REASON, SPLIT_MARK)
    }

    @Test
    void middleNullIsInvalidWithCorrectMessage() {
        File pdfFile = new File(TEST_FILE_LOCATION + PDF_MIDDLE_NULL)
        SipProcessingException sipProcessingException = underTest.validatePdf(pdfFile.toPath())

        assertNotNull("PDF validation produced an exception", sipProcessingException)

        assertThat("sipProcessingException has correct number of reasons",
                sipProcessingException.reasons.size(), is((Integer) 1))

        SipProcessingExceptionReason reason1 = sipProcessingException.reasons.first()
        assertThat("reason1 is INVALID_PDF", reason1.reasonType, is(SipProcessingExceptionReasonType.INVALID_PDF))
        checkReasonMatches(reason1.toString(), PDF_MIDDLE_NULL_REASON, SPLIT_MARK)
    }

    @Test
    void trailingControlCharIsInvalidWithCorrectMessage() {
        File pdfFile = new File(TEST_FILE_LOCATION + PDF_TRAILING_CONTROL_CHAR)
        SipProcessingException sipProcessingException = underTest.validatePdf(pdfFile.toPath())

        assertNotNull("PDF validation produced an exception", sipProcessingException)

        assertThat("sipProcessingException has correct number of reasons",
                sipProcessingException.reasons.size(), is((Integer) 1))

        SipProcessingExceptionReason reason1 = sipProcessingException.reasons.first()
        assertThat("reason1 is INVALID_PDF", reason1.reasonType, is(SipProcessingExceptionReasonType.INVALID_PDF))
        checkReasonMatches(reason1.toString(), PDF_TRAILING_CONTROL_CHAR_REASON, SPLIT_MARK)
    }

    @Test
    void trailingNullIsValid() {
        File pdfFile = new File(TEST_FILE_LOCATION + PDF_TRAILING_NULL)
        SipProcessingException sipProcessingException = underTest.validatePdf(pdfFile.toPath())

        assertNull("PDF validation did not produce an exception", sipProcessingException)
    }

    @Test
    @Ignore // This PDF, produced by LibreOffice and rotated by pdftk, is considered invalid by PdfBox, so we ignore
            // this test.
            // TODO Maybe we need a test of 'PDF is renderable', rather than valid/invalid.
    void a3PortraitRotated90IsValid() {
        File pdfFile = new File(TEST_FILE_LOCATION + PDF_ROTATED_90_VALID)
        SipProcessingException sipProcessingException = underTest.validatePdf(pdfFile.toPath())

        assertNull("PDF validation did not produce an exception", sipProcessingException)
    }

    @Test
    void trailingSpacesIsInvalidWithCorrectMessage() {
        File pdfFile = new File(TEST_FILE_LOCATION + PDF_TRAILING_SPACES)
        SipProcessingException sipProcessingException = underTest.validatePdf(pdfFile.toPath())

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
        assertTrue("Reason starts with expected value", actualReason.startsWith(prefix))
        assertTrue("Reason ends with expected value", actualReason.endsWith(suffix))
    }
}
