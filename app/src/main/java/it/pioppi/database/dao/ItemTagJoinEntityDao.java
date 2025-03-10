package it.pioppi.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Upsert;

import java.util.List;
import java.util.UUID;

import it.pioppi.database.entity.ItemEntity;
import it.pioppi.database.entity.ItemTagEntity;
import it.pioppi.database.entity.ItemTagJoinEntity;

@Dao
public interface ItemTagJoinEntityDao extends BaseDao<ItemTagEntity> {

    @Override
    @Update
    void update(ItemTagEntity entity);

    @Override
    @Delete
    void delete(ItemTagEntity entity);

    @Override
    @Insert
    void insert(ItemTagEntity entity);

    @Override
    @Upsert
    void upsert(ItemTagEntity entity);

    @Query("DELETE FROM item_tag_join WHERE tag_id = :tagId AND item_id = :itemId")
    void delete(UUID tagId, UUID itemId);

    @Query("SELECT * FROM item_tag_join")
    List<ItemTagJoinEntity> getAll();

}
