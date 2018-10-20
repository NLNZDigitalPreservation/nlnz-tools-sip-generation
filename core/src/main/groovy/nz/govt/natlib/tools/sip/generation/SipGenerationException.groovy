package nz.govt.natlib.tools.sip.generation

class SipGenerationException extends Exception {
    SipGenerationException() {
        super()
    }

    SipGenerationException(String message) {
        super(message)
    }

    SipGenerationException(String message, Throwable cause) {
        super(message, cause)
    }

    SipGenerationException(Throwable cause) {
        super(cause)
    }

    protected SipGenerationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace)
    }
}
