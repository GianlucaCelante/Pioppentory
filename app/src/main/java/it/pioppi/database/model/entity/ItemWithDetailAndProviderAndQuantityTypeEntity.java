package it.pioppi.database.model.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class ItemWithDetailAndProviderAndQuantityTypeEntity {

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

    @Relation(
            parentColumn = "id",
            entityColumn = "item_id"
    )
    public List<QuantityTypeEntity> quantityType;

}
