package it.pioppi.database.repository;

import android.app.Application;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.pioppi.database.AppDatabase;
import it.pioppi.database.dao.ItemEntityDao;
import it.pioppi.database.dao.ItemFTSEntityDao;
import it.pioppi.database.model.entity.ItemEntity;

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
            itemFTSEntityDao.insertItemFTS(item.getFtsId());
        });
    }

    public void delete(ItemEntity item) {
        executorService.execute(() -> {
            itemEntityDao.delete(item);
            itemFTSEntityDao.insertItemFTS(item.getFtsId());
        });
    }

}
