package nz.govt.natlib.tools.sip.pdf

import nz.govt.natlib.tools.sip.state.SipProcessingException

import java.nio.file.Path

class PdfValidatorFactory {
    static PdfValidator getValidator(PdfValidatorType pdfValidatorType) {
        Objects.requireNonNull(pdfValidatorType)

        PdfValidator pdfValidator = (PdfValidator) pdfValidatorType.getImplementation().getDeclaredConstructor().newInstance()
        return pdfValidator
    }

    static SipProcessingException validate(Path pdfPath, PdfValidatorType pdfValidatorType) {
        PdfValidator pdfValidator = getValidator(pdfValidatorType)

        SipProcessingException sipProcessingException = null

        if (pdfValidator != null) {
            sipProcessingException = pdfValidator.validatePdf(pdfPath)
        }
        return sipProcessingException
    }
}
