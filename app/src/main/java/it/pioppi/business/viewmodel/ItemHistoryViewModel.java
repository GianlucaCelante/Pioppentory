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
import it.pioppi.business.dto.ItemHistoryGroup;
import it.pioppi.database.AppDatabase;
import it.pioppi.database.entity.ItemHistoryEntity;
import it.pioppi.database.mapper.EntityDtoMapper;

public class ItemHistoryViewModel extends AndroidViewModel {

    private MutableLiveData<List<ItemHistoryGroup>> itemHistoryGroups = new MutableLiveData<>();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private AppDatabase appDatabase;

    public ItemHistoryViewModel(@NonNull Application application) {
        super(application);
        appDatabase = AppDatabase.getInstance(application.getApplicationContext());
        loadItemHistoryGroups();
    }

    public LiveData<List<ItemHistoryGroup>> getItemHistoryGroups() {
        return itemHistoryGroups;
    }

    private void loadItemHistoryGroups() {
        executorService.execute(() -> {
            List<ItemHistoryGroup> groupList = prepareData();
            itemHistoryGroups.postValue(groupList);
        });
    }

    private List<ItemHistoryGroup> prepareData() {
        // Stesso codice di prima
        List<ItemHistoryDto> allItemHistories = fetchAllItemHistories();

        Map<LocalDate, List<ItemHistoryDto>> groupedMap = allItemHistories.stream()
                .collect(Collectors.groupingBy(ItemHistoryDto::getInventoryClosureDate));

        List<ItemHistoryGroup> groupList = new ArrayList<>();
        for (Map.Entry<LocalDate, List<ItemHistoryDto>> entry : groupedMap.entrySet()) {
            groupList.add(new ItemHistoryGroup(entry.getKey(), entry.getValue()));
        }

        groupList.sort(Comparator.comparing(ItemHistoryGroup::getInventoryClosureDate).reversed());

        return groupList;
    }

    private List<ItemHistoryDto> fetchAllItemHistories() {
        List<ItemHistoryEntity> allItemHistory = appDatabase.itemHistoryEntityDao().getAllItemHistory();
        return EntityDtoMapper.mapItemHistoryEntityListToDtoList(allItemHistory);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Chiudi l'ExecutorService quando il ViewModel viene distrutto
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}