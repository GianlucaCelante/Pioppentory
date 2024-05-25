package it.pioppi.database.model;

import androidx.room.ColumnInfo;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import java.time.LocalDateTime;

import it.pioppi.database.tipeconverters.LocalDateTimeConverter;

public class BaseEntity {

    @ColumnInfo(name = "creation_date")
    private LocalDateTime creationDate;
    @ColumnInfo(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(LocalDateTime lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }


}
