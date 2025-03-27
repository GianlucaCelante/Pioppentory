package it.pioppi.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {

    private static final DateTimeFormatter LOCAL_DATE_TIME_PATTERN =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter LOCAL_DATE_PATTERN =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static String formatForDisplay(ZonedDateTime date) {
        if (date == null) return "";

        ZonedDateTime localDate = date.withZoneSameInstant(ZoneId.of(ConstantUtils.ZONE_ID));
        return localDate.format(LOCAL_DATE_TIME_PATTERN);
    }

    public static String formatForDisplay(LocalDate date) {
        if (date == null) return "";

        ZonedDateTime localDate = date.atStartOfDay(ZoneId.of(ConstantUtils.ZONE_ID));
        return localDate.format(LOCAL_DATE_PATTERN);
    }
}
