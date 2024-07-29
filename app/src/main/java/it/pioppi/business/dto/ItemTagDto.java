package it.pioppi.business.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class ItemTagDto {

    private UUID id;
    private String name;
    private boolean isSelected;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdate;

    public ItemTagDto(UUID id, String name, boolean isSelected, LocalDateTime creationDate, LocalDateTime lastUpdate) {
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
