package it.pioppi.business.viewmodel;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.pioppi.business.dto.ItemWithDetailAndProviderAndQuantityTypeDto;
import it.pioppi.database.AppDatabase;
import it.pioppi.database.dao.ItemEntityDao;
import it.pioppi.database.mapper.EntityDtoMapper;
import it.pioppi.database.model.entity.ItemWithDetailAndProviderAndQuantityTypeEntity;

public class ItemDetailViewModel extends ViewModel {

    private MutableLiveData<ItemWithDetailAndProviderAndQuantityTypeDto> itemLiveData = new MutableLiveData<>();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private AppDatabase appDatabase;

    public ItemDetailViewModel(Application application) {
        appDatabase = AppDatabase.getInstance(application);
    }

    public LiveData<ItemWithDetailAndProviderAndQuantityTypeDto> getItemLiveData() {
        return itemLiveData;
    }

    public void fetchItemById(UUID itemId) {
        executorService.submit(() -> {
            ItemEntityDao entityDao = appDatabase.itemEntityDao();
            ItemWithDetailAndProviderAndQuantityTypeEntity entity = entityDao.getItemsWithDetailsAndProviderAndQuantityType(itemId);

            if (entity == null || entity.item == null) {
                // Gestisci errore
                return;
            }

            ItemWithDetailAndProviderAndQuantityTypeDto dto = new ItemWithDetailAndProviderAndQuantityTypeDto();
            dto.setItem(EntityDtoMapper.entityToDto(entity.item));
            dto.setItemDetail(entity.itemDetail != null ? EntityDtoMapper.detailEntityToDto(entity.itemDetail) : null);
            dto.setProvider(entity.provider != null ? EntityDtoMapper.entityToDto(entity.provider) : null);
            dto.setQuantityTypes(entity.quantityTypes != null ? EntityDtoMapper.entitiesToDtosForQuantityTypes(entity.quantityTypes) : new ArrayList<>());

            itemLiveData.postValue(dto);
        });
    }

}

