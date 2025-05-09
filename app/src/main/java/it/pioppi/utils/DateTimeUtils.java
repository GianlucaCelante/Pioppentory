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

    /**
     * Formatta uno ZonedDateTime (incluso orario) per display completo.
     */
    public static String formatForDisplay(ZonedDateTime date) {
        if (date == null) return "";
        ZonedDateTime localDate = date.withZoneSameInstant(ZoneId.of(ConstantUtils.ZONE_ID));
        return localDate.format(LOCAL_DATE_TIME_PATTERN);
    }

    /**
     * Formatta uno ZonedDateTime solo come data dd-MM-yyyy.
     */
    public static String formatForDisplayToDate(ZonedDateTime date) {
        if (date == null) return "";
        ZonedDateTime localDate = date.withZoneSameInstant(ZoneId.of(ConstantUtils.ZONE_ID));
        return localDate.format(LOCAL_DATE_PATTERN);
    }

    /**
     * Formatta un LocalDate come dd-MM-yyyy.
     */
    public static String formatForDisplayLocalDate(LocalDate date) {
        if (date == null) return "";
        ZonedDateTime localDate = date.atStartOfDay(ZoneId.of(ConstantUtils.ZONE_ID));
        return localDate.format(LOCAL_DATE_PATTERN);
    }

    /**
     * Parsifica una stringa nel formato dd-MM-yyyy in un LocalDate.
     */
    public static LocalDate parse(String date) {
        if (date == null || date.isEmpty()) {
            return null;
        }
        return LocalDate.parse(date, LOCAL_DATE_PATTERN);
    }

    /**
     * Parsifica una stringa nel formato yyyy-MM-dd HH:mm:ss in uno ZonedDateTime.
     */
    public static ZonedDateTime parseZonedDateTime(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) {
            return null;
        }
        ZonedDateTime zdt = ZonedDateTime.parse(dateTime, LOCAL_DATE_TIME_PATTERN.withZone(ZoneId.of(ConstantUtils.ZONE_ID)));
        return zdt.withZoneSameInstant(ZoneId.of(ConstantUtils.ZONE_ID));
    }
}
