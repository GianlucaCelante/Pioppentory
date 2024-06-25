package it.pioppi.business.dto;

import java.util.UUID;

import it.pioppi.database.model.QuantityType;

public class QuantityTypeDto {

    private UUID id;
    private QuantityType quantityType;
    private String description;
    private Integer quantity;
    private UUID itemId;

    public QuantityTypeDto(UUID id, QuantityType quantityType, String description, Integer quantity, UUID itemId) {
        this.id = id;
        this.quantityType = quantityType;
        this.description = description;
        this.quantity = quantity;
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

    public void setId(UUID id) {
        this.id = id;
    }

    public void setItemId(UUID itemId) {
        this.itemId = itemId;
    }
}

