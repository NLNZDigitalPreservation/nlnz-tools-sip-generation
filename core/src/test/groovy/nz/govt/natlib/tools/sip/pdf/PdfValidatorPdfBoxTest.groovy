package nz.govt.natlib.tools.sip.pdf

import nz.govt.natlib.tools.sip.state.SipProcessingException
import nz.govt.natlib.tools.sip.state.SipProcessingExceptionReason
import nz.govt.natlib.tools.sip.state.SipProcessingExceptionReasonType
import org.junit.Before
import org.junit.Test

import static org.hamcrest.core.Is.is
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertThat

/**
 * Files are borrowed from Apache PDFBox (https://github.com/apache/pdfbox).
 */
class PdfValidatorPdfBoxTest {
    static final String TEST_FILE_LOCATION = "src/test/resources/nz/govt/natlib/tools/sip/pdf/pdfbox/"

    static final String PDF_MIDDLE_CONTROL_CHAR = "PDFAMetaDataValidationTestMiddleControlChar.pdf"
    static final String PDF_MIDDLE_CONTROL_CHAR_REASON =
            'The given file=src/test/resources/nz/govt/natlib/tools/sip/pdf/pdfbox/PDFAMetaDataValidationTestMiddleControlChar.pdf is an invalid PDF. Validation failure(s)=errorCode=7.2, details=Error on MetaData, Author present in the document catalog dictionary doesn\'t match with XMP information.'

    static final String PDF_MIDDLE_NULL = "PDFAMetaDataValidationTestMiddleNul.pdf"
    static final String PDF_MIDDLE_NULL_REASON =
            'The given file=src/test/resources/nz/govt/natlib/tools/sip/pdf/pdfbox/PDFAMetaDataValidationTestMiddleNul.pdf is an invalid PDF. Validation failure(s)=errorCode=7.2, details=Error on MetaData, Author present in the document catalog dictionary doesn\'t match with XMP information.'

    static final String PDF_TRAILING_CONTROL_CHAR = "PDFAMetaDataValidationTestTrailingControlChar.pdf"
    static final String PDF_TRAILING_CONTROL_CHAR_REASON =
            'The given file=src/test/resources/nz/govt/natlib/tools/sip/pdf/pdfbox/PDFAMetaDataValidationTestTrailingControlChar.pdf is an invalid PDF. Validation failure(s)=errorCode=7.2, details=Error on MetaData, Author present in the document catalog dictionary doesn\'t match with XMP information.'

    static final String PDF_TRAILING_NULL = "PDFAMetaDataValidationTestTrailingNul.pdf"

    static final String PDF_TRAILING_SPACES = "PDFAMetaDataValidationTestTrailingSpaces.pdf"
    static final String PDF_TRAILING_SPACES_REASON =
            'The given file=src/test/resources/nz/govt/natlib/tools/sip/pdf/pdfbox/PDFAMetaDataValidationTestTrailingSpaces.pdf is an invalid PDF. Validation failure(s)=errorCode=7.2, details=Error on MetaData, Author present in the document catalog dictionary doesn\'t match with XMP information.'

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
        assertThat("reason1 is '${PDF_MIDDLE_CONTROL_CHAR_REASON}", reason1.toString(), is(PDF_MIDDLE_CONTROL_CHAR_REASON))
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
        assertThat("reason1 is '${PDF_MIDDLE_NULL_REASON}", reason1.toString(), is(PDF_MIDDLE_NULL_REASON))
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
        assertThat("reason1 is '${PDF_TRAILING_CONTROL_CHAR_REASON}", reason1.toString(), is(PDF_TRAILING_CONTROL_CHAR_REASON))
    }

    @Test
    void trailingNullIsValid() {
        File pdfFile = new File(TEST_FILE_LOCATION + PDF_TRAILING_NULL)
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
        assertThat("reason1 is '${PDF_TRAILING_SPACES_REASON}", reason1.toString(), is(PDF_TRAILING_SPACES_REASON))
    }
}
