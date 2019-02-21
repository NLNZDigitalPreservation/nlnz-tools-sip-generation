package nz.govt.natlib.tools.sip.pdf

import nz.govt.natlib.tools.sip.state.SipProcessingException

import java.nio.file.Path

/**
 * Validates the given PDF file.Note that the validation itself returns a SipProcessingException if the PDF is invalid
 * and <code>null</code> otherwise.
 */
interface PdfValidator {
    SipProcessingException validatePdf(Path path)
}
