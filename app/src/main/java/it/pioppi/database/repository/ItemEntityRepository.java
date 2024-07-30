package it.pioppi.database.repository;

import android.app.Application;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.pioppi.database.AppDatabase;
import it.pioppi.database.dao.ItemEntityDao;
import it.pioppi.database.dao.ItemFTSEntityDao;
import it.pioppi.database.model.entity.ItemEntity;
import it.pioppi.database.model.entity.ItemFTSEntity;

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

    public void insert(ItemEntity item) {
        executorService.execute(() -> {
            itemEntityDao.insert(item);
            Integer nextId = itemFTSEntityDao.getNextId();
            itemFTSEntityDao.insertItemFTS(nextId);
        });
    }

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
