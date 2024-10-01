package it.pioppi.business.dto;

import java.time.LocalDate;
import java.util.List;

public class ItemHistoryGroup {
    private LocalDate inventoryClosureDate;
    private List<ItemHistoryDto> itemHistories;

    public ItemHistoryGroup(LocalDate date, List<ItemHistoryDto> histories) {
        this.inventoryClosureDate = date;
        this.itemHistories = histories;
    }

    public LocalDate getInventoryClosureDate() {
        return inventoryClosureDate;
    }

    public void setInventoryClosureDate(LocalDate inventoryClosureDate) {
        this.inventoryClosureDate = inventoryClosureDate;
    }

    public List<ItemHistoryDto> getItemHistories() {
        return itemHistories;
    }

    public void setItemHistories(List<ItemHistoryDto> itemHistories) {
        this.itemHistories = itemHistories;
    }


}

