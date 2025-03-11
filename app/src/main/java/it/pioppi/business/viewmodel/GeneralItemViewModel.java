package it.pioppi.business.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import it.pioppi.business.dto.ItemDto;
import it.pioppi.business.dto.ItemTagDto;
import it.pioppi.business.dto.ItemDetailDto;
import it.pioppi.business.dto.QuantityTypeDto;

public class GeneralItemViewModel extends ViewModel {

    private final MutableLiveData<List<ItemDto>> items = new MutableLiveData<>();
    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final MediatorLiveData<List<ItemDto>> filteredItems = new MediatorLiveData<>();

    // Gestione degli ItemTags e degli ItemDetails
    private final MutableLiveData<List<ItemTagDto>> itemTags = new MutableLiveData<>();
    private final MutableLiveData<List<ItemTagDto>> allItemTags = new MutableLiveData<>();

    private final MutableLiveData<List<ItemDetailDto>> itemDetails = new MutableLiveData<>();
    private final MutableLiveData<Map<UUID, Set<UUID>>> itemTagJoins = new MutableLiveData<>();
    private final MutableLiveData<List<QuantityTypeDto>> quantityTypes = new MutableLiveData<>();

    public GeneralItemViewModel() {
        filteredItems.addSource(items, itemList -> applyFilter());
        filteredItems.addSource(query, q -> applyFilter());
    }

    // Metodi per Items e filtraggio
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

    // Metodi per ItemTags
    public LiveData<List<ItemTagDto>> getItemTags() {
        return itemTags;
    }

    public void setItemTags(List<ItemTagDto> tags) {
        itemTags.setValue(tags);
    }

    public void updateItemTag(ItemTagDto updatedTag) {
        List<ItemTagDto> currentTags = itemTags.getValue();
        if (currentTags != null) {
            for (int i = 0; i < currentTags.size(); i++) {
                if (currentTags.get(i).getId().equals(updatedTag.getId())) {
                    currentTags.set(i, updatedTag);
                    itemTags.setValue(currentTags);
                    break;
                }
            }
        }
    }

    // Metodi per ItemDetails (relativi ad ItemTagDto, se necessari)
    public LiveData<List<ItemDetailDto>> getItemDetails() {
        return itemDetails;
    }

    public void setItemDetails(List<ItemDetailDto> details) {
        itemDetails.setValue(details);
    }

    public LiveData<List<QuantityTypeDto>> getQuantityTypes() {
        return quantityTypes;
    }

    public void setQuantityTypes(List<QuantityTypeDto> types) {
        quantityTypes.setValue(types);
    }

    public void updateItemDetail(ItemDetailDto updatedDetail) {
        List<ItemDetailDto> currentDetails = itemDetails.getValue();
        if (currentDetails != null) {
            for (int i = 0; i < currentDetails.size(); i++) {
                if (currentDetails.get(i).getId().equals(updatedDetail.getId())) {
                    currentDetails.set(i, updatedDetail);
                    itemDetails.setValue(currentDetails);
                    break;
                }
            }
        }
    }

    public void updateQuantityType(QuantityTypeDto updatedType) {
        List<QuantityTypeDto> currentTypes = quantityTypes.getValue();
        if (currentTypes != null) {
            for (int i = 0; i < currentTypes.size(); i++) {
                if (currentTypes.get(i).getId().equals(updatedType.getId())) {
                    currentTypes.set(i, updatedType);
                    quantityTypes.setValue(currentTypes);
                    break;
                }
            }
        }
    }

    public void setItemTagJoins(Map<UUID, Set<UUID>> itemTagJoins) {
        this.itemTagJoins.setValue(itemTagJoins);
    }

    public Map<UUID, Set<UUID>> getItemTagJoins() {
        return itemTagJoins.getValue();
    }

    public boolean itemBelongsToTag(UUID itemId, UUID tagId) {
        Set<UUID> itemsForTag = itemTagJoins.getValue().get(tagId);
        return itemsForTag != null && itemsForTag.contains(itemId);
    }

    public LiveData<List<ItemTagDto>> getAllItemTags() {
        return allItemTags;
    }

    public void setAllItemTags(List<ItemTagDto> allItemTags) {
        this.allItemTags.setValue(allItemTags);
    }
}
