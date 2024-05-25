package it.pioppi.database.model;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class ProviderWithItemsEntity {

    @Embedded
    public ProviderEntity provider;

    @Relation(
            parentColumn = "id",
            entityColumn = "provider_id"
    )
    public List<ItemEntity> items;
}
