package it.pioppi.database.typeconverters;

import androidx.room.TypeConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import it.pioppi.database.model.QuantityType;
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
    public static LocalDateTime toDate(String dateString) {
        if (dateString == null) {
            return null;
        } else {
            return LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }

    @TypeConverter
    public static String toDateString(LocalDateTime date) {
        if (date == null) {
            return null;
        } else {
            return date.toString();
        }
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
    public static QuantityType toQuantityType(String quantityTypeString) {
        if (quantityTypeString == null) {
            return null;
        } else {
            return QuantityType.valueOf(quantityTypeString);
        }
    }

    @TypeConverter
    public static String fromQuantityType(QuantityType quantityType) {
        if (quantityType == null) {
            return null;
        } else {
            return quantityType.name();
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
