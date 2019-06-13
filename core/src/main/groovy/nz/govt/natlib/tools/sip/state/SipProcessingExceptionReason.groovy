package nz.govt.natlib.tools.sip.state

import groovy.transform.Canonical

@Canonical
class SipProcessingExceptionReason {

    SipProcessingExceptionReasonType reasonType
    List<String> details = [ ]
    Exception exception = null

    SipProcessingExceptionReason(SipProcessingExceptionReasonType reasonType, Exception exception, String... detail) {
        Objects.nonNull(reasonType)

        this.reasonType = reasonType
        if (detail != null) {
            details.addAll(detail)
        }
        this.exception = exception
    }

    String getSummary() {
        return reasonType.summary
    }

    String toString() {
        return reasonType.getDescription(details, exception)
    }
}
