package nz.govt.natlib.tools.sip.state

import groovy.transform.Canonical
import org.apache.commons.lang3.StringUtils

@Canonical
class SipProcessingFailure {
    List<SipProcessingFailureReason> reasons = [ ]

    static SipProcessingFailure createWithReason(SipProcessingFailureReason sipProcessingFailureReason) {
        SipProcessingFailure sipProcessingFailure = new SipProcessingFailure()
        sipProcessingFailure.addReason(sipProcessingFailureReason)

        return sipProcessingFailure
    }

    void addReason(SipProcessingFailureReasonType failureReasonType, Exception exception, String... detail) {
        SipProcessingFailureReason sipProcessingFailureReason =
                new SipProcessingFailureReason(failureReasonType, exception, detail)

        this.addReason(sipProcessingFailureReason)
    }

    void addReason(SipProcessingFailureReason failureReason) {
        reasons.add(failureReason)
    }

    String toString() {
        return toString(0)
    }

    String toString(int offset) {
        String initialOffset = StringUtils.repeat(' ', offset)
        StringBuilder stringBuilder = new StringBuilder(initialOffset)
        stringBuilder.append(this.getClass().getName())
        stringBuilder.append(":")
        if (reasons.size() > 1) {
            this.reasons.each { SipProcessingFailureReason reason ->
                stringBuilder.append(System.lineSeparator())
                appendReason(stringBuilder, offset + 4, reason)
            }
        } else if (reasons.size() == 1) {
            appendReason(stringBuilder, 0, reasons.first())
        } else {
            stringBuilder.append(" NO REASONS GIVEN")
        }

        return stringBuilder.toString()
    }

    private void appendReason(StringBuilder stringBuilder, int offset, SipProcessingFailureReason reason) {
        String initialOffset = StringUtils.repeat(' ', offset)
        stringBuilder.append(initialOffset)
        stringBuilder.append(reason.toString())
    }
}
