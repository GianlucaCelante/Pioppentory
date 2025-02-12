package it.pioppi.business.dto;

public class ItemWithProviderDto {

    private ItemDto item;
    private ProviderDto provider;

    public ItemDto getItem() {
        return item;
    }

    public void setItem(ItemDto item) {
        this.item = item;
    }

    public ProviderDto getProvider() {
        return provider;
    }

    public void setProvider(ProviderDto provider) {
        this.provider = provider;}
}
