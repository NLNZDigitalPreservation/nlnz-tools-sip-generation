package nz.govt.natlib.tools.sip.state


import static org.hamcrest.core.Is.is

import org.junit.Test

import static org.junit.Assert.assertThat

/**
 * Tests the {@link SipProcessingExceptionReason}.
 */
class SipProcessingExceptionReasonTest {

    SipProcessingExceptionReason sipProcessingExceptionReason

    @Test
    void substitutionsHappenCorrectlyForNullExceptionNullDetails() {
        sipProcessingExceptionReason = new SipProcessingExceptionReason(SipProcessingExceptionReasonType.GENERIC_THREE_PLACES,
                null)

        assertThat("No substitutions for no details", sipProcessingExceptionReason.toString(),
                is('${a} ${b} ${c}'))
    }

    @Test
    void substitutionsHappenCorrectlyForNullExceptionOneDetail() {
        sipProcessingExceptionReason = new SipProcessingExceptionReason(SipProcessingExceptionReasonType.GENERIC_THREE_PLACES,
                null, "ONE")

        assertThat("No substitutions for no details", sipProcessingExceptionReason.toString(),
                is('ONE ${b} ${c}'))
    }

    @Test
    void substitutionsHappenCorrectlyForNullExceptionTwoDetails() {
        sipProcessingExceptionReason = new SipProcessingExceptionReason(SipProcessingExceptionReasonType.GENERIC_THREE_PLACES,
                null, "ONE", "TWO")

        assertThat("No substitutions for no details", sipProcessingExceptionReason.toString(),
                is('ONE TWO ${c}'))
    }

    @Test
    void substitutionsHappenCorrectlyForNullExceptionThreeDetails() {
        sipProcessingExceptionReason = new SipProcessingExceptionReason(SipProcessingExceptionReasonType.GENERIC_THREE_PLACES,
                null, "ONE", "TWO", "THREE")

        assertThat("No substitutions for no details", sipProcessingExceptionReason.toString(),
                is('ONE TWO THREE'))
    }

    @Test
    void substitutionsHappenCorrectlyForNullExceptionFourDetails() {
        sipProcessingExceptionReason = new SipProcessingExceptionReason(SipProcessingExceptionReasonType.GENERIC_THREE_PLACES,
                null, "ONE", "TWO", "THREE", "FOUR")

        assertThat("No substitutions for no details", sipProcessingExceptionReason.toString(),
                is('ONE TWO THREE'))
    }

    @Test
    void substitutionsHappenCorrectlyForExceptionNoDetails() {
        Exception exception = new SipProcessingException("TEST EXCEPTION")
        sipProcessingExceptionReason = new SipProcessingExceptionReason(SipProcessingExceptionReasonType.GENERIC_THREE_PLACES,
                exception)

        assertThat("No substitutions for no details", sipProcessingExceptionReason.toString(),
                is('${a} ${b} nz.govt.natlib.tools.sip.state.SipProcessingException: TEST EXCEPTION: NO REASONS GIVEN'))
    }

    @Test
    void substitutionsHappenCorrectlyForExceptionOneDetails() {
        Exception exception = new SipProcessingException("TEST EXCEPTION")
        sipProcessingExceptionReason = new SipProcessingExceptionReason(SipProcessingExceptionReasonType.GENERIC_THREE_PLACES,
                exception, "ONE")

        assertThat("No substitutions for no details", sipProcessingExceptionReason.toString(),
                is('ONE ${b} nz.govt.natlib.tools.sip.state.SipProcessingException: TEST EXCEPTION: NO REASONS GIVEN'))
    }

    @Test
    void substitutionsHappenCorrectlyForExceptionTwoDetails() {
        Exception exception = new SipProcessingException("TEST EXCEPTION")
        sipProcessingExceptionReason = new SipProcessingExceptionReason(SipProcessingExceptionReasonType.GENERIC_THREE_PLACES,
                exception, "ONE", "TWO")

        assertThat("No substitutions for no details", sipProcessingExceptionReason.toString(),
                is('ONE TWO nz.govt.natlib.tools.sip.state.SipProcessingException: TEST EXCEPTION: NO REASONS GIVEN'))
    }

    @Test
    void substitutionsHappenCorrectlyForExceptionThreeDetails() {
        Exception exception = new SipProcessingException("TEST EXCEPTION")
        sipProcessingExceptionReason = new SipProcessingExceptionReason(SipProcessingExceptionReasonType.GENERIC_THREE_PLACES,
                exception, "ONE", "TWO", "THREE")

        assertThat("No substitutions for no details", sipProcessingExceptionReason.toString(),
                is('ONE TWO nz.govt.natlib.tools.sip.state.SipProcessingException: TEST EXCEPTION: NO REASONS GIVEN'))
    }
}
