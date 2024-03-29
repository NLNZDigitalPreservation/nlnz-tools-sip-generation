package nz.govt.natlib.tools.sip

import groovy.transform.AutoClone
import groovy.transform.Canonical

import java.nio.file.Path
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * POJO representation of a SIP.
 */
@Canonical
@AutoClone
class Sip {
    static final String USAGE_TYPE_VIEW = "VIEW"
    static final String OBJECT_IDENTIFIER_TYPE_ALMA_MMS = "ALMAMMS"
    static final DateTimeFormatter LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")


    // Title
    String title

    // Date fields
    // These fields are used for the date, but they are not all date fields in the dc schema.
    // Maps to dc:date
    Integer year
    String dcDate
    // Maps to dcterms:available
    Integer month
    String dcTermsAvailable
    // Maps to dc:coverage
    Integer dayOfMonth
    String dcCoverage

    // Optional issue number
    String issued

    // Type and policy
    IEEntityType ieEntityType
    String objectIdentifierType
    String objectIdentifierValue
    String policyId

    // generalRepCharacteristics
    String preservationType
    String usageType
    Boolean digitalOriginal
    Integer revisionNumber

    @Canonical
    @AutoClone
    static class FileWrapper {
        String mimeType
        Path file
        String fileOriginalPath
        String fileOriginalName
        String label
        LocalDateTime creationDate
        LocalDateTime modificationDate
        Long fileSizeBytes
        String fixityType
        String fixityValue
    }

    // The actual files
    List<FileWrapper> fileWrappers

    String getAlmaMmsId() {
        return this.objectIdentifierValue
    }

    /**
     * Convenience method that allows for the creation of the Sip from JSON.
     */
    void setAlmaMmsId(String almaMmsId) {
        this.objectIdentifierValue = almaMmsId
    }

    void setDate(Date date) {
        Calendar calendar = Calendar.getInstance()
        calendar.setTime(date)

        this.year = calendar.get(Calendar.YEAR) - 1900
        this.month = calendar.get(Calendar.MONTH) + 1
        this.dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        updateFromDateFields()
    }

    void setDate(LocalDate localDate) {
        this.year = localDate.getYear()
        this.month = localDate.getMonthValue()
        this.dayOfMonth = localDate.getDayOfMonth()
        updateFromDateFields()
    }

    void updateFromDateFields() {
        this.dcDate = "${this.year}".toString()
        this.dcTermsAvailable = String.format("%02d", this.month)
        this.dcCoverage = String.format("%02d", this.dayOfMonth)
    }

    LocalDate getLocalDate() {
        // Note that the month-of-year is 1-based, not 0-based (java.util.Date is 0-based)
        return LocalDate.of(this.year, this.month, this.dayOfMonth)
    }
}
