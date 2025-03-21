package it.pioppi.business.dto.item;

import java.util.List;

import it.pioppi.business.dto.item.detail.ItemDetailDto;
import it.pioppi.business.dto.item.quantity.QuantityTypeDto;

public class ItemWithDetailAndQuantityTypeDto {

    private ItemDto item;
    private ItemDetailDto itemDetail;
    private List<QuantityTypeDto> quantityTypes;

    public ItemDto getItem() {
        return item;
    }

    public void setItem(ItemDto item) {
        this.item = item;
    }

    public ItemDetailDto getItemDetail() {
        return itemDetail;
    }

    public void setItemDetail(ItemDetailDto itemDetail) {
        this.itemDetail = itemDetail;
    }

    public List<QuantityTypeDto> getQuantityTypes() {
        return quantityTypes;
    }

    public void setQuantityTypes(List<QuantityTypeDto> quantityTypes) {
        this.quantityTypes = quantityTypes;
    }
}
