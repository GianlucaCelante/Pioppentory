package it.pioppi.database.dao;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;


public interface BaseDao<T> {

    void update(T entity);

    void delete(T entity);

    void insert(T entity);

}
