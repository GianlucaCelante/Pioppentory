package it.pioppi.business.dto;

public class ItemWithDetailAndProviderDto {

    private ItemDto item;
    private ItemDetailDto itemDetail;
    private ProviderDto provider;

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
}
