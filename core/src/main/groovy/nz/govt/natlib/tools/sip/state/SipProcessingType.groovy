package nz.govt.natlib.tools.sip.state

enum SipProcessingType {
    UNKNOWN('unknown'),
    NEWSPAPER('newspaper'),
    MAGAZINE('magazine')

    private final String displayName

    SipProcessingType(String displayName) {
        this.displayName = displayName
    }

    String getDisplayName() {
        return this.displayName
    }
}