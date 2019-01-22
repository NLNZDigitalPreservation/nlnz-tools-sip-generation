package nz.govt.natlib.tools.sip.state

import groovy.transform.Canonical

@Canonical
class SipProcessingFailureReason {
    SipProcessingFailureReasonType reasonType
    List<String> details = [ ]
    Exception exception = null

    SipProcessingFailureReason(SipProcessingFailureReasonType reasonType, Exception exception, String... detail) {
        Objects.nonNull(reasonType)

        this.reasonType = reasonType
        if (detail != null) {
            details.addAll(detail)
        }
        this.exception = exception
    }

    String toString() {
        return reasonType.getDescription(details, exception)
    }
}
