package nz.govt.natlib.tools.sip.pdf

import edu.harvard.hul.ois.jhove.JhoveBase
import edu.harvard.hul.ois.jhove.Message
import edu.harvard.hul.ois.jhove.RepInfo
import edu.harvard.hul.ois.jhove.module.PdfModule
import nz.govt.natlib.tools.sip.state.SipProcessingException
import nz.govt.natlib.tools.sip.state.SipProcessingExceptionReason
import nz.govt.natlib.tools.sip.state.SipProcessingExceptionReasonType

import java.nio.file.Path

/**
 * This is based on the Jhove unit tests found at https://github.com/openpreserve/jhove
 * jhove-modules/src/test/java/edu/harvard/hul/ois/jhove/module/pdf
 */
class PdfValidatorJhove implements PdfValidator {
    SipProcessingException validatePdf(Path path) {
        SipProcessingException sipProcessingException = null

        PdfModule pdfModule = new PdfModule()
        JhoveBase jhoveBase = new JhoveBase()

        pdfModule.setBase(jhoveBase)
        RepInfo repInfo = new RepInfo(path.toFile().getName())

        RandomAccessFile randomAccessFile = null
        try {
            randomAccessFile = new RandomAccessFile(path.toFile(), "r")
            pdfModule.parse(randomAccessFile, repInfo)

            sipProcessingException = validateRepInfo(path, repInfo)
        } catch (FileNotFoundException e) {
            sipProcessingException = new SipProcessingException("Cannot find file=" + path.toString(), e)
        } catch (IOException e) {
            sipProcessingException = new SipProcessingException("Exception processing file=" + path.toString(), e)
        } finally {
            try {
                if (randomAccessFile != null) {
                    randomAccessFile.close()
                }
            } catch (IOException e) {
                // ignore this
            }
        }
        return sipProcessingException
    }

    SipProcessingException validateRepInfo(Path path, RepInfo repInfo) {
        SipProcessingException sipProcessingException = null
        List<SipProcessingExceptionReason> reasons = [ ]

        boolean invalidPdf = false
        if (!repInfo.getWellFormed()) {
            invalidPdf = true
            SipProcessingExceptionReason reason =
                    new SipProcessingExceptionReason(SipProcessingExceptionReasonType.INVALID_PDF, null,
                            path.toString(), formatError("PDF not well formed", repInfo))
            reasons.add(reason)
        }
        if (!repInfo.getValid()) {
            invalidPdf = true
            SipProcessingExceptionReason reason =
                    new SipProcessingExceptionReason(SipProcessingExceptionReasonType.INVALID_PDF, null,
                            path.toString(), formatError("PDF not valid", repInfo))
            reasons.add(reason)
        }

        if (invalidPdf) {
            sipProcessingException = new SipProcessingException("Invalid PDF")
            reasons.each { SipProcessingExceptionReason sipProcessingExceptionReason ->
                sipProcessingException.addReason(sipProcessingExceptionReason)
            }
        }
        return sipProcessingException
    }

    static String formatError(String initialMessage, RepInfo repInfo) {
        StringBuilder stringBuilder = new StringBuilder()
        stringBuilder.append("error=")
        stringBuilder.append(initialMessage)
        if (repInfo.getMessage().size() > 0) {
            boolean firstMessage = true
            stringBuilder.append(", messages=")
            for (Message message : repInfo.getMessage()) {
                if (firstMessage) {
                    firstMessage = false
                } else {
                    stringBuilder.append(" | ")
                }
                stringBuilder.append(message.getMessage())
            }
        }
        return stringBuilder.toString()
    }
}
