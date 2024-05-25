package it.pioppi.database.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

import it.pioppi.ConstantUtils;

@Entity(
        tableName = ConstantUtils.ITEM_TABLE_NAME,
        foreignKeys = @ForeignKey(
                entity = ProviderEntity.class,
                parentColumns = "id",
                childColumns = "provider_id"
        ),
        indices = {@Index("provider_id"), @Index("id")}
)
public class ItemEntity extends BaseEntity {

    @NotNull
    @PrimaryKey
    @ColumnInfo(name = "id")
    private UUID id;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "quantity_type")
    private QuantityType quantityType;
    @ColumnInfo(name = "tot_porzioni")
    private Integer totPorzioni;
    @ColumnInfo(name = "quantity_to_be_ordered")
    private Integer quantityToBeOrdered;
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
    @ColumnInfo(name = "check_date")
    private LocalDateTime checkDate;
    @ColumnInfo(name = "delivery_date")
    private LocalDateTime deliveryDate;
    @ColumnInfo(name = "note")
    private String note;
    @ColumnInfo(name = "status")
    private ItemStatus status;
    @ColumnInfo(name = "provider_id")
    private UUID providerId;


    public ItemEntity(@NotNull UUID id, String name, QuantityType quantityType, Integer totPorzioni, Integer quantityToBeOrdered, Integer orderedQuantity, Integer portionsRequiredOnSaturday, Integer portionsRequiredOnSunday, Integer portionsPerWeekend, Integer portionsOnHoliday, Integer maxPortionsSold, LocalDateTime checkDate, LocalDateTime deliveryDate, ItemStatus status, String note, UUID providerId) {
        this.id = id;
        this.name = name;
        this.quantityType = quantityType;
        this.totPorzioni = totPorzioni;
        this.quantityToBeOrdered = quantityToBeOrdered;
        this.orderedQuantity = orderedQuantity;
        this.portionsRequiredOnSaturday = portionsRequiredOnSaturday;
        this.portionsRequiredOnSunday = portionsRequiredOnSunday;
        this.portionsPerWeekend = portionsPerWeekend;
        this.portionsOnHoliday = portionsOnHoliday;
        this.maxPortionsSold = maxPortionsSold;
        this.checkDate = checkDate;
        this.deliveryDate = deliveryDate;
        this.note = note;
        this.status = status;
        this.providerId = providerId;
    }

    @NonNull
    public UUID getId() {
        return id;
    }

    public void setId(@NonNull UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public QuantityType getQuantityType() {
        return quantityType;
    }

    public void setQuantityType(QuantityType quantityType) {
        this.quantityType = quantityType;
    }

    public Integer getTotPorzioni() {
        return totPorzioni;
    }

    public void setTotPorzioni(Integer totPorzioni) {
        this.totPorzioni = totPorzioni;
    }

    public Integer getQuantityToBeOrdered() {
        return quantityToBeOrdered;
    }

    public void setQuantityToBeOrdered(Integer quantityToBeOrdered) {
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

    public LocalDateTime getCheckDate() {
        return checkDate;
    }

    public void setCheckDate(LocalDateTime checkDate) {
        this.checkDate = checkDate;
    }

    public LocalDateTime getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDateTime deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }

    public UUID getProviderId() {
        return providerId;
    }

    public void setProviderId(UUID providerId) {
        this.providerId = providerId;
    }
}