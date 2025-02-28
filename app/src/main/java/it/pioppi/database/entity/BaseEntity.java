package it.pioppi.database.entity;

import androidx.room.ColumnInfo;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class BaseEntity {

    @ColumnInfo(name = "creation_date")
    private ZonedDateTime creationDate;
    @ColumnInfo(name = "last_update")
    private ZonedDateTime lastUpdate;

    public BaseEntity() {
        this.creationDate = ZonedDateTime.now(ZoneId.of("Europe/Rome"));
        this.lastUpdate = ZonedDateTime.now(ZoneId.of("Europe/Rome"));
    }

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(ZonedDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public ZonedDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(ZonedDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }


}
