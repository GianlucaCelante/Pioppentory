package it.pioppi.database.model.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import it.pioppi.ConstantUtils;
import it.pioppi.database.model.QuantityPurpose;
import it.pioppi.database.model.QuantityType;

@Entity(
        tableName = ConstantUtils.QUANTITY_TYPE_TABLE_NAME,
        indices = {@Index("id"), @Index("item_id")},
        foreignKeys = {
                @ForeignKey(
                        entity = ItemEntity.class,
                        parentColumns = "id",
                        childColumns = "item_id"
                )}
)
public class QuantityTypeEntity extends BaseEntity {
    @NotNull
    @PrimaryKey
    @ColumnInfo(name = "id")
    private UUID id;
    @ColumnInfo(name = "quantity_type")
    private QuantityType quantityType;
    @ColumnInfo(name = "quantity_type_description")
    private String quantityTypeDescription;
    @ColumnInfo(name = "quantity_type_available")
    private Integer quantityTypeAvailable;
    @ColumnInfo(name = "quantity_type_purpose")
    private QuantityPurpose purpose;
    @ColumnInfo(name = "item_id")
    private UUID itemId;

    @Ignore
    public QuantityTypeEntity(@NotNull UUID id, QuantityType quantityType, String quantityTypeDescription, Integer quantityTypeAvailable, QuantityPurpose purpose, UUID itemId) {
        super();
        this.id = id;
        this.quantityType = quantityType;
        this.quantityTypeDescription = quantityTypeDescription;
        this.quantityTypeAvailable = quantityTypeAvailable;
        this.purpose = purpose;
        this.itemId = itemId;
    }

    public QuantityTypeEntity() {

    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public QuantityType getQuantityType() {
        return quantityType;
    }

    public void setQuantityType(QuantityType quantityType) {
        this.quantityType = quantityType;
    }

    public String getQuantityTypeDescription() {
        return quantityTypeDescription;
    }

    public void setQuantityTypeDescription(String quantityTypeDescription) {
        this.quantityTypeDescription = quantityTypeDescription;
    }

    public Integer getQuantityTypeAvailable() {
        return quantityTypeAvailable;
    }

    public void setQuantityTypeAvailable(Integer quantityTypeAvailable) {
        this.quantityTypeAvailable = quantityTypeAvailable;
    }

    public QuantityPurpose getPurpose() {
        return purpose;
    }

    public void setPurpose(QuantityPurpose purpose) {
        this.purpose = purpose;
    }

    public UUID getItemId() {
        return itemId;
    }

    public void setItemId(UUID itemId) {
        this.itemId = itemId;
    }

}
