package nz.govt.natlib.tools.sip.state

import groovy.transform.Canonical
import org.apache.commons.lang3.StringUtils

@Canonical
class SipProcessingState {

    boolean complete = false
    List<SipProcessingFailure> failures = [ ]

    boolean hasFailures() {
        return failures.size() > 0
    }

    boolean isSuccessful() {
        return complete && failures.size() == 0
    }

    void addFailure(SipProcessingFailure sipProcessingFailure) {
        this.failures.add(sipProcessingFailure)
    }

    String toString() {
        return toString(0)
    }

    String toString(int offset) {
        String initialOffset = StringUtils.repeat(' ', offset)
        StringBuilder stringBuilder = new StringBuilder(initialOffset)
        stringBuilder.append(this.getClass().getName())
        stringBuilder.append(": ")
        stringBuilder.append(complete ? "Complete, " : "NOT Complete")
        stringBuilder.append(isSuccessful() ? ", Successful " : ", NOT Successful")
        if (this.failures.size() > 1) {
            stringBuilder.append(':')
            this.failures.each { SipProcessingFailure failure ->
                stringBuilder.append(System.lineSeparator())
                appendFailure(stringBuilder, offset + 4, failure)
            }
        } else if (this.failures.size() == 1) {
            stringBuilder.append(': ')
            appendFailure(stringBuilder, 0, this.failures.first())
        }

        return stringBuilder.toString()
    }

    private void appendFailure(StringBuilder stringBuilder, int offset, SipProcessingFailure failure) {
        String initialOffset = StringUtils.repeat(' ', offset)
        stringBuilder.append(initialOffset)
        stringBuilder.append(failure.toString(offset))
    }
}
