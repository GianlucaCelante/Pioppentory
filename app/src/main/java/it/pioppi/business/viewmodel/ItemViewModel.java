package it.pioppi.business.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import it.pioppi.business.dto.ItemDto;

public class ItemViewModel extends ViewModel {
    private final MutableLiveData<List<ItemDto>> items = new MutableLiveData<>();

    public LiveData<List<ItemDto>> getItems() {
        return items;
    }

    public void setItems(List<ItemDto> itemList) {
        items.setValue(itemList);
    }

    public void updateItem(ItemDto updatedItem) {
        List<ItemDto> currentItems = items.getValue();
        if (currentItems != null) {
            for (int i = 0; i < currentItems.size(); i++) {
                if (currentItems.get(i).getId().equals(updatedItem.getId())) {
                    currentItems.set(i, updatedItem);
                    items.setValue(currentItems);
                    break;
                }
            }
        }
    }
}
