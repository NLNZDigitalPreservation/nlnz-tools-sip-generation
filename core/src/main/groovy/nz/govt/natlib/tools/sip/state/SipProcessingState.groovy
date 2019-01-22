package nz.govt.natlib.tools.sip.state

import groovy.transform.Canonical
import org.apache.commons.lang3.StringUtils

@Canonical
class SipProcessingState {

    boolean complete = false
    List<SipProcessingException> exceptions = [ ]

    boolean hasExceptions() {
        return exceptions.size() > 0
    }

    boolean isSuccessful() {
        return complete && exceptions.size() == 0
    }

    void addException(SipProcessingException sipProcessingException) {
        this.exceptions.add(sipProcessingException)
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
        if (this.exceptions.size() > 1) {
            stringBuilder.append(':')
            this.exceptions.each { SipProcessingException exception ->
                stringBuilder.append(System.lineSeparator())
                appendException(stringBuilder, offset + 4, exception)
            }
        } else if (this.exceptions.size() == 1) {
            stringBuilder.append(': ')
            appendException(stringBuilder, 0, this.exceptions.first())
        }

        return stringBuilder.toString()
    }

    private void appendException(StringBuilder stringBuilder, int offset, SipProcessingException exception) {
        String initialOffset = StringUtils.repeat(' ', offset)
        stringBuilder.append(initialOffset)
        stringBuilder.append(exception.toString(offset))
    }
}
