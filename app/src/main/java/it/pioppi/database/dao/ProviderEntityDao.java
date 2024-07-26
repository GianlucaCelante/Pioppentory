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

import it.pioppi.database.model.entity.ProviderEntity;

@Dao
public interface ProviderEntityDao extends BaseDao<ProviderEntity> {

    @Override
    @Update
    void update(ProviderEntity entity);

    @Override
    @Delete
    void delete(ProviderEntity entity);

    @Override
    @Insert
    void insert(ProviderEntity entity);

    @Override
    @Upsert
    void upsert(ProviderEntity entity);

    @Query("SELECT DISTINCT name FROM provider")
    List<String> getProviderNames();

}
