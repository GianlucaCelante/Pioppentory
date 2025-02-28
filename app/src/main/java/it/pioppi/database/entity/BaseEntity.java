package it.pioppi.database.entity;

import androidx.room.ColumnInfo;

import java.time.LocalDateTime;

import it.pioppi.DateTimeUtils;
import it.pioppi.database.typeconverters.Converters;

public class BaseEntity {

    @ColumnInfo(name = "creation_date")
    private LocalDateTime creationDate;
    @ColumnInfo(name = "last_update")
    private LocalDateTime lastUpdate;

    public BaseEntity() {
        this.creationDate = LocalDateTime.now();
        this.lastUpdate = LocalDateTime.now();
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }


}
