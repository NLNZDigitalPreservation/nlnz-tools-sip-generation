package nz.govt.natlib.tools.sip.pdf

import groovy.util.logging.Log4j2
import nz.govt.natlib.tools.sip.state.SipProcessingException
import nz.govt.natlib.tools.sip.state.SipProcessingExceptionReason
import nz.govt.natlib.tools.sip.state.SipProcessingExceptionReasonType
import org.apache.pdfbox.preflight.PreflightDocument
import org.apache.pdfbox.preflight.ValidationResult
import org.apache.pdfbox.preflight.exception.SyntaxValidationException
import org.apache.pdfbox.preflight.parser.PreflightParser

import java.nio.file.Path

/**
 * Validates the given PDF file. Currently Apache's PDFBox (https://github.com/apache/pdfbox) is used to provide that
 * validation. Note that the validation itself returns a SipProcessingException if the PDF is invalid and
 * <code>null</code> otherwise.
 */
@Log4j2
class PdfValidatorPdfBox implements PdfValidator {
    SipProcessingException validatePdf(Path path) {
        SipProcessingException sipProcessingException = null

        // Based on https://pdfbox.apache.org/1.8/cookbook/pdfavalidation.html
        // Note that this is compliance with ISO-19005 specification (aka PDF/A-1). Check Compliance with PDF/A-1b
        ValidationResult result = null

        PreflightParser parser = new PreflightParser(path.toFile())
        try {
            /* Parse the PDF file with PreflightParser that inherits from the NonSequentialParser.
             * Some additional controls are present to check a set of PDF/A requirements.
             * (Stream length consistency, EOL after some Keyword...)
             */
            parser.parse()

            /* Once the syntax validation is done, the parser can provide a PreflightDocument
             * (that inherits from PDDocument)
             * This document process the end of PDF/A validation.
             */
            PreflightDocument document = parser.getPreflightDocument()
            document.validate()

            // Get validation result
            result = document.getResult()
            document.close()

        } catch (SyntaxValidationException e) {
            /* the parse method can throw a SyntaxValidationException the PDF file can't be parsed.
             * In this case, the exception contains an instance of ValidationResult
             */
            result = e.getResult()
        }
        // go through list of errors and create SipProcessingExceptinoReason and add them to SipProcessingException
        if (result != null && result.errorsList.size() > 0) {
            StringBuilder reasonsList = new StringBuilder()
            boolean first = true
            result.errorsList.each { ValidationResult.ValidationError validationError ->
                if (!first) {
                    reasonsList.append(" | ")
                } else {
                    first = false
                }
                formatValidationError(reasonsList, validationError)
            }

            // add the errors to the SipProcessingException
            SipProcessingExceptionReason reason =
                    new SipProcessingExceptionReason(SipProcessingExceptionReasonType.INVALID_PDF, null,
                    path.toString(), reasonsList.toString())
            sipProcessingException = new SipProcessingException("Invalid PDF")
            sipProcessingException.addReason(reason)
        }

        return sipProcessingException
    }

    static void formatValidationError(StringBuilder stringBuilder, ValidationResult.ValidationError validationError) {
        stringBuilder.append("errorCode=")
        stringBuilder.append(validationError.errorCode)
        stringBuilder.append(", details=")
        stringBuilder.append(validationError.details)
        if (validationError.pageNumber != null) {
            stringBuilder.append(", pageNumber=")
            stringBuilder.append(validationError.pageNumber)
        }
        if (validationError.cause != null) {
            stringBuilder.append(", cause=")
            stringBuilder.append(validationError.cause)
        }
        // The validationError.throwable is an exception created by ValidationError itself to mark the point in the
        // stack where the ValidationError itself was created. This would be useful for debugging, but not so useful
        // for providing a String-based reason for the failure of the PDF, so we omit it for the moment (since
        // converting it to a String will just show 'Exception', and have little useful value (and may confuse the
        // reader).
        //if (validationError.throwable != null) {
        //    stringBuilder.append(", creationPoint=")
        //    stringBuilder.append(validationError.throwable)
        //}
    }
}
