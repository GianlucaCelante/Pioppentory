package it.pioppi.database.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.room.Transaction;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import it.pioppi.business.dto.item.ItemDto;
import it.pioppi.database.AppDatabase;
import it.pioppi.database.dao.ItemEntityDao;
import it.pioppi.database.dao.ItemFTSEntityDao;
import it.pioppi.database.entity.ItemEntity;
import it.pioppi.database.entity.ItemFTSEntity;
import it.pioppi.database.mapper.EntityDtoMapper;
import it.pioppi.database.model.ItemStatus;
import it.pioppi.utils.LoggerManager;

public class ItemEntityRepository {
    private final ItemEntityDao itemEntityDao;
    private final ItemFTSEntityDao itemFTSEntityDao;
    private final ExecutorService executorService;

    public ItemEntityRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        itemEntityDao = db.itemEntityDao();
        itemFTSEntityDao = db.itemFTSEntityDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    @Transaction
    public void insert(ItemEntity item) throws ExecutionException, InterruptedException {
        executorService.execute(() -> {
            Integer nextId = itemFTSEntityDao.getNextId();
            itemFTSEntityDao.insertItemFTS(nextId);
            item.setFtsId(nextId);
            itemEntityDao.insert(item);
        });
    }

    @Transaction
    public void update(ItemEntity item) {
        executorService.execute(() -> {
            itemEntityDao.update(item);
            ItemFTSEntity itemFTSEntity = new ItemFTSEntity();
            itemFTSEntity.setId(item.getFtsId());
            itemFTSEntity.setName(item.getName());
            itemFTSEntity.setBarcode(item.getBarcode());
            itemFTSEntityDao.update(itemFTSEntity);
        });
    }

    @Transaction
    public void delete(ItemEntity item) {
        executorService.execute(() -> {
            itemEntityDao.delete(item);
            ItemFTSEntity itemFTSEntity = new ItemFTSEntity();
            itemFTSEntity.setId(item.getFtsId());
            itemFTSEntity.setName(item.getName());
            itemFTSEntity.setBarcode(item.getBarcode());
            itemFTSEntityDao.delete(itemFTSEntity);
        });
    }

    public LiveData<List<ItemDto>> getFilteredItems(String query, Boolean checkedOnly, ItemStatus status, boolean ascending) {
        LiveData<List<ItemEntity>> filteredItems = itemEntityDao.getFilteredItems(query, checkedOnly, status, ascending ? 1 : 0);
        return new LiveData<List<ItemDto>>() {
            @Override
            protected void onActive() {
                super.onActive();
                filteredItems.observeForever(itemEntities -> {
                    List<ItemDto> itemDtoList = EntityDtoMapper.dtosToEntitiesForItemDto(itemEntities);
                    setValue(itemDtoList);
                });
            }
        };
    }

    public boolean updateItemImageUrl(UUID itemId, String imageUrl) {
        Future<Boolean> future = executorService.submit(() -> {
            itemEntityDao.updateItemImageUrl(itemId, imageUrl);
            return true;
        });
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            LoggerManager.getInstance().logException(e);
            return false;
        }
    }
}
