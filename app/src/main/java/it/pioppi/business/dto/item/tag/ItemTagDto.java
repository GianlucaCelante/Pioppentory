package it.pioppi.business.dto.item.tag;

import java.time.ZonedDateTime;
import java.util.UUID;

public class ItemTagDto {

    private UUID id;
    private String name;
    private boolean isSelected;
    private ZonedDateTime creationDate;
    private ZonedDateTime lastUpdate;

    public ItemTagDto(UUID id, String name, boolean isSelected, ZonedDateTime creationDate, ZonedDateTime lastUpdate) {
        this.id = id;
        this.name = name;
        this.isSelected = isSelected;
        this.creationDate = creationDate;
        this.lastUpdate = lastUpdate;
    }

    public ItemTagDto(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
    }

    public ItemTagDto() {

    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
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
