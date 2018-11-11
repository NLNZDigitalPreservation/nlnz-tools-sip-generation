package nz.govt.natlib.tools.sip.generation

import static org.hamcrest.core.Is.is
import static org.junit.Assert.assertThat

import org.junit.Test

/**
 * Tests {@link MediaMimeType}.
 */
class MediaMimeTypeTest {

    @Test
    void applicationPdfCorrectlyDerivedForLowercasePDF() {
        MediaMimeType mediaMimeType = MediaMimeType.forExtension("pdf")

        assertThat("Correct mediaMimeType derived", mediaMimeType, is(MediaMimeType.APPLICATION_PDF))
        assertThat("Correct mimeType derived", mediaMimeType.mediaType.toString(), is("application/pdf"))
    }

    @Test
    void applicationPdfCorrectlyDerivedForUppercasePDF() {
        MediaMimeType mediaMimeType = MediaMimeType.forExtension("PDF")

        assertThat("Correct mediaMimeType derived", mediaMimeType, is(MediaMimeType.APPLICATION_PDF))
        assertThat("Correct mimeType derived", mediaMimeType.mediaType.toString(), is("application/pdf"))
    }
}
