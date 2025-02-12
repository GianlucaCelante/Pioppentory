package it.pioppi.business.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.pioppi.business.dto.ItemWithProviderDto;
import it.pioppi.database.AppDatabase;
import it.pioppi.database.mapper.EntityDtoMapper;
import it.pioppi.database.entity.ItemWithProviderEntity;

public class ItemWithProviderViewModel extends ViewModel {
    private final MutableLiveData<List<ItemWithProviderDto>> itemsWithProviders = new MutableLiveData<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public LiveData<List<ItemWithProviderDto>> getItemsWithProviders() {
        return itemsWithProviders;
    }

    public void setItemsWithProviders(List<ItemWithProviderDto> itemsWithProvidersList) {
        itemsWithProviders.setValue(itemsWithProvidersList);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
