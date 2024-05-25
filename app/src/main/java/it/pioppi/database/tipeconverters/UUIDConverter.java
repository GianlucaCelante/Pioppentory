package it.pioppi.database.tipeconverters;

import androidx.room.TypeConverter;

import java.util.UUID;

public class UUIDConverter {

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
}