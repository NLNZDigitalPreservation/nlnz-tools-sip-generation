package nz.govt.natlib.tools.sip

import static org.hamcrest.core.Is.is

import org.junit.Test

import java.time.LocalDate

import static org.junit.Assert.assertThat

/**
 * Tests the {@link Sip}.
 */
class SipTest {
    Sip testSip

    @Test
    void localDateExtractsCorrectly() {
        testSip = new Sip()
        testSip.year = 2018
        testSip.month = 11
        testSip.dayOfMonth = 22

        LocalDate verifyDate = new LocalDate(2018, 11, 22)
        assertThat("LocalDate is correct", testSip.getLocalDate(), is(verifyDate))
    }

    @Test
    void localDateSetsCorrectly() {
        testSip = new Sip()
        LocalDate setDate = new LocalDate(2018, 11, 22)
        testSip.setDate(setDate)

        assertThat("year is correct", testSip.year, is(2018))
        assertThat("month is correct", testSip.month, is(11))
        assertThat("dayOfMonth is correct", testSip.dayOfMonth, is(22))
    }

    @Test
    void dateSetsCorrectly() {
        testSip = new Sip()

        Date setDate = new Date(2018, 10, 22)
        testSip.setDate(setDate)

        assertThat("year is correct", testSip.year, is(2018))
        assertThat("month is correct", testSip.month, is(11))
        assertThat("dayOfMonth is correct", testSip.dayOfMonth, is(22))
    }

    @Test
    void almaMmsIdDerivedCorrectly() {
        testSip = new Sip()

        String mmsId = "123456"
        testSip.objectIdentifierValue = mmsId

        assertThat("Alma MMS ID derived correctly", testSip.almaMmsId, is(mmsId))
    }

    @Test
    void almaMmsIdSetsCorrectly() {
        testSip = new Sip()

        String mmsId = "123456"
        testSip.objectIdentifierValue = "another-id"
        testSip.setAlmaMmsId(mmsId)

        assertThat("Alma MMS ID sets objectIdentifierValue correctly", testSip.objectIdentifierValue, is(mmsId))
    }
}
