package it.pioppi.business.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import it.pioppi.database.model.QuantityPurpose;
import it.pioppi.database.model.QuantityType;

public class QuantityTypeDto {

    private UUID id;
    private QuantityType quantityType;
    private String description;
    private Integer quantity;
    private QuantityPurpose purpose;
    private Integer unitsPerQuantityType;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdateDate;
    private UUID itemId;

    public QuantityTypeDto(UUID id, QuantityType quantityType, String description,
                           Integer quantity, QuantityPurpose purpose, Integer unitsPerQuantityType,
                           LocalDateTime creationDate, LocalDateTime lastUpdateDate, UUID itemId) {
        this.id = id;
        this.quantityType = quantityType;
        this.description = description;
        this.quantity = quantity;
        this.purpose = purpose;
        this.unitsPerQuantityType = unitsPerQuantityType;
        this.creationDate = creationDate;
        this.lastUpdateDate = lastUpdateDate;
        this.itemId = itemId;
    }

    public QuantityTypeDto() {

    }

    public UUID getId() {
        return id;
    }

    public UUID getItemId() {
        return itemId;
    }

    public QuantityType getQuantityType() {
        return quantityType;
    }

    public void setQuantityType(QuantityType quantityType) {
        this.quantityType = quantityType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public QuantityPurpose getPurpose() {
        return purpose;
    }

    public void setPurpose(QuantityPurpose purpose) {
        this.purpose = purpose;
    }

    public Integer getUnitsPerQuantityType() {
        return unitsPerQuantityType;
    }

    public void setUnitsPerQuantityType(Integer unitsPerQuantityType) {
        this.unitsPerQuantityType = unitsPerQuantityType;
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

    public void setId(UUID id) {
        this.id = id;
    }

    public void setItemId(UUID itemId) {
        this.itemId = itemId;
    }
}

