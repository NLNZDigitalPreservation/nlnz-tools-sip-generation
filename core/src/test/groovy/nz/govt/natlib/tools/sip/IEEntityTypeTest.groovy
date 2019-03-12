package nz.govt.natlib.tools.sip

import static org.hamcrest.core.Is.is
import static org.junit.Assert.assertThat

import org.junit.Test

/**
 * Tests {@link IEEntityType}.
 */
class IEEntityTypeTest {

    @Test
    void correctlyDeterminesIEEntityTypeFromValue() {
        assertThat(IEEntityType.matching('UNKNOWN'), is(IEEntityType.UNKNOWN))
        assertThat(IEEntityType.matching('UNKNOWN_IE'), is(IEEntityType.UNKNOWN))
        assertThat(IEEntityType.matching('unknown'), is(IEEntityType.UNKNOWN))

        assertThat(IEEntityType.matching('JUNK'), is(IEEntityType.UNKNOWN))

        assertThat(IEEntityType.matching('MagazineIE'), is(IEEntityType.MagazineIE))
        assertThat(IEEntityType.matching('magazine'), is(IEEntityType.MagazineIE))
        assertThat(IEEntityType.matching('MagazineIE'), is(IEEntityType.MagazineIE))

        assertThat(IEEntityType.matching('NewspaperIE'), is(IEEntityType.NewspaperIE))
        assertThat(IEEntityType.matching('newspaper'), is(IEEntityType.NewspaperIE))
        assertThat(IEEntityType.matching('NewspaperIE'), is(IEEntityType.NewspaperIE))
    }

}
