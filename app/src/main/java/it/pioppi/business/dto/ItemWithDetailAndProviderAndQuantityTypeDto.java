package it.pioppi.business.dto;

import java.util.List;

public class ItemWithDetailAndProviderAndQuantityTypeDto {

    private ItemDto item;
    private ItemDetailDto itemDetail;
    private ProviderDto provider;
    private List<QuantityTypeDto> quantityType;

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

    public ProviderDto getProvider() {
        return provider;
    }

    public void setProvider(ProviderDto provider) {
        this.provider = provider;
    }

    public List<QuantityTypeDto> getQuantityType() {
        return quantityType;
    }

    public void setQuantityType(List<QuantityTypeDto> quantityType) {
        this.quantityType = quantityType;
    }
}
