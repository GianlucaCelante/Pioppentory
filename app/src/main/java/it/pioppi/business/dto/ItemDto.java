package it.pioppi.business.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import it.pioppi.database.model.QuantityType;
import it.pioppi.database.model.entity.ItemStatus;

public class ItemDto {

    private UUID id;
    private String name;
    private Integer totPortions;
    private ItemStatus status;
    private String barcode;
    private LocalDateTime checkDate;
    private boolean hasNote;
    private UUID providerId;
    private UUID itemDetailId;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdateDate;

    public ItemDto(UUID id, String name, Integer totPortions, ItemStatus status, String barcode, LocalDateTime checkDate, boolean hasNote, UUID providerId, UUID itemDetailId, LocalDateTime creationDate, LocalDateTime lastUpdateDate) {
        this.id = id;
        this.name = name;
        this.totPortions = totPortions;
        this.status = status;
        this.barcode = barcode;
        this.checkDate = checkDate;
        this.hasNote = hasNote;
        this.providerId = providerId;
        this.itemDetailId = itemDetailId;
        this.creationDate = creationDate;
        this.lastUpdateDate = lastUpdateDate;
    }

    public ItemDto() {}

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

    public Integer getTotPortions() {
        return totPortions;
    }

    public void setTotPortions(Integer totPortions) {
        this.totPortions = totPortions;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public LocalDateTime getCheckDate() {
        return checkDate;
    }

    public void setCheckDate(LocalDateTime checkDate) {
        this.checkDate = checkDate;
    }

    public boolean hasNote() {
        return hasNote;
    }

    public void setHasNote(boolean hasNote) {
        this.hasNote = hasNote;
    }

    public UUID getProviderId() {
        return providerId;
    }

    public void setProviderId(UUID providerId) {
        this.providerId = providerId;
    }

    public UUID getItemDetailId() {
        return itemDetailId;
    }

    public void setItemDetailId(UUID itemDetailId) {
        this.itemDetailId = itemDetailId;
    }

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
