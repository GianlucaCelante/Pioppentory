package it.pioppi.business.dto.history;

import java.time.LocalDate;
import java.util.List;

public class ItemHistoryGroupDto {
    private LocalDate inventoryClosureDate;
    private List<ItemHistoryDto> itemHistories;

    public ItemHistoryGroupDto(LocalDate date, List<ItemHistoryDto> histories) {
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

