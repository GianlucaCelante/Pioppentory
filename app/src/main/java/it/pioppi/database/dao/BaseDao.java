package it.pioppi.database.dao;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Upsert;


public interface BaseDao<T> {

    @Update
    void update(T entity);

    @Delete
    void delete(T entity);

    @Insert
    void insert(T entity);

    @Upsert
    void upsert(T entity);

}
