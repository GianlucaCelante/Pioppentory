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


import it.pioppi.database.model.entity.ItemWithDetailAndProviderAndQuantityTypeEntity;
import it.pioppi.database.model.entity.ItemEntity;
import it.pioppi.database.model.entity.ItemWithDetailAndProviderEntity;
import it.pioppi.database.model.entity.ItemWithDetailEntity;
import it.pioppi.database.model.entity.ItemWithProviderEntity;

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
    @Query("SELECT item.*, provider.* FROM item JOIN provider ON item.id = provider.item_id ORDER BY provider.name")
    List<ItemWithProviderEntity> getItemsWithProvidersOrderedByProvider();

    @Transaction
    @Query("SELECT * FROM item WHERE id = :itemId")
    ItemWithDetailAndProviderEntity getItemsWithDetailsAndProvider(UUID itemId);

    @Transaction
    @Query("SELECT * FROM item WHERE id = :itemId")
    ItemWithDetailAndProviderAndQuantityTypeEntity getItemsWithDetailsAndProviderAndQuantityType(UUID itemId);
}
