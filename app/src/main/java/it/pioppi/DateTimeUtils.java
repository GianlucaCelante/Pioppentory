package it.pioppi;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {

    private static final DateTimeFormatter USER_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String formatForDisplay(ZonedDateTime date) {
        if (date == null) return "";

        ZonedDateTime localDate = date.withZoneSameInstant(ZoneId.of("Europe/Rome"));
        return localDate.format(USER_FORMATTER);
    }
}
