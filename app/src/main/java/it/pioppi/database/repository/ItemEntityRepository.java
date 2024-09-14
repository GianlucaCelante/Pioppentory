package it.pioppi.database.repository;

import android.app.Application;

import androidx.room.Transaction;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import it.pioppi.database.AppDatabase;
import it.pioppi.database.dao.ItemEntityDao;
import it.pioppi.database.dao.ItemFTSEntityDao;
import it.pioppi.database.entity.ItemEntity;
import it.pioppi.database.entity.ItemFTSEntity;

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

}
