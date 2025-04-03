package it.pioppi.business.dto.item.quantity;

import java.time.ZonedDateTime;
import java.util.UUID;

import it.pioppi.database.model.QuantityPurpose;
import it.pioppi.database.model.QuantityTypeEnum;

public class QuantityTypeDto {

    private UUID id;
    private QuantityTypeEnum quantityTypeEnum;
    private String description;
    private Integer quantity;
    private QuantityPurpose purpose;
    private Integer unitsPerQuantityType;
    private ZonedDateTime creationDate;
    private ZonedDateTime lastUpdateDate;
    private UUID itemId;

    public QuantityTypeDto(UUID id, QuantityTypeEnum quantityTypeEnum, String description,
                           Integer quantity, QuantityPurpose purpose, Integer unitsPerQuantityType,
                           ZonedDateTime creationDate, ZonedDateTime lastUpdateDate, UUID itemId) {
        this.id = id;
        this.quantityTypeEnum = quantityTypeEnum;
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

    public QuantityTypeEnum getQuantityType() {
        return quantityTypeEnum;
    }

    public void setQuantityType(QuantityTypeEnum quantityTypeEnum) {
        this.quantityTypeEnum = quantityTypeEnum;
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

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(ZonedDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public ZonedDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(ZonedDateTime lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setItemId(UUID itemId) {
        this.itemId = itemId;
    }
}

