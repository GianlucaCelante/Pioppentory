package it.pioppi.database.model.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

public class ItemWithDetailAndProviderEntity {

    @Embedded
    public ItemEntity item;

    @Relation(
            parentColumn = "id",
            entityColumn = "item_id"
    )
    public ItemDetailEntity itemDetail;

    @Relation(
            parentColumn = "id",
            entityColumn = "item_id"
    )
    public ProviderEntity provider;

}
