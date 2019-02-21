package nz.govt.natlib.tools.sip.pdf

enum PdfValidatorType {
    PDF_BOX_VALIDATOR(PdfValidatorPdfBox.class),
    JHOVE_VALIDATOR(PdfValidatorJhove.class)

    private final Class implementation

    PdfValidatorType(Class clazz) {
        this.implementation = clazz
    }

    Class getImplementation() {
        return this.implementation
    }
}
