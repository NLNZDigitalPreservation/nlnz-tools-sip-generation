package nz.govt.natlib.tools.sip.state

import nz.govt.natlib.tools.sip.SipProcessingException

import static org.hamcrest.core.Is.is

import org.junit.Test

import static org.junit.Assert.assertThat

/**
 * Tests the {@link SipProcessingFailureReason}.
 */
class SipProcessingFailureReasonTest {

    SipProcessingFailureReason sipProcessingFailureReason

    @Test
    void substitutionsHappenCorrectlyForNullExceptionNullDetails() {
        sipProcessingFailureReason = new SipProcessingFailureReason(SipProcessingFailureReasonType.GENERIC_THREE_PLACES,
                null)

        assertThat("No substitutions for no details", sipProcessingFailureReason.toString(),
                is('${a} ${b} ${c}'))
    }

    @Test
    void substitutionsHappenCorrectlyForNullExceptionOneDetail() {
        sipProcessingFailureReason = new SipProcessingFailureReason(SipProcessingFailureReasonType.GENERIC_THREE_PLACES,
                null, "ONE")

        assertThat("No substitutions for no details", sipProcessingFailureReason.toString(),
                is('ONE ${b} ${c}'))
    }

    @Test
    void substitutionsHappenCorrectlyForNullExceptionTwoDetails() {
        sipProcessingFailureReason = new SipProcessingFailureReason(SipProcessingFailureReasonType.GENERIC_THREE_PLACES,
                null, "ONE", "TWO")

        assertThat("No substitutions for no details", sipProcessingFailureReason.toString(),
                is('ONE TWO ${c}'))
    }

    @Test
    void substitutionsHappenCorrectlyForNullExceptionThreeDetails() {
        sipProcessingFailureReason = new SipProcessingFailureReason(SipProcessingFailureReasonType.GENERIC_THREE_PLACES,
                null, "ONE", "TWO", "THREE")

        assertThat("No substitutions for no details", sipProcessingFailureReason.toString(),
                is('ONE TWO THREE'))
    }

    @Test
    void substitutionsHappenCorrectlyForNullExceptionFourDetails() {
        sipProcessingFailureReason = new SipProcessingFailureReason(SipProcessingFailureReasonType.GENERIC_THREE_PLACES,
                null, "ONE", "TWO", "THREE", "FOUR")

        assertThat("No substitutions for no details", sipProcessingFailureReason.toString(),
                is('ONE TWO THREE'))
    }

    @Test
    void substitutionsHappenCorrectlyForExceptionNoDetails() {
        Exception exception = new SipProcessingException("TEST EXCEPTION")
        sipProcessingFailureReason = new SipProcessingFailureReason(SipProcessingFailureReasonType.GENERIC_THREE_PLACES,
                exception)

        assertThat("No substitutions for no details", sipProcessingFailureReason.toString(),
                is('${a} ${b} nz.govt.natlib.tools.sip.SipProcessingException: TEST EXCEPTION'))
    }

    @Test
    void substitutionsHappenCorrectlyForExceptionOneDetails() {
        Exception exception = new SipProcessingException("TEST EXCEPTION")
        sipProcessingFailureReason = new SipProcessingFailureReason(SipProcessingFailureReasonType.GENERIC_THREE_PLACES,
                exception, "ONE")

        assertThat("No substitutions for no details", sipProcessingFailureReason.toString(),
                is('ONE ${b} nz.govt.natlib.tools.sip.SipProcessingException: TEST EXCEPTION'))
    }

    @Test
    void substitutionsHappenCorrectlyForExceptionTwoDetails() {
        Exception exception = new SipProcessingException("TEST EXCEPTION")
        sipProcessingFailureReason = new SipProcessingFailureReason(SipProcessingFailureReasonType.GENERIC_THREE_PLACES,
                exception, "ONE", "TWO")

        assertThat("No substitutions for no details", sipProcessingFailureReason.toString(),
                is('ONE TWO nz.govt.natlib.tools.sip.SipProcessingException: TEST EXCEPTION'))
    }

    @Test
    void substitutionsHappenCorrectlyForExceptionThreeDetails() {
        Exception exception = new SipProcessingException("TEST EXCEPTION")
        sipProcessingFailureReason = new SipProcessingFailureReason(SipProcessingFailureReasonType.GENERIC_THREE_PLACES,
                exception, "ONE", "TWO", "THREE")

        assertThat("No substitutions for no details", sipProcessingFailureReason.toString(),
                is('ONE TWO nz.govt.natlib.tools.sip.SipProcessingException: TEST EXCEPTION'))
    }
}
