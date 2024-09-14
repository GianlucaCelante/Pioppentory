package it.pioppi.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Upsert;

import java.util.List;
import java.util.UUID;

import it.pioppi.database.entity.ItemDetailEntity;

@Dao
public interface ItemDetailEntityDao extends BaseDao<ItemDetailEntity> {

    @Override
    @Update
    public void update(ItemDetailEntity entity);

    @Override
    @Delete
    public void delete(ItemDetailEntity entity);

    @Override
    @Insert
    public void insert(ItemDetailEntity entity);

    @Override
    @Upsert
    public void upsert(ItemDetailEntity entity);

    @Query("SELECT * FROM item_detail WHERE id = :itemDetailId")
    ItemDetailEntity getItemDetailById(UUID itemDetailId);

    @Query("SELECT * FROM item_detail")
    List<ItemDetailEntity> getAllItemDetails();

}
