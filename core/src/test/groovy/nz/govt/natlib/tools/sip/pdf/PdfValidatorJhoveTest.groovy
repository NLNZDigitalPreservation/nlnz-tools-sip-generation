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

class PdfValidatorJhoveTest {
    static final String TEST_FILE_LOCATION = "src/test/resources/nz/govt/natlib/tools/sip/pdf/jhove/"

    static final String PDF_MINIMAL_VALID = "T00_000_minimal-valid.pdf"
    static final String PDF_ROTATED_90_VALID = "A3-portrait-dimensioned-rotated-90.pdf"

    static final String PDF_HEADER_INVALID_MAJOR_VERSION = "T01_001_header-invalid-major-version.pdf"
    static final String PDF_HEADER_INVALID_MAJOR_VERSION_REASON_1 =
            'The given file=src/test/resources/nz/govt/natlib/tools/sip/pdf/jhove/T01_001_header-invalid-major-version.pdf is an invalid PDF. Validation failure(s)=error=PDF not well formed, messages=No PDF header.'
    static final String PDF_HEADER_INVALID_MAJOR_VERSION_REASON_2 =
            'The given file=src/test/resources/nz/govt/natlib/tools/sip/pdf/jhove/T01_001_header-invalid-major-version.pdf is an invalid PDF. Validation failure(s)=error=PDF not valid, messages=No PDF header.'

    static final String PDF_DOCUMENT_CATALOG_INCORRECT_PAGES_REFERENCE = "T02-01_004_document-catalog-incorrect-pages-reference.pdf"
    static final String PDF_DOCUMENT_CATALOG_INCORRECT_PAGES_REFERENCE_REASON_1 =
            'The given file=src/test/resources/nz/govt/natlib/tools/sip/pdf/jhove/T02-01_004_document-catalog-incorrect-pages-reference.pdf is an invalid PDF. Validation failure(s)=error=PDF not well formed, messages=Document page tree not found.'
    static final String PDF_DOCUMENT_CATALOG_INCORRECT_PAGES_REFERENCE_REASON_2 =
            'The given file=src/test/resources/nz/govt/natlib/tools/sip/pdf/jhove/T02-01_004_document-catalog-incorrect-pages-reference.pdf is an invalid PDF. Validation failure(s)=error=PDF not valid, messages=Document page tree not found.'

    static final String PDF_PAGE_TREE_NON_EXISTING_OBJECT_AS_KID = "T02-02_004_page-tree-non-existing-object-as-kid.pdf"
    static final String PDF_PAGE_TREE_NON_EXISTING_OBJECT_AS_KID_REASON_1 =
            'The given file=src/test/resources/nz/govt/natlib/tools/sip/pdf/jhove/T02-02_004_page-tree-non-existing-object-as-kid.pdf is an invalid PDF. Validation failure(s)=error=PDF not valid, messages=Page tree node not found. | Page information is not displayed; to display remove param value of p from the config file.'

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
        File pdfFile = new File(TEST_FILE_LOCATION + PDF_MINIMAL_VALID)
        SipProcessingException sipProcessingException = underTest.validatePdf(pdfFile.toPath())

        assertNull("PDF validation did not produce an exception", sipProcessingException)
    }

    @Test
    void pdfA3PortraitRotated90IsValid() {
        File pdfFile = new File(TEST_FILE_LOCATION + PDF_ROTATED_90_VALID)
        SipProcessingException sipProcessingException = underTest.validatePdf(pdfFile.toPath())

        assertNull("PDF validation did not produce an exception", sipProcessingException)
    }

    @Test
    void pdfHeaderInvalidMajorVersionIsInvalidWithCorrectMessage() {
        File pdfFile = new File(TEST_FILE_LOCATION + PDF_HEADER_INVALID_MAJOR_VERSION)
        SipProcessingException sipProcessingException = underTest.validatePdf(pdfFile.toPath())

        assertNotNull("PDF validation produced an exception", sipProcessingException)

        assertThat("sipProcessingException has correct number of reasons",
                sipProcessingException.reasons.size(), is((Integer) 2))

        SipProcessingExceptionReason reason1 = sipProcessingException.reasons.first()
        assertThat("reason1 is INVALID_PDF", reason1.reasonType, is(SipProcessingExceptionReasonType.INVALID_PDF))
        assertThat("reason1 is '${PDF_HEADER_INVALID_MAJOR_VERSION_REASON_1}", reason1.toString(), is(PDF_HEADER_INVALID_MAJOR_VERSION_REASON_1))


        SipProcessingExceptionReason reason2 = sipProcessingException.reasons.get(1)
        assertThat("reason2 is INVALID_PDF", reason2.reasonType, is(SipProcessingExceptionReasonType.INVALID_PDF))
        assertThat("reason2 is '${PDF_HEADER_INVALID_MAJOR_VERSION_REASON_2}", reason2.toString(), is(PDF_HEADER_INVALID_MAJOR_VERSION_REASON_2))
    }

    @Test
    void pdfDocumentCatalogIncorrectPagesReferenceIsInvalidWithCorrectMessage() {
        File pdfFile = new File(TEST_FILE_LOCATION + PDF_DOCUMENT_CATALOG_INCORRECT_PAGES_REFERENCE)
        SipProcessingException sipProcessingException = underTest.validatePdf(pdfFile.toPath())

        assertNotNull("PDF validation produced an exception", sipProcessingException)

        assertThat("sipProcessingException has correct number of reasons",
                sipProcessingException.reasons.size(), is((Integer) 2))

        SipProcessingExceptionReason reason1 = sipProcessingException.reasons.first()
        assertThat("reason1 is INVALID_PDF", reason1.reasonType, is(SipProcessingExceptionReasonType.INVALID_PDF))
        assertThat("reason1 is '${PDF_DOCUMENT_CATALOG_INCORRECT_PAGES_REFERENCE_REASON_1}", reason1.toString(), is(PDF_DOCUMENT_CATALOG_INCORRECT_PAGES_REFERENCE_REASON_1))

        SipProcessingExceptionReason reason2 = sipProcessingException.reasons.get(1)
        assertThat("reason2 is INVALID_PDF", reason2.reasonType, is(SipProcessingExceptionReasonType.INVALID_PDF))
        assertThat("reason2 is '${PDF_DOCUMENT_CATALOG_INCORRECT_PAGES_REFERENCE_REASON_2}", reason2.toString(), is(PDF_DOCUMENT_CATALOG_INCORRECT_PAGES_REFERENCE_REASON_2))
    }

    @Test
    void pdfPageTreeNonExistingObjectAsKidIsInvalidWithCorrectMessage() {
        File pdfFile = new File(TEST_FILE_LOCATION + PDF_PAGE_TREE_NON_EXISTING_OBJECT_AS_KID)
        SipProcessingException sipProcessingException = underTest.validatePdf(pdfFile.toPath())

        assertNotNull("PDF validation produced an exception", sipProcessingException)

        assertThat("sipProcessingException has correct number of reasons",
                sipProcessingException.reasons.size(), is((Integer) 1))

        SipProcessingExceptionReason reason1 = sipProcessingException.reasons.first()
        assertThat("reason1 is INVALID_PDF", reason1.reasonType, is(SipProcessingExceptionReasonType.INVALID_PDF))
        assertThat("reason1 is '${PDF_PAGE_TREE_NON_EXISTING_OBJECT_AS_KID_REASON_1}", reason1.toString(), is(PDF_PAGE_TREE_NON_EXISTING_OBJECT_AS_KID_REASON_1))
    }
}
