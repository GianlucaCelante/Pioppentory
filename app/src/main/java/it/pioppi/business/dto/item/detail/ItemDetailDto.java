package it.pioppi.business.dto.item.detail;

import java.time.ZonedDateTime;
import java.util.UUID;

public class ItemDetailDto {

    private UUID id;
    private Long quantityToBeOrdered;
    private Integer orderedQuantity;
    private Integer portionsRequiredOnSaturday;
    private Integer portionsRequiredOnSunday;
    private Integer portionsPerWeekend;
    private Integer portionsOnHoliday;
    private Integer maxPortionsSold;
    private ZonedDateTime deliveryDate;
    private ZonedDateTime creationDate;
    private ZonedDateTime lastUpdateDate;
    private UUID itemId;

    public ItemDetailDto(UUID id, Long quantityToBeOrdered, Integer orderedQuantity, Integer portionsRequiredOnSaturday, Integer portionsRequiredOnSunday, Integer portionsPerWeekend, Integer portionsOnHoliday, Integer maxPortionsSold, ZonedDateTime deliveryDate, ZonedDateTime creationDate, ZonedDateTime lastUpdateDate, UUID itemId) {
        this.id = id;
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

    public Long getQuantityToBeOrdered() {
        return quantityToBeOrdered;
    }

    public void setQuantityToBeOrdered(Long quantityToBeOrdered) {
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

    public ZonedDateTime getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(ZonedDateTime deliveryDate) {
        this.deliveryDate = deliveryDate;
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

    public UUID getItemId() {
        return itemId;
    }

    public void setItemId(UUID itemId) {
        this.itemId = itemId;
    }
}
