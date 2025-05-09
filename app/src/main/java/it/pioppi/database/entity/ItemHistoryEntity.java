package it.pioppi.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

import it.pioppi.database.model.QuantityTypeEnum;
import it.pioppi.utils.ConstantUtils;

@Entity(
        tableName = ConstantUtils.ITEM_HISTORY_TABLE_NAME,
        indices = {
                @Index("id")
        }
)
public class ItemHistoryEntity extends BaseEntity {

    @NotNull
    @PrimaryKey
    @ColumnInfo(name = "id")
    private UUID id;
    @ColumnInfo(name = "provider_name")
    private String providerName;
    @ColumnInfo(name = "item_name")
    private String itemName;
    @ColumnInfo(name = "quantity_present")
    private Long quantityPresent;
    @ColumnInfo(name = "quantity_ordered")
    private Long quantityOrdered;
    @ColumnInfo(name = "portions_per_weekend")
    private Long portionsPerWeekend;
    @ColumnInfo(name = "inventory_closure_date")
    private LocalDate inventoryClosureDate;
    @ColumnInfo(name = "delivery_date")
    private LocalDate deliveryDate;
    @ColumnInfo(name = "barcode")
    private String barcode;
    @ColumnInfo(name = "note")
    private String note;
    @ColumnInfo(name = "item_id")
    private UUID itemId;

    public ItemHistoryEntity(@NotNull UUID id, String providerName, String itemName, Long quantityPresent, Long quantityOrdered, Long portionsPerWeekend, LocalDate inventoryClosureDate, LocalDate deliveryDate, String barcode, String note, UUID itemId) {
        this.id = id;
        this.providerName = providerName;
        this.itemName = itemName;
        this.quantityPresent = quantityPresent;
        this.quantityOrdered = quantityOrdered;
        this.portionsPerWeekend = portionsPerWeekend;
        this.inventoryClosureDate = inventoryClosureDate;
        this.deliveryDate = deliveryDate;
        this.barcode = barcode;
        this.note = note;
        this.itemId = itemId;
    }

    public ItemHistoryEntity() {
    }

    public @NotNull UUID getId() {
        return id;
    }

    public void setId(@NotNull UUID id) {
        this.id = id;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Long getQuantityPresent() {
        return quantityPresent;
    }

    public void setQuantityPresent(Long quantityPresent) {
        this.quantityPresent = quantityPresent;
    }

    public Long getQuantityOrdered() {
        return quantityOrdered;
    }

    public void setQuantityOrdered(Long quantityOrdered) {
        this.quantityOrdered = quantityOrdered;
    }

    public LocalDate getInventoryClosureDate() {
        return inventoryClosureDate;
    }

    public void setInventoryClosureDate(LocalDate inventoryClosureDate) {
        this.inventoryClosureDate = inventoryClosureDate;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Long getPortionsPerWeekend() {
        return portionsPerWeekend;
    }

    public void setPortionsPerWeekend(Long portionsPerWeekend) {
        this.portionsPerWeekend = portionsPerWeekend;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public UUID getItemId() {
        return itemId;
    }

    public void setItemId(UUID itemId) {
        this.itemId = itemId;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ItemHistoryEntity.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("providerName='" + providerName + "'")
                .add("itemName='" + itemName + "'")
                .add("quantityPresent=" + quantityPresent)
                .add("quantityOrdered=" + quantityOrdered)
                .add("portionsPerWeekend=" + portionsPerWeekend)
                .add("inventoryClosureDate=" + inventoryClosureDate)
                .add("deliveryDate=" + deliveryDate)
                .add("barcode='" + barcode + "'")
                .add("note='" + note + "'")
                .add("itemId=" + itemId)
                .toString();
    }
}
