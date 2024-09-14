package it.pioppi.database.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;
public class ItemWithProviderEntity {
    @Embedded
    public ItemEntity itemEntity;

    @Relation(parentColumn = "id", entityColumn = "item_id")
    public List<ProviderEntity> providers;
}

