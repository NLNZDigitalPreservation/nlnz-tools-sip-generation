package nz.govt.natlib.tools.sip.extraction

import groovy.util.slurpersupport.GPathResult
import nz.govt.natlib.tools.sip.IEEntityType
import nz.govt.natlib.tools.sip.Sip
import nz.govt.natlib.tools.sip.SipTestHelper

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

import static org.hamcrest.core.Is.is

import org.junit.Test

import static org.junit.Assert.assertNull
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue

/**
 * Tests the {@link SipXmlExtractor}.
 */
class SipXmlExtractorTest {
    SipXmlExtractor testSip

    @Test
    void extractsSipOneCorrectly() {
        testSip = new SipXmlExtractor(SipTestHelper.getTextFromResourceOrFile(SipTestHelper.TEST_SIP_XML_1_FILENAME))

        assertTrue("SipXmlExtractor has content", testSip.xml.length() > 0)

        Sip theSip = testSip.asSip()

        // The XML generation does not store the actual File stored in the SIP
        boolean checkFileWrapperFiles = false
        SipTestHelper.verifySipOne(theSip, checkFileWrapperFiles)
    }

    @Test
    void extractsAucklandCityHarbourNewsCorrectly() {
        testSip = new SipXmlExtractor(SipTestHelper.getTextFromResourceOrFile(SipTestHelper.TEST_SIP_XML_ACHN_ACTUAL_FILENAME))

        assertTrue("SipXmlExtractor has content", testSip.xml.length() > 0)

        Sip theSip = testSip.asSip()

        SipTestHelper.verifyAucklandCityHarbourNews(theSip)
    }

    @Test
    void rawMethodsExtractsAucklandCityHarbourNewsCorrectly() {
        testSip = new SipXmlExtractor(SipTestHelper.getTextFromResourceOrFile(SipTestHelper.TEST_SIP_XML_ACHN_ACTUAL_FILENAME))

        assertTrue("SipXmlExtractor has content", testSip.xml.length() > 0)

        assertThat("title", testSip.extractTitle(), is("Auckland City Harbour News"))
        assertThat("year", testSip.extractYear(), is(2015))
        assertThat("month", testSip.extractMonth(), is(7))
        assertThat("dayOfMonth", testSip. extractDayOfMonth(), is(29))
        assertThat("dcDate", testSip.extractDcDate(), is("2015"))
        assertThat("dcTermsAvailable", testSip.extractDcTermsAvailable(), is("7"))
        assertThat("dcCoverage", testSip. extractDcCoverage(), is("29"))
        assertThat("ieEntityType",testSip.extractIEEntityType(), is(IEEntityType.NewspaperIE))
        assertThat("objectIdentifierType", testSip.extractObjectIdentifierType(), is("ALMAMMS"))
        assertThat("objectIdentifierValue", testSip.extractObjectIdentifierValue(), is("9917982663502836"))
        assertThat("policyId", testSip.extractPolicyId(), is("200"))
        assertThat("preservationType", testSip.extractPreservationType(), is("PRESERVATION_MASTER"))
        assertThat("usageType", testSip.extractUsageType(), is("VIEW"))
        assertThat("digitalOriginal", testSip.extractDigitalOriginal(), is(true))
        assertThat("revisionNumber", testSip.extractRevisionNumber(), is(1))

        GPathResult fileGPath1 = testSip.extractFileIdRecord(1)
        assertThat("fileWrapper1.creationDate", testSip.extractFileCreationDate(fileGPath1), is(LocalDateTime.of(
                LocalDate.of(2015, 7, 29),
                LocalTime.of(0, 0, 0, 0))))
        assertThat("fileWrapper1.fileOriginalName", testSip.extractFileOriginalName(fileGPath1), is("AHNED1-20150729-001.pdf"))
        assertThat("fileWrapper1.fileOriginalPath", testSip.extractFileOriginalPath(fileGPath1), is("AHNED1-20150729-001.pdf"))
        assertThat("fileWrapper1.fileSizeBytes", testSip.extractFileSizeBytes(fileGPath1), is(1036040L))
        assertThat("fileWrapper1.fixityType", testSip.extractFileFixityType(fileGPath1), is("MD5"))
        assertThat("fileWrapper1.fixityValue", testSip.extractFileFixityValue(fileGPath1), is("e9c6a70cd194cb38c11e56499beee282"))
        assertThat("fileWrapper1.label", testSip.extractFileLabel(fileGPath1), is("001"))
        assertNull("fileWrapper1.mimeType", testSip.extractFileMimeType(fileGPath1))
        assertThat("fileWrapper1.modificationDate", testSip.extractFileModificationDate(fileGPath1), is(LocalDateTime.of(
                LocalDate.of(2015, 7, 29),
                LocalTime.of(0, 0, 0, 0))))

        GPathResult fileGPath12 = testSip.extractFileIdRecord(12)
        assertThat("fileWrapper12.creationDate", testSip.extractFileCreationDate(fileGPath12), is(LocalDateTime.of(
                LocalDate.of(2015, 7, 29),
                LocalTime.of(0, 0, 0, 0))))
        assertThat("fileWrapper12.fileOriginalName", testSip.extractFileOriginalName(fileGPath12), is("AHNED1-20150729-012.pdf"))
        assertThat("fileWrapper12.fileOriginalPath", testSip.extractFileOriginalPath(fileGPath12), is("AHNED1-20150729-012.pdf"))
        assertThat("fileWrapper12.fileSizeBytes", testSip.extractFileSizeBytes(fileGPath12), is(2410038L))
        assertThat("fileWrapper12.fixityType", testSip.extractFileFixityType(fileGPath12), is("MD5"))
        assertThat("fileWrapper12.fixityValue", testSip.extractFileFixityValue(fileGPath12), is("1f2d11ca06e302b7c1295815a20c86cd"))
        assertThat("fileWrapper12.label", testSip.extractFileLabel(fileGPath12), is("012"))
        assertNull("fileWrapper12.mimeType", testSip.extractFileMimeType(fileGPath12))
        assertThat("fileWrapper12.modificationDate", testSip.extractFileModificationDate(fileGPath12), is(LocalDateTime.of(
                LocalDate.of(2015, 7, 29),
                LocalTime.of(0, 0, 0, 0))))
    }
}
