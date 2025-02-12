package it.pioppi.business.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;
import java.util.stream.Collectors;

import it.pioppi.business.dto.ItemDto;

public class ItemViewModel extends ViewModel {
    private final MutableLiveData<List<ItemDto>> items = new MutableLiveData<>();
    private final MutableLiveData<String> query = new MutableLiveData<>("");

    private final MediatorLiveData<List<ItemDto>> filteredItems = new MediatorLiveData<>();

    public ItemViewModel() {
        filteredItems.addSource(items, itemList -> applyFilter());
        filteredItems.addSource(query, q -> applyFilter());
    }

    public LiveData<List<ItemDto>> getItems() {
        return items;
    }

    public void setItems(List<ItemDto> itemList) {
        items.setValue(itemList);
    }

    public LiveData<List<ItemDto>> getFilteredItems() {
        return filteredItems;
    }

    public void setQuery(String query) {
        this.query.setValue(query);
    }

    private void applyFilter() {
        List<ItemDto> itemList = items.getValue();
        String q = query.getValue();

        if (itemList == null || q == null || q.isEmpty()) {
            filteredItems.setValue(itemList);
        } else {
            List<ItemDto> filteredList = itemList.stream()
                    .filter(item -> (item.getName() != null && item.getName().toLowerCase().contains(q.toLowerCase())) ||
                            (item.getBarcode() != null && item.getBarcode().toLowerCase().contains(q.toLowerCase())))
                    .collect(Collectors.toList());
            filteredItems.setValue(filteredList);
        }
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
