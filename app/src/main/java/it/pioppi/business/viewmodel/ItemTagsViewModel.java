package it.pioppi.business.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import it.pioppi.business.dto.ItemTagDto;

public class ItemTagsViewModel extends ViewModel {
    private final MutableLiveData<List<ItemTagDto>> itemTags = new MutableLiveData<>();

    public LiveData<List<ItemTagDto>> getItemTags() {
        return itemTags;
    }

    public void setItemTags(List<ItemTagDto> itemTagDtos) {
        this.itemTags.setValue(itemTagDtos);
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
