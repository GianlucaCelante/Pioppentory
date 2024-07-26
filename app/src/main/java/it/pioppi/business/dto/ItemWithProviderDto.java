package it.pioppi.business.dto;

import java.util.List;

public class ItemWithProviderDto {

    private ItemDto item;
    private List<ProviderDto> providers;

    public ItemDto getItem() {
        return item;
    }

    public void setItem(ItemDto item) {
        this.item = item;
    }

    public List<ProviderDto> getProviders() {
        return providers;
    }

    public void setProviders(List<ProviderDto> provider) {
        this.providers = provider;}
}
