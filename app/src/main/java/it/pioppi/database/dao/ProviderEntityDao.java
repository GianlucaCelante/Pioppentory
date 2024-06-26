package it.pioppi.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.UUID;

import it.pioppi.database.model.ProviderEntity;
import it.pioppi.database.model.ProviderWithItemsEntity;

@Dao
public interface ProviderEntityDao extends BaseDao<ProviderEntity> {

    @Override
    @Update
    public void update(ProviderEntity entity);

    @Override
    @Delete
    public void delete(ProviderEntity entity);

    @Override
    @Insert
    public void insert(ProviderEntity entity);

    @Transaction
    @Query("SELECT * FROM provider WHERE id = :providerId")
    ProviderWithItemsEntity getProviderWithItems(UUID providerId);
}
