package nz.govt.natlib.tools.sip.utils

import groovy.util.logging.Log4j2
import nz.govt.natlib.tools.sip.logging.Timekeeper

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Log4j2
class GeneralUtils {
    static final SimpleDateFormat TIMESTAMP_FORMATTER = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss.SSS')
    static final DateTimeFormatter DATE_YYYYMMDD_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd")
    static final DecimalFormat TOTAL_FORMAT = new DecimalFormat("###,###,###,###,###")

    static LocalDate parseDate(String dateString, DateTimeFormatter formatter = DATE_YYYYMMDD_FORMATTER) {
        LocalDate parsedDate = null
        if (dateString != null) {
            parsedDate = LocalDate.parse(dateString, formatter)
        }
        return parsedDate
    }

    static List<LocalDate> datesInRange(LocalDate startingDate, LocalDate endingDate) {
        List<LocalDate> dates = [ ]
        if (startingDate > endingDate) {
            log.warn("Starting date=${startingDate} comes after=${endingDate}, will return empty collection of dates.")
            return dates
        }
        LocalDate currentDate = startingDate
        while (currentDate <= endingDate) {
            dates.add(currentDate)
            currentDate = currentDate.plusDays(1L)
        }
        return dates
    }

    static List<String> datesAsStringsInRange(LocalDate startingDate, LocalDate endingDate,
                                              DateTimeFormatter localDateFormatter = DATE_YYYYMMDD_FORMATTER) {
        return datesInRange(startingDate, endingDate).collect { LocalDate date ->
            localDateFormatter.format(date)
        }
    }

    static void markElapsed(Timekeeper theTimekeeper, String marker, String markerDescription = null) {
        if (theTimekeeper != null) {
            theTimekeeper.markElapsed(marker, markerDescription)
        }
    }

    static void printAndFlush(String message) {
        System.out.print(message)
        System.out.flush()
    }
}
