package it.pioppi.business.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import it.pioppi.business.dto.item.ItemDto;
import it.pioppi.business.dto.item.tag.ItemTagDto;
import it.pioppi.business.dto.item.detail.ItemDetailDto;
import it.pioppi.business.dto.item.quantity.QuantityTypeDto;
import it.pioppi.database.model.ItemStatus;
import it.pioppi.utils.ConstantUtils;

public class GeneralItemViewModel extends ViewModel {

    private String ascendingOrder = "ASC";
    private Boolean filterCheckedOnly = null;
    private ItemStatus filterStatus = null;

    private List<ItemDto> originalItems = new ArrayList<>();

    private final MutableLiveData<List<ItemDto>> items = new MutableLiveData<>();
    private final MutableLiveData<String> barcodeScanned = new MutableLiveData<>("");
    private final MediatorLiveData<List<ItemDto>> filteredItems = new MediatorLiveData<>();

    // Gestione degli ItemTags e degli ItemDetails
    private final MutableLiveData<List<ItemTagDto>> itemTags = new MutableLiveData<>();
    private final MutableLiveData<List<ItemTagDto>> allItemTags = new MutableLiveData<>();

    private final MutableLiveData<List<ItemDetailDto>> itemDetails = new MutableLiveData<>();
    private final MutableLiveData<Map<UUID, Set<UUID>>> itemTagJoins = new MutableLiveData<>();
    private final MutableLiveData<List<QuantityTypeDto>> quantityTypes = new MutableLiveData<>();

    private UUID lastVisitedItemId;

    public GeneralItemViewModel() {
        filteredItems.addSource(items, itemList -> applyFilter());
        filteredItems.addSource(barcodeScanned, q -> applyFilter());
    }

    // Metodi per Items e filtraggio
    public LiveData<List<ItemDto>> getItems() {
        return items;
    }

    // Imposta la lista completa e aggiorna la lista filtrata
    public void setItems(List<ItemDto> itemList) {
        originalItems = new ArrayList<>(itemList);
        items.setValue(itemList);
    }

    public LiveData<List<ItemDto>> getFilteredItems() {
        return filteredItems;
    }

    public void setQuery(String query) {
        this.barcodeScanned.setValue(query);
    }

    private void applyFilter() {
        List<ItemDto> filteredList = new ArrayList<>(originalItems);

        String q = barcodeScanned.getValue();
        if (q != null && !q.isEmpty()) {
            filteredList = filteredList.stream()
                    .filter(item -> (item.getName() != null && item.getName().toLowerCase().contains(q.toLowerCase()))
                            || (item.getBarcode() != null && item.getBarcode().toLowerCase().contains(q.toLowerCase())))
                    .collect(Collectors.toList());
        }

        if (filterCheckedOnly != null && filterCheckedOnly) {
            filteredList = filteredList.stream()
                    .filter(ItemDto::isChecked)
                    .collect(Collectors.toList());
        }

        if (filterStatus != null) {
            filteredList = filteredList.stream()
                    .filter(item -> item.getStatus() != null && item.getStatus().equals(filterStatus))
                    .collect(Collectors.toList());
        }

        if (ConstantUtils.SORTING_DESCENDING.equals(ascendingOrder)) {
            filteredList.sort(Comparator.comparing(ItemDto::getName, String.CASE_INSENSITIVE_ORDER).reversed());
        } else {
            filteredList.sort(Comparator.comparing(ItemDto::getName, String.CASE_INSENSITIVE_ORDER));
        }

        filteredItems.setValue(filteredList);
    }

    public void setFilterCheckedOnly(Boolean filterCheckedOnly) {
        this.filterCheckedOnly = filterCheckedOnly;
        applyFilter();
    }

    public void setFilterStatus(ItemStatus filterStatus) {
        this.filterStatus = filterStatus;
        applyFilter();
    }

    public void setSortOrder(String sortOrder) {
        ascendingOrder = sortOrder;
        applyFilter();
    }

    public void removeItem(ItemDto item) {
        List<ItemDto> current = items.getValue();
        if (current != null) {
            List<ItemDto> updated = new ArrayList<>(current);
            updated.remove(item);
            setItems(updated);
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

    public LiveData<List<ItemTagDto>> getAllItemTags() {
        return allItemTags;
    }

    public void setAllItemTags(List<ItemTagDto> allItemTags) {
        this.allItemTags.setValue(allItemTags);
    }

    // Metodi per ItemDetails
    public LiveData<List<ItemDetailDto>> getItemDetails() {
        return itemDetails;
    }

    public void setItemDetails(List<ItemDetailDto> details) {
        itemDetails.setValue(details);
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

    // Metodi per QuantityTypes
    public LiveData<List<QuantityTypeDto>> getQuantityTypes() {
        return quantityTypes;
    }

    public void setQuantityTypes(List<QuantityTypeDto> types) {
        quantityTypes.setValue(types);
    }

    public void updateQuantityTypes(List<QuantityTypeDto> finalQuantityTypes) {

        List<QuantityTypeDto> currentTypes = quantityTypes.getValue();
        if (currentTypes != null) {
            for (QuantityTypeDto type : finalQuantityTypes) {
                updateQuantityType(type);
            }
        } else {
            quantityTypes.setValue(finalQuantityTypes);
        }

    }

    public void updateQuantityType(QuantityTypeDto updatedType) {
        List<QuantityTypeDto> currentTypes = quantityTypes.getValue();
        if (currentTypes != null) {
            boolean found = false;
            for (int i = 0; i < currentTypes.size(); i++) {
                if (currentTypes.get(i).getId().equals(updatedType.getId())) {
                    currentTypes.set(i, updatedType);
                    found = true;
                    break;
                }
            }
            if (!found) {
                currentTypes.add(updatedType);
            }
            quantityTypes.setValue(currentTypes);
        }
    }

    // Metodi per la gestione delle relazioni ItemTagJoins
    public void setItemTagJoins(Map<UUID, Set<UUID>> itemTagJoins) {
        this.itemTagJoins.setValue(itemTagJoins);
    }

    public Map<UUID, Set<UUID>> getItemTagJoins() {
        return itemTagJoins.getValue();
    }

    public void updateItemImageUrl(UUID itemId, String imageUrl) {
        Objects.requireNonNull(items.getValue()).stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .ifPresent(item -> item.setImageUrl(imageUrl));
    }

    public void setLastVisitedItemId(UUID lastVisitedItemId) {
        this.lastVisitedItemId = lastVisitedItemId;
    }

    public UUID getLastVisitedItemId() {
        return lastVisitedItemId;
    }

}
