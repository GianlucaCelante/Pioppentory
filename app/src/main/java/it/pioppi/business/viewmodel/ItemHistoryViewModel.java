package it.pioppi.business.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import it.pioppi.business.dto.ItemHistoryDto;
import it.pioppi.business.dto.ItemHistoryGroupDto;
import it.pioppi.database.AppDatabase;
import it.pioppi.database.entity.ItemHistoryEntity;
import it.pioppi.database.mapper.EntityDtoMapper;

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
        List<ItemHistoryDto> allItemHistories = fetchAllItemHistories();

        Map<LocalDate, List<ItemHistoryDto>> groupedMap = allItemHistories.stream()
                .collect(Collectors.groupingBy(ItemHistoryDto::getInventoryClosureDate));

        List<ItemHistoryGroupDto> groupList = new ArrayList<>();
        for (Map.Entry<LocalDate, List<ItemHistoryDto>> entry : groupedMap.entrySet()) {
            groupList.add(new ItemHistoryGroupDto(entry.getKey(), entry.getValue()));
        }

        groupList.sort(Comparator.comparing(ItemHistoryGroupDto::getInventoryClosureDate).reversed());

        return groupList;
    }

    private List<ItemHistoryDto> fetchAllItemHistories() {
        List<ItemHistoryEntity> allItemHistory = appDatabase.itemHistoryEntityDao().getAllItemHistory();
        return EntityDtoMapper.mapItemHistoryEntityListToDtoList(allItemHistory);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}