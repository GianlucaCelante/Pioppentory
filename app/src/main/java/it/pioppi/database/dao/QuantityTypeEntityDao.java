package it.pioppi.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Upsert;

import java.util.UUID;

import it.pioppi.database.model.entity.ItemWithQuantityTypes;
import it.pioppi.database.model.entity.QuantityTypeEntity;

@Dao
public interface QuantityTypeEntityDao extends BaseDao<QuantityTypeEntity> {

    @Override
    @Delete
    public void delete(QuantityTypeEntity entity);

    @Override
    @Insert
    public void insert(QuantityTypeEntity entity);

    @Override
    @Upsert
    public void upsert(QuantityTypeEntity entity);

    @Transaction
    @Query("SELECT * FROM item WHERE id = :itemId")
    ItemWithQuantityTypes getItemWithQuantityTypes(UUID itemId);

}
