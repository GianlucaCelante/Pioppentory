package it.pioppi.database.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;
public class ItemWithProviderEntity {
    @Embedded
    public ItemEntity itemEntity;

    @Relation(parentColumn = "id", entityColumn = "item_id")
    public List<ProviderEntity> providers;
}

