package it.pioppi.database.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;
public class ItemWithProviderEntity {
    @Embedded
    public ItemEntity item;

    @Relation(
            parentColumn = "providerId",
            entityColumn = "id"
    )
    public ProviderEntity provider;
}

