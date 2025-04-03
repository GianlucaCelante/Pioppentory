package it.pioppi.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Upsert;

import java.util.List;
import java.util.UUID;

import it.pioppi.database.entity.ItemHistoryEntity;

@Dao
public interface ItemHistoryEntityDao extends BaseDao<ItemHistoryEntity> {

    @Override
    @Update
    void update(ItemHistoryEntity entity);

    @Override
    @Delete
    void delete(ItemHistoryEntity entity);

    @Override
    @Insert
    void insert(ItemHistoryEntity entity);

    @Override
    @Upsert
    void upsert(ItemHistoryEntity entity);

    @Query("SELECT * FROM item_history WHERE id = :id")
    ItemHistoryEntity getItemHistoryById(UUID id);

    @Query("SELECT * FROM item_history")
    List<ItemHistoryEntity> getAllItemHistory();

    @Query("SELECT * FROM item_history WHERE id = :itemId")
    List<ItemHistoryEntity> getItemHistoryByItemId(UUID itemId);

    @Query("SELECT * FROM item_history WHERE item_name = :itemName AND inventory_closure_date = :closureDate")
    ItemHistoryEntity getItemHistoryByItemNameInClosureDate(String itemName, String closureDate);
}
