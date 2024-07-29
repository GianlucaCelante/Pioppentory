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

import it.pioppi.database.model.entity.ItemTagEntity;
import it.pioppi.database.model.entity.ItemTagJoinEntity;
import it.pioppi.database.model.entity.ProviderEntity;

@Dao
public interface ItemTagEntityDao extends BaseDao<ItemTagEntity> {

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

    @Query("SELECT DISTINCT name FROM item_tag")
    List<String> getItemTags();

    @Query("SELECT * FROM item_tag WHERE name = :name")
    ItemTagEntity getItemTagByName(String name);

    @Query("SELECT * FROM item_tag WHERE id IN (SELECT tagId FROM item_tag_join WHERE itemId = :itemId)")
    List<ItemTagEntity> getItemTagsForItem(UUID itemId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertItemTagJoin(ItemTagJoinEntity itemTagJoinEntity);
}
