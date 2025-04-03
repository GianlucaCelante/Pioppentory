package it.pioppi.database.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class ItemWithDetailAndQuantityTypeEntity {

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
    public List<QuantityTypeEntity> quantityTypes;

}
