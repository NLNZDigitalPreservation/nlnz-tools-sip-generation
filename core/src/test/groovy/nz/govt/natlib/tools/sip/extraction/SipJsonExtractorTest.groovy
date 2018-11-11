package nz.govt.natlib.tools.sip.extraction

import nz.govt.natlib.tools.sip.Sip
import nz.govt.natlib.tools.sip.SipTestHelper

import org.junit.Test

import static org.junit.Assert.assertTrue

/**
 * Tests the {@link SipJsonExtractor}.
 *
 * TODO Use cases:
 * - Invalid JSON
 * - JSON with no FileWrappers
 */
class SipJsonExtractorTest {

    SipJsonExtractor testSip

    @Test
    void extractsSipOneCorrectly() {
        testSip = new SipJsonExtractor(SipTestHelper.getTextFromResourceOrFile(SipTestHelper.TEST_SIP_JSON_1_FILENAME))

        assertTrue("SipJsonExtractor has content", testSip.json.length() > 0)

        Sip theSip = testSip.asSip()
        SipTestHelper.verifySipOne(theSip)
    }

    @Test
    void extractsAucklandCityHarbourNewsCorrectly() {
        testSip = new SipJsonExtractor(SipTestHelper.getTextFromResourceOrFile(SipTestHelper.TEST_SIP_JSON_ACHN_ACTUAL_FILENAME))

        assertTrue("SipJsonExtractor has content", testSip.json.length() > 0)

        Sip theSip = testSip.asSip()
        SipTestHelper.verifyAucklandCityHarbourNews(theSip)
    }
}