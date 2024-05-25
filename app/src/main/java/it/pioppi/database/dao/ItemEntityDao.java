package it.pioppi.database.dao;

import android.content.Context;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.time.LocalDateTime;
import java.util.UUID;


import it.pioppi.database.AppDatabase;
import it.pioppi.database.model.ItemEntity;

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

    @Query("SELECT * FROM item WHERE id = :itemId")
    ItemEntity getItemById(UUID itemId);



}
