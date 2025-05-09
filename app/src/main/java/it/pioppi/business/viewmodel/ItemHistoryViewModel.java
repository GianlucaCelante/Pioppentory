package it.pioppi.business.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import it.pioppi.business.dto.history.ItemHistoryDto;
import it.pioppi.business.dto.history.ItemHistoryGroupDto;
import it.pioppi.business.dto.item.quantity.QuantityTypeDto;
import it.pioppi.database.AppDatabase;
import it.pioppi.database.entity.ItemHistoryEntity;
import it.pioppi.database.entity.QuantityTypeEntity;
import it.pioppi.database.mapper.EntityDtoMapper;
import it.pioppi.database.model.QuantityPurpose;

public class ItemHistoryViewModel extends AndroidViewModel {

    private final MutableLiveData<List<ItemHistoryGroupDto>> itemHistoryGroups = new MutableLiveData<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final AppDatabase appDatabase;

    public ItemHistoryViewModel(@NonNull Application application) {
        super(application);
        appDatabase = AppDatabase.getInstance(application.getApplicationContext());
        loadItemHistoryGroups();
    }

    public LiveData<List<ItemHistoryGroupDto>> getItemHistoryGroups() {
        return itemHistoryGroups;
    }

    private void loadItemHistoryGroups() {
        executorService.execute(() -> {
            List<ItemHistoryGroupDto> groupList = prepareData();
            itemHistoryGroups.postValue(groupList);
        });
    }

    private List<ItemHistoryGroupDto> prepareData() {
        List<ItemHistoryDto> allItemHistories = fetchAllItemHistoriesWithQuantities();

        Map<LocalDate, List<ItemHistoryDto>> groupedMap = allItemHistories.stream()
                .collect(Collectors.groupingBy(ItemHistoryDto::getInventoryClosureDate));

        List<ItemHistoryGroupDto> groupList = new ArrayList<>();
        for (Map.Entry<LocalDate, List<ItemHistoryDto>> entry : groupedMap.entrySet()) {
            groupList.add(new ItemHistoryGroupDto(entry.getKey(), entry.getValue()));
        }

        groupList.sort(Comparator.comparing(ItemHistoryGroupDto::getInventoryClosureDate).reversed());

        return groupList;
    }

    private List<ItemHistoryDto> fetchAllItemHistoriesWithQuantities() {
        // 1) prendo tutti gli history
        List<ItemHistoryEntity> histories = appDatabase
                .itemHistoryEntityDao()
                .getAllItemHistory();

        // 2) prendo tutte le quantityTypes, le mappo in DTO e le raggruppo per itemId
        Map<UUID, List<QuantityTypeDto>> qtyByItemId = appDatabase
                .quantityTypeEntityDao()
                .getAll()                                  // List<QuantityTypeEntity>
                .stream() // filtro solo le disponibili
                .filter(qty -> qty.getPurpose().equals(QuantityPurpose.AVAILABLE))
                .map(EntityDtoMapper::entityToDto)
                .collect(Collectors.groupingBy(QuantityTypeDto::getItemId));

        // 3) per ogni historyEntity costruisco l’ItemHistoryDto e ci appendo le quantità
        return histories.stream().map(history -> {
            ItemHistoryDto dto = EntityDtoMapper.mapItemHistoryEntityToDto(history);
            // recupera la lista (o vuota se non c’è)
            List<QuantityTypeDto> list = qtyByItemId.getOrDefault(history.getItemId(), Collections.emptyList());
            dto.setQuantityTypes(list);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}