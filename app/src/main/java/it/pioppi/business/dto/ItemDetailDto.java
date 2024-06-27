package it.pioppi.business.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import it.pioppi.database.model.QuantityType;

public class ItemDetailDto {

    private UUID id;
    private QuantityTypeDto quantityType;
    private Integer quantityToBeOrdered;
    private Integer orderedQuantity;
    private Integer portionsRequiredOnSaturday;
    private Integer portionsRequiredOnSunday;
    private Integer portionsPerWeekend;
    private Integer portionsOnHoliday;
    private Integer maxPortionsSold;
    private LocalDateTime deliveryDate;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdateDate;
    private UUID itemId;

    public ItemDetailDto(UUID id, QuantityTypeDto quantityType, Integer quantityToBeOrdered, Integer orderedQuantity, Integer portionsRequiredOnSaturday, Integer portionsRequiredOnSunday, Integer portionsPerWeekend, Integer portionsOnHoliday, Integer maxPortionsSold, LocalDateTime deliveryDate, LocalDateTime creationDate, LocalDateTime lastUpdateDate, UUID itemId) {
        this.id = id;
        this.quantityType = quantityType;
        this.quantityToBeOrdered = quantityToBeOrdered;
        this.orderedQuantity = orderedQuantity;
        this.portionsRequiredOnSaturday = portionsRequiredOnSaturday;
        this.portionsRequiredOnSunday = portionsRequiredOnSunday;
        this.portionsPerWeekend = portionsPerWeekend;
        this.portionsOnHoliday = portionsOnHoliday;
        this.maxPortionsSold = maxPortionsSold;
        this.deliveryDate = deliveryDate;
        this.creationDate = creationDate;
        this.lastUpdateDate = lastUpdateDate;
        this.itemId = itemId;
    }

    public ItemDetailDto() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public QuantityTypeDto getQuantityType() {
        return quantityType;
    }

    public void setQuantityType(QuantityTypeDto quantityType) {
        this.quantityType = quantityType;
    }

    public Integer getQuantityToBeOrdered() {
        return quantityToBeOrdered;
    }

    public void setQuantityToBeOrdered(Integer quantityToBeOrdered) {
        this.quantityToBeOrdered = quantityToBeOrdered;
    }

    public Integer getOrderedQuantity() {
        return orderedQuantity;
    }

    public void setOrderedQuantity(Integer orderedQuantity) {
        this.orderedQuantity = orderedQuantity;
    }

    public Integer getPortionsRequiredOnSaturday() {
        return portionsRequiredOnSaturday;
    }

    public void setPortionsRequiredOnSaturday(Integer portionsRequiredOnSaturday) {
        this.portionsRequiredOnSaturday = portionsRequiredOnSaturday;
    }

    public Integer getPortionsRequiredOnSunday() {
        return portionsRequiredOnSunday;
    }

    public void setPortionsRequiredOnSunday(Integer portionsRequiredOnSunday) {
        this.portionsRequiredOnSunday = portionsRequiredOnSunday;
    }

    public Integer getPortionsPerWeekend() {
        return portionsPerWeekend;
    }

    public void setPortionsPerWeekend(Integer portionsPerWeekend) {
        this.portionsPerWeekend = portionsPerWeekend;
    }

    public Integer getPortionsOnHoliday() {
        return portionsOnHoliday;
    }

    public void setPortionsOnHoliday(Integer portionsOnHoliday) {
        this.portionsOnHoliday = portionsOnHoliday;
    }

    public Integer getMaxPortionsSold() {
        return maxPortionsSold;
    }

    public void setMaxPortionsSold(Integer maxPortionsSold) {
        this.maxPortionsSold = maxPortionsSold;
    }

    public LocalDateTime getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDateTime deliveryDate) {
        this.deliveryDate = deliveryDate;
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

    public UUID getItemId() {
        return itemId;
    }

    public void setItemId(UUID itemId) {
        this.itemId = itemId;
    }
}
