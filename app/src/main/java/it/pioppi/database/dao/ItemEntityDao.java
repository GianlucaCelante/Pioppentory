package it.pioppi.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;
import androidx.room.Upsert;

import java.util.List;
import java.util.UUID;


import it.pioppi.database.entity.ItemEntity;
import it.pioppi.database.entity.ItemWithDetailAndQuantityTypeEntity;
import it.pioppi.database.entity.ItemWithDetailEntity;

@Dao
public interface ItemEntityDao extends BaseDao<ItemEntity> {

    @Override
    @Update
    void update(ItemEntity entity);

    @Override
    @Delete
    void delete(ItemEntity entity);

    @Override
    @Insert
    void insert(ItemEntity entity);

    @Override
    @Upsert
    void upsert(ItemEntity entity);

    @Query("SELECT * FROM item WHERE id = :itemId")
    ItemEntity getItemById(UUID itemId);

    @Query("SELECT * FROM ITEM")
    List<ItemEntity> getAllItems();

    @Query("SELECT * FROM item WHERE status = :status")
    List<ItemEntity> getItemByStatus(String status);

    @Transaction
    @Query("SELECT * FROM item")
    List<ItemWithDetailEntity> getItemsWithDetails();

    @Transaction
    @Query("SELECT * FROM item WHERE id = :itemId")
    ItemWithDetailEntity getItemWithDetail(UUID itemId);

    @Transaction
    @Query("SELECT * FROM item WHERE id = :itemId")
    ItemWithDetailAndQuantityTypeEntity getItemsWithDetailsAndQuantityType(UUID itemId);

    @Query("SELECT id FROM item WHERE name = :name COLLATE NOCASE")
    UUID getItemByName(String name);

    @Query("SELECT DISTINCT name FROM item")
    List<String> getUniqueItemNames();

    @Query("SELECT * FROM ITEM " +
            "JOIN item_fts ON ITEM.fts_id = item_fts.rowid WHERE ITEM.fts_id = :fts_id")
    ItemEntity searchForId(Integer fts_id);

    @Query("SELECT id FROM item WHERE barcode = :barcode")
    String getItemByBarcode(String barcode);

}
