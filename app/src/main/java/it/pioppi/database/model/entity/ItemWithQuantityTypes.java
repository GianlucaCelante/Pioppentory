package it.pioppi.database.model.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class ItemWithQuantityTypes {

    @Embedded
    public ItemEntity item;
    @Relation(
            parentColumn = "id",
            entityColumn = "item_id"
    )
    public List<QuantityTypeEntity> quantityTypeEntities;

}