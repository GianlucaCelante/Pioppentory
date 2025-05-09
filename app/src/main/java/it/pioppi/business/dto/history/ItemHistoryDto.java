package it.pioppi.business.dto.history;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import it.pioppi.business.dto.item.quantity.QuantityTypeDto;

public class ItemHistoryDto {

    private UUID id;
    private String providerName;
    private String itemName;
    private Long quantityPresent;
    private Long quantityOrdered;
    private Long portionsPerWeekend;
    private LocalDate inventoryClosureDate;
    private LocalDate deliveryDate;
    private String barcode;
    private String note;
    private UUID itemId;
    private List<QuantityTypeDto> quantityTypes;

    public ItemHistoryDto(String providerName, String itemName, Long quantityPresent, Long quantityOrdered, Long portionsPerWeekend, LocalDate inventoryClosureDate, LocalDate deliveryDate, String barcode, String note, UUID itemId, List<QuantityTypeDto> quantityTypes) {
        this.providerName = providerName;
        this.itemName = itemName;
        this.quantityPresent = quantityPresent;
        this.quantityOrdered = quantityOrdered;
        this.portionsPerWeekend = portionsPerWeekend;
        this.inventoryClosureDate = inventoryClosureDate;
        this.deliveryDate = deliveryDate;
        this.barcode = barcode;
        this.note = note;
        this.itemId = itemId;
        this.quantityTypes = quantityTypes;

    }

    public ItemHistoryDto() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Long getQuantityPresent() {
        return quantityPresent;
    }

    public void setQuantityPresent(Long quantityPresent) {
        this.quantityPresent = quantityPresent;
    }

    public Long getQuantityOrdered() {
        return quantityOrdered;
    }

    public void setQuantityOrdered(Long quantityOrdered) {
        this.quantityOrdered = quantityOrdered;
    }

    public Long getPortionsPerWeekend() {
        return portionsPerWeekend;
    }

    public void setPortionsPerWeekend(Long portionsPerWeekend) {
        this.portionsPerWeekend = portionsPerWeekend;
    }

    public LocalDate getInventoryClosureDate() {
        return inventoryClosureDate;
    }

    public void setInventoryClosureDate(LocalDate inventoryClosureDate) {
        this.inventoryClosureDate = inventoryClosureDate;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public UUID getItemId() {
        return itemId;
    }

    public void setItemId(UUID itemId) {
        this.itemId = itemId;
    }

    public List<QuantityTypeDto> getQuantityTypes() {
        return quantityTypes;
    }

    public void setQuantityTypes(List<QuantityTypeDto> quantityTypes) {
        this.quantityTypes = quantityTypes;
    }

}
