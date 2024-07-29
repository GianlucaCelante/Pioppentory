package it.pioppi.database.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Fts4;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import it.pioppi.ConstantUtils;

@Entity(tableName = "item_fts", primaryKeys = {"rowid"})
@Fts4(contentEntity = ItemEntity.class)
public class ItemFTSEntity {

    @ColumnInfo(name = "rowid")
    public Integer rowid;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "barcode")
    public String barcode;

    public ItemFTSEntity(Integer rowid, String name, String barcode) {
        this.rowid = rowid;
        this.name = name;
        this.barcode = barcode;
    }

    public ItemFTSEntity() {
    }

    public Integer getId() {
        return rowid;
    }

    public String getName() {
        return name;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setId(Integer rowid) {
        this.rowid = rowid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
}