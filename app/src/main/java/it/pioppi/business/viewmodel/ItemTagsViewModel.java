package it.pioppi.business.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import it.pioppi.business.dto.ItemDetailDto;
import it.pioppi.business.dto.ItemDto;
import it.pioppi.business.dto.ItemTagDto;

public class ItemTagsViewModel extends ViewModel {
    private final MutableLiveData<List<ItemTagDto>> itemTags = new MutableLiveData<>();
    private final MutableLiveData<List<ItemDto>> items = new MutableLiveData<>();
    private final MutableLiveData<List<ItemDetailDto>> itemDetails = new MutableLiveData<>();


    public LiveData<List<ItemTagDto>> getItemTags() {
        return itemTags;
    }

    public void setItemTags(List<ItemTagDto> itemTagDtos) {
        this.itemTags.setValue(itemTagDtos);
    }

    public LiveData<List<ItemDto>> getItems() {
        return items;
    }

    public void setItems(List<ItemDto> itemList) {
        items.setValue(itemList);
    }

    public LiveData<List<ItemDetailDto>> getItemDetails() {
        return itemDetails;
    }

    public void setItemDetails(List<ItemDetailDto> itemDetailList) {
        itemDetails.setValue(itemDetailList);
    }

    public void updateItemTag(ItemTagDto updatedItemTag) {
        List<ItemTagDto> currentItems = itemTags.getValue();
        if (currentItems != null) {
            for (int i = 0; i < currentItems.size(); i++) {
                if (currentItems.get(i).getId().equals(updatedItemTag.getId())) {
                    currentItems.set(i, updatedItemTag);
                    itemTags.setValue(currentItems);
                    break;
                }
            }
        }
    }
}
