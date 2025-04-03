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

import it.pioppi.database.entity.ItemWithQuantityTypes;
import it.pioppi.database.entity.QuantityTypeEntity;

@Dao
public interface QuantityTypeEntityDao extends BaseDao<QuantityTypeEntity> {

    @Override
    @Delete
    void delete(QuantityTypeEntity entity);

    @Override
    @Insert
    void insert(QuantityTypeEntity entity);

    @Override
    @Upsert
    void upsert(QuantityTypeEntity entity);

    @Override
    @Update
    void update(QuantityTypeEntity entity);

    @Transaction
    @Query("SELECT * FROM item WHERE id = :itemId")
    ItemWithQuantityTypes getItemWithQuantityTypes(UUID itemId);

    @Query("SELECT * FROM quantity_type")
    List<QuantityTypeEntity> getAll();

}
