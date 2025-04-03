package it.pioppi.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import it.pioppi.database.model.QuantityTypeEnum;
import it.pioppi.utils.ConstantUtils;
import it.pioppi.database.model.QuantityPurpose;

@Entity(
        tableName = ConstantUtils.QUANTITY_TYPE_TABLE_NAME,
        indices = {@Index("id"), @Index("item_id")},
        foreignKeys = {
                @ForeignKey(
                        entity = ItemEntity.class,
                        parentColumns = "id",
                        childColumns = "item_id",
                        onDelete = ForeignKey.CASCADE
                )}
)
public class QuantityTypeEntity extends BaseEntity {
    @NotNull
    @PrimaryKey
    @ColumnInfo(name = "id")
    private UUID id;
    @ColumnInfo(name = "quantity_type")
    private QuantityTypeEnum quantityTypeEnum;
    @ColumnInfo(name = "quantity_type_description")
    private String quantityTypeDescription;
    @ColumnInfo(name = "quantity_type_available")
    private Integer quantityTypeAvailable;
    @ColumnInfo(name = "quantity_type_purpose")
    private QuantityPurpose purpose;
    @ColumnInfo(name = "units_per_quantity_type")
    private Integer unitsPerQuantityType;
    @ColumnInfo(name = "item_id")
    private UUID itemId;

    @Ignore
    public QuantityTypeEntity(@NotNull UUID id, QuantityTypeEnum quantityTypeEnum, String quantityTypeDescription, Integer quantityTypeAvailable, QuantityPurpose purpose, Integer unitsPerQuantityType, UUID itemId) {
        super();
        this.id = id;
        this.quantityTypeEnum = quantityTypeEnum;
        this.quantityTypeDescription = quantityTypeDescription;
        this.quantityTypeAvailable = quantityTypeAvailable;
        this.purpose = purpose;
        this.unitsPerQuantityType = unitsPerQuantityType;
        this.itemId = itemId;
    }

    public QuantityTypeEntity() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public QuantityTypeEnum getQuantityTypeEnum() {
        return quantityTypeEnum;
    }

    public void setQuantityTypeEnum(QuantityTypeEnum quantityTypeEnum) {
        this.quantityTypeEnum = quantityTypeEnum;
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

    public Integer getUnitsPerQuantityType() {
        return unitsPerQuantityType;
    }

    public void setUnitsPerQuantityType(Integer unitsPerQuantityType) {
        this.unitsPerQuantityType = unitsPerQuantityType;
    }

    public UUID getItemId() {
        return itemId;
    }

    public void setItemId(UUID itemId) {
        this.itemId = itemId;
    }

}
