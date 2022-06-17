package nz.govt.natlib.tools.sip.generation

import org.xmlunit.diff.Difference

import static org.junit.Assert.assertFalse

import nz.govt.natlib.tools.sip.Sip
import nz.govt.natlib.tools.sip.SipTestHelper
import nz.govt.natlib.tools.sip.extraction.SipXmlExtractor
import org.junit.Test
import org.xmlunit.builder.DiffBuilder
import org.xmlunit.builder.Input
import org.xmlunit.diff.Diff

/**
 * Tests {@link SipXmlGenerator}.
 */
class SipXmlGeneratorTest {

    @Test
    void testSipXmlGenerationAucklandHarbourNews() {
        SipXmlExtractor sipXml = new SipXmlExtractor(SipTestHelper.getTextFromResourceOrFile(
                SipTestHelper.TEST_SIP_XML_ACHN_ACTUAL_FILENAME))

        Sip sip = sipXml.asSip()
        SipXmlGenerator sipXmlGenerator = new SipXmlGenerator(sip)

        String sipAsXml = sipXmlGenerator.sipAsXml

        String expectedXml = SipTestHelper.getTextFromResourceOrFile(SipTestHelper.TEST_SIP_XML_ACHN_EXPECTED_FILENAME)

        Diff theDiff = DiffBuilder.compare(Input.fromString(expectedXml))
            .withTest(Input.fromString(sipAsXml))
            .ignoreWhitespace()
            .ignoreComments()
            .normalizeWhitespace()
            .build()

        assertFalse("XML for Auckland City Harbour News 2015-07-29 is the same as generated", theDiff.hasDifferences())
    }

    @Test
    void testSipOne() {
        Sip sip = SipTestHelper.sipOne()
        SipXmlGenerator sipXmlGenerator = new SipXmlGenerator(sip)

        String sipAsXml = sipXmlGenerator.sipAsXml

        String expectedXml = SipTestHelper.getTextFromResourceOrFile(SipTestHelper.TEST_SIP_XML_1_FILENAME)

        Diff theDiff = DiffBuilder.compare(Input.fromString(expectedXml))
                .withTest(Input.fromString(sipAsXml))
                .ignoreWhitespace()
                .ignoreComments()
                .normalizeWhitespace()
                .build()

        if (theDiff.hasDifferences()) {
            StringBuffer buffer = new StringBuffer();
            for (Difference d: theDiff.getDifferences()) {
                buffer.append(d.toString());
            }
            print(buffer)
        }

        assertFalse("XML for SipTestHelper.sipOne() is the same as generated", theDiff.hasDifferences())
    }

}
