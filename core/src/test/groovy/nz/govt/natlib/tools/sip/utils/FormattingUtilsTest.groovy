package nz.govt.natlib.tools.sip.utils

import java.time.Duration

import static org.hamcrest.core.Is.is
import static org.junit.Assert.assertThat

import org.junit.Test

class FormattingUtilsTest {

    @Test
    void scalesBase2ValuesAsExpected() {
        assertThat(FormattingUtils.formatByteCount(1L, false, " "), is("1 B"))
        assertThat(FormattingUtils.formatByteCount(1234L, false, " "), is("1.205 KiB"))
        assertThat(FormattingUtils.formatByteCount(12345600L, false, " "), is("11.774 MiB"))
        assertThat(FormattingUtils.formatByteCount(1234560999L, false, ""), is("1.150GiB"))
        assertThat(FormattingUtils.formatByteCount(991234460999000L, false, " "), is("901.522 TiB"))
    }

    @Test
    void scalesSiValuesAsExpected() {
        assertThat(FormattingUtils.formatByteCount(1L, true, " "), is("1 B"))
        assertThat(FormattingUtils.formatByteCount(1234L, true, " "), is("1.234 kB"))
        // Note the rounding
        assertThat(FormattingUtils.formatByteCount(12345600L, true, " "), is("12.346 MB"))
        assertThat(FormattingUtils.formatByteCount(1234260999L, true, ""), is("1.234GB"))
        assertThat(FormattingUtils.formatByteCount(1234560999L, true, " "), is("1.235 GB"))
        assertThat(FormattingUtils.formatByteCount(991234460999000L, true, " "), is("991.234 TB"))
        assertThat(FormattingUtils.formatByteCount(991234560999000L, true, ""), is("991.235TB"))
    }

    @Test
    void formatsDurationAsExpected() {
        assertThat(FormattingUtils.formatDuration(Duration.ofNanos(1999888777)), is("0:00:01.999"))
        assertThat(FormattingUtils.formatDuration(Duration.ofMillis(5123456)), is("1:25:23.456"))
        assertThat(FormattingUtils.formatDuration(Duration.ofSeconds(7574)), is("2:06:14.0"))
        assertThat(FormattingUtils.formatDuration(Duration.ofSeconds(86401)), is("1d 0:00:01.0"))
        assertThat(FormattingUtils.formatDuration(Duration.ofSeconds(98256451)), is("1137d 5:27:31.0"))
        assertThat(FormattingUtils.formatDuration(Duration.ofNanos(83576)), is("0:00:00.0"))
    }

    @Test
    void formatsDecimalsAsExpected() {
        assertThat(FormattingUtils.formatPercent(0.123, 2), is("12.30%"))
        // Note the rounding
        assertThat(FormattingUtils.formatPercent(0.12344, 2), is("12.34%"))
        assertThat(FormattingUtils.formatPercent(0.12345, 2), is("12.35%"))
        assertThat(FormattingUtils.formatPercent(23.3, 0), is("2330%"))
        assertThat(FormattingUtils.formatPercent(0.8356, 1), is("83.6%"))
        assertThat(FormattingUtils.formatPercent(0.97123), is("97.123%"))
    }
}
