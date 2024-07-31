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

import it.pioppi.database.model.entity.ItemEntity;
import it.pioppi.database.model.entity.ItemFTSEntity;

@Dao
public interface ItemFTSEntityDao extends BaseDao<ItemFTSEntity> {

    @Override
    @Update
    void update(ItemFTSEntity entity);

    @Override
    @Delete
    void delete(ItemFTSEntity entity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ItemFTSEntity item);

    @Override
    @Upsert
    void upsert(ItemFTSEntity entity);

    @Query("INSERT INTO item_fts(rowid, name, barcode) SELECT fts_id, name, barcode FROM item WHERE fts_id = :ftsId")
    void insertItemFTS(Integer ftsId);

    @Query("SELECT max(rowid) + 1 FROM item_fts")
    Integer getNextId();

    @Query("SELECT rowid FROM item_fts WHERE name MATCH :query OR barcode MATCH :query")
    List<Integer> searchForNameAndBarcode(String query);

    @Query("SELECT * FROM item WHERE rowid IN (SELECT rowid FROM item_fts WHERE item_fts MATCH :query || '*')")
    List<ItemEntity> searchForItem(String query);

}
