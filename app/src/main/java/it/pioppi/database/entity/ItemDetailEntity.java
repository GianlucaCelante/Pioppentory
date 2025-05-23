package it.pioppi.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.util.UUID;

import it.pioppi.utils.ConstantUtils;

@Entity(
        tableName = ConstantUtils.ITEM_DETAIL_TABLE_NAME,
        indices = {@Index("id"), @Index("item_id")},
        foreignKeys = {
                @ForeignKey(
                        entity = ItemEntity.class,
                        parentColumns = "id",
                        childColumns = "item_id",
                        onDelete = ForeignKey.CASCADE
                )
        }
)
public class ItemDetailEntity extends BaseEntity {

    @NotNull
    @PrimaryKey
    @ColumnInfo(name = "id")
    private UUID id;
    @ColumnInfo(name = "quantity_to_be_ordered")
    private Long quantityToBeOrdered;
    @ColumnInfo(name = "ordered_quantity")
    private Integer orderedQuantity;
    @ColumnInfo(name = "portions_required_on_saturday")
    private Integer portionsRequiredOnSaturday;
    @ColumnInfo(name = "portions_required_on_sunday")
    private Integer portionsRequiredOnSunday;
    @ColumnInfo(name = "portions_per_weekend")
    private Integer portionsPerWeekend;
    @ColumnInfo(name = "portions_on_holiday")
    private Integer portionsOnHoliday;
    @ColumnInfo(name = "max_portions_sold")
    private Integer maxPortionsSold;
    @ColumnInfo(name = "delivery_date")
    private ZonedDateTime deliveryDate;
    @ColumnInfo(name = "item_id")
    private UUID itemId;

    @Ignore
    public ItemDetailEntity(@NotNull UUID id, Long quantityToBeOrdered, Integer orderedQuantity, Integer portionsRequiredOnSaturday, Integer portionsRequiredOnSunday, Integer portionsPerWeekend, Integer portionsOnHoliday, Integer maxPortionsSold, ZonedDateTime deliveryDate, UUID itemId) {
        super();
        this.id = id;
        this.quantityToBeOrdered = quantityToBeOrdered;
        this.orderedQuantity = orderedQuantity;
        this.portionsRequiredOnSaturday = portionsRequiredOnSaturday;
        this.portionsRequiredOnSunday = portionsRequiredOnSunday;
        this.portionsPerWeekend = portionsPerWeekend;
        this.portionsOnHoliday = portionsOnHoliday;
        this.maxPortionsSold = maxPortionsSold;
        this.deliveryDate = deliveryDate;
        this.itemId = itemId;
    }

    public ItemDetailEntity() {}

    @NonNull
    public UUID getId() {
        return id;
    }

    public void setId(@NonNull UUID id) {
        this.id = id;
    }

    public Long getQuantityToBeOrdered() {
        return quantityToBeOrdered;
    }

    public void setQuantityToBeOrdered(Long quantityToBeOrdered) {
        this.quantityToBeOrdered = quantityToBeOrdered;
    }

    public Integer getOrderedQuantity() {
        return orderedQuantity;
    }

    public void setOrderedQuantity(Integer orderedQuantity) {
        this.orderedQuantity = orderedQuantity;
    }

    public Integer getPortionsRequiredOnSaturday() {
        return portionsRequiredOnSaturday;
    }

    public void setPortionsRequiredOnSaturday(Integer portionsRequiredOnSaturday) {
        this.portionsRequiredOnSaturday = portionsRequiredOnSaturday;
    }

    public Integer getPortionsRequiredOnSunday() {
        return portionsRequiredOnSunday;
    }

    public void setPortionsRequiredOnSunday(Integer portionsRequiredOnSunday) {
        this.portionsRequiredOnSunday = portionsRequiredOnSunday;
    }

    public Integer getPortionsPerWeekend() {
        return portionsPerWeekend;
    }

    public void setPortionsPerWeekend(Integer portionsPerWeekend) {
        this.portionsPerWeekend = portionsPerWeekend;
    }

    public Integer getPortionsOnHoliday() {
        return portionsOnHoliday;
    }

    public void setPortionsOnHoliday(Integer portionsOnHoliday) {
        this.portionsOnHoliday = portionsOnHoliday;
    }

    public Integer getMaxPortionsSold() {
        return maxPortionsSold;
    }

    public void setMaxPortionsSold(Integer maxPortionsSold) {
        this.maxPortionsSold = maxPortionsSold;
    }

    public ZonedDateTime getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(ZonedDateTime deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public UUID getItemId() {
        return itemId;
    }

    public void setItemId(UUID itemId) {
        this.itemId = itemId;
    }


    @NonNull
    @Override
    public String toString() {
        return "ItemDetailEntity{" + "id=" + id +
                ", quantityToBeOrdered=" + quantityToBeOrdered +
                ", orderedQuantity=" + orderedQuantity +
                ", portionsRequiredOnSaturday=" + portionsRequiredOnSaturday +
                ", portionsRequiredOnSunday=" + portionsRequiredOnSunday +
                ", portionsPerWeekend=" + portionsPerWeekend +
                ", portionsOnHoliday=" + portionsOnHoliday +
                ", maxPortionsSold=" + maxPortionsSold +
                ", deliveryDate=" + deliveryDate +
                '}';
    }
}