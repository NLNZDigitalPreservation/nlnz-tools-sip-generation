package nz.govt.natlib.tools.sip.utils

import java.text.DateFormat
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.Duration

class FormattingUtils {
    static final DecimalFormat TRILLION_THREE_DECIMAL_FORMAT = new DecimalFormat("###,###,###,###,###.000")
    static final DateFormat TIMESTAMP_FORMAT_MILLI_ZONE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z")

    // Based on https://programming.guide/java/formatting-byte-size-to-human-readable-format.html
    static String formatByteCount(long numberBytes, boolean useSiUnits = true, String separator = " ") {
        int unitFactor = useSiUnits ? 1000 : 1024
        if (numberBytes < unitFactor) {
            return "${numberBytes}${separator}B"
        }
        int exponent = (int) (Math.log(numberBytes) / Math.log(unitFactor))
        String unitCharacter = (useSiUnits ? "kMGTPE" : "KMGTPE").charAt(exponent - 1)
        String siOrBase2 = useSiUnits ? "" : "i"
        String suffix = "${unitCharacter}${siOrBase2}B"

        double scaledNumberBytes = numberBytes / Math.pow(unitFactor, exponent)

        String formatted = "${TRILLION_THREE_DECIMAL_FORMAT.format(scaledNumberBytes)}${separator}${suffix}"

        return formatted
    }

    static String formatDuration(Duration duration) {
        String daysPart = duration.toDaysPart() > 0 ? "${duration.toDaysPart()}d " : ""
        String minutesPart = String.format("%02d", duration.toMinutesPart())
        String secondsPart = String.format("%02d", duration.toSecondsPart())
        return "${daysPart}${duration.toHoursPart()}:${minutesPart}:${secondsPart}.${duration.toMillisPart()}"
    }

    static String formatPercent(double value, int decimalPlaces = 3) {
        double percentage = value * 100.0
        return "${String.format("%01.${decimalPlaces}f", percentage)}%"
    }
}
