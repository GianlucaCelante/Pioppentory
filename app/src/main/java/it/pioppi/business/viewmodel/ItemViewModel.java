package it.pioppi.business.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import it.pioppi.business.dto.ItemDto;

public class ItemViewModel extends AndroidViewModel {
    private final MutableLiveData<List<ItemDto>> items = new MutableLiveData<>();

    public ItemViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<ItemDto>> getItems() {
        return items;
    }

    public void setItems(List<ItemDto> items) {
        this.items.setValue(items);
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
