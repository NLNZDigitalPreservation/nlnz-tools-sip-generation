package nz.govt.natlib.tools.sip

class SipProcessingException extends Exception {
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
}
