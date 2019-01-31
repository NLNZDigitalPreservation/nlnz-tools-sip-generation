package nz.govt.natlib.tools.sip.state


import org.apache.commons.lang3.StringUtils

class SipProcessingException extends Exception {
    List<SipProcessingExceptionReason> reasons = [ ]

    static SipProcessingException createWithReason(SipProcessingExceptionReason sipProcessingExceptionReason) {
        SipProcessingException sipProcessingException = new SipProcessingException()
        sipProcessingException.addReason(sipProcessingExceptionReason)

        return sipProcessingException
    }

    SipProcessingException() {
        super()
    }

    SipProcessingException(String message) {
        super(message)
    }

    SipProcessingException(String message, Throwable cause) {
        super(message, cause)
    }

    SipProcessingException(Throwable cause) {
        super(cause)
    }

    protected SipProcessingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace)
    }

    void addReason(SipProcessingExceptionReasonType exceptionReasonType, Exception exception, String... detail) {
        SipProcessingExceptionReason sipProcessingExceptionReason =
                new SipProcessingExceptionReason(exceptionReasonType, exception, detail)

        this.addReason(sipProcessingExceptionReason)
    }

    void addReason(SipProcessingExceptionReason exceptionReason) {
        reasons.add(exceptionReason)
    }

    String toString() {
        return toString(0)
    }

    String toString(int offset) {
        String initialOffset = StringUtils.repeat(' ', offset)
        StringBuilder stringBuilder = new StringBuilder(initialOffset)
        stringBuilder.append(this.getClass().getName())
        stringBuilder.append(':')
        String localizedMessage = getLocalizedMessage()
        if (localizedMessage != null) {
            stringBuilder.append(' ')
            stringBuilder.append(localizedMessage)
            stringBuilder.append(': ')
        }
        if (reasons.size() > 1) {
            this.reasons.each { SipProcessingExceptionReason reason ->
                stringBuilder.append(System.lineSeparator())
                appendReason(stringBuilder, offset + 4, reason)
            }
        } else if (reasons.size() == 1) {
            appendReason(stringBuilder, 1, reasons.first())
        } else {
            stringBuilder.append("NO REASONS GIVEN")
        }

        return stringBuilder.toString()
    }

    private void appendReason(StringBuilder stringBuilder, int offset, SipProcessingExceptionReason reason) {
        String initialOffset = StringUtils.repeat(' ', offset)
        stringBuilder.append(initialOffset)
        stringBuilder.append(reason.toString())
    }
}
