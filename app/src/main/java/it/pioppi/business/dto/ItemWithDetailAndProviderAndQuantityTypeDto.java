package it.pioppi.business.dto;

import java.util.List;

public class ItemWithDetailAndProviderAndQuantityTypeDto {

    private ItemDto item;
    private ItemDetailDto itemDetail;
    private ProviderDto provider;
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

    public ProviderDto getProvider() {
        return provider;
    }

    public void setProvider(ProviderDto provider) {
        this.provider = provider;
    }

    public List<QuantityTypeDto> getQuantityTypes() {
        return quantityTypes;
    }

    public void setQuantityTypes(List<QuantityTypeDto> quantityTypes) {
        this.quantityTypes = quantityTypes;
    }
}
