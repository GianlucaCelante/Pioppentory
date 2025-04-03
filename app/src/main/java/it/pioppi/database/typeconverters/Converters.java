package it.pioppi.database.typeconverters;

import androidx.room.TypeConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;

import it.pioppi.database.model.QuantityTypeEnum;
import it.pioppi.database.model.ItemStatus;

public class Converters {

    @TypeConverter
    public static UUID toUUID(String uuidString) {
        if (uuidString == null) {
            return null;
        } else {
            return UUID.fromString(uuidString);
        }
    }

    @TypeConverter
    public static String fromUUID(UUID uuid) {
        if (uuid == null) {
            return null;
        } else {
            return uuid.toString();
        }
    }

    @TypeConverter
    public static ZonedDateTime toZonedDateTime(String dateString) {
        if (dateString == null) {
            return null;
        }
        try {
            // Prova a fare il parsing considerando la presenza dell'offset/fuso
            return ZonedDateTime.parse(dateString, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        } catch (DateTimeParseException e) {
            // Se non c'è il fuso, lo interpretiamo come LocalDateTime e applichiamo il fuso di default
            LocalDateTime localDateTime = LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return localDateTime.atZone(ZoneId.systemDefault());
        }
    }

    @TypeConverter
    public static String fromZonedDateTime(ZonedDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }


    @TypeConverter
    public static LocalDate toLocalDate(String dateString) {
        if (dateString == null) {
            return null;
        } else {
            return LocalDate.parse(dateString);
        }
    }

    @TypeConverter
    public static String toLocalDateString(LocalDate date) {
        if (date == null) {
            return null;
        } else {
            return date.toString();
        }
    }

    @TypeConverter
    public static QuantityTypeEnum toQuantityType(String quantityTypeString) {
        if (quantityTypeString == null) {
            return null;
        } else {
            return QuantityTypeEnum.valueOf(quantityTypeString);
        }
    }

    @TypeConverter
    public static String fromQuantityType(QuantityTypeEnum quantityTypeEnum) {
        if (quantityTypeEnum == null) {
            return null;
        } else {
            return quantityTypeEnum.name();
        }
    }

    @TypeConverter
    public static String fromItemStatus(ItemStatus itemStatus) {
        if (itemStatus == null) {
            return null;
        } else {
            return itemStatus.name();
        }
    }

    @TypeConverter
    public static ItemStatus toItemStatus(String itemStatusString) {
        if (itemStatusString == null) {
            return null;
        } else {
            return ItemStatus.valueOf(itemStatusString);
        }
    }
}
