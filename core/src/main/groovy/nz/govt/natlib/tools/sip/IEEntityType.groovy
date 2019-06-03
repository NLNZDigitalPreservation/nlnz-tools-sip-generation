package nz.govt.natlib.tools.sip

enum IEEntityType {
    UNKNOWN('UNKNOWN_IE', 'unknown_ie'),
    NewspaperIE('NewspaperIE', 'newspaper'),
    MagazineIE('MagazineIE', 'magazine')

    private final String fieldValue
    private final String displayName

    IEEntityType(String fieldValue, String displayName) {
        this.fieldValue = fieldValue
        this.displayName = displayName
    }

    static IEEntityType matching(String matchValue) {
        IEEntityType foundType = values().find { IEEntityType ieEntityType ->
            (ieEntityType.getFieldValue() == matchValue || ieEntityType.getDisplayName() == matchValue ||
                    ieEntityType.name() == matchValue)
        }
        return foundType == null ? UNKNOWN : foundType
    }

    String getFieldValue() {
        return this.fieldValue
    }

    String getDisplayName() {
        return this.displayName
    }

    String toString() {
        return this.fieldValue
    }
}