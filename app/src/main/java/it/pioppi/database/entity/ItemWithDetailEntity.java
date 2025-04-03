package it.pioppi.database.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

public class ItemWithDetailEntity {

    @Embedded
    public ItemEntity item;

    @Relation(
            parentColumn = "id",
            entityColumn = "item_id"
    )
    public ItemDetailEntity itemDetail;
}