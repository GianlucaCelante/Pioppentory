package it.pioppi.database.model.entity;

import android.text.BoringLayout;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import it.pioppi.ConstantUtils;

@Entity(
        tableName = ConstantUtils.ITEM_TABLE_NAME,
        indices = {
                @Index("id")
        }
)
public class ItemEntity extends BaseEntity {

    @NotNull
    @PrimaryKey
    @ColumnInfo(name = "id")
    private UUID id;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "tot_portions")
    private Integer totPortions;
    @ColumnInfo(name = "status")
    private ItemStatus status;
    @ColumnInfo(name = "barcode")
    private String barcode;
    @ColumnInfo(name = "check_date")
    private LocalDateTime checkDate;
    @ColumnInfo(name = "note")
    private String note;


    @Ignore
    private List<ItemTagEntity> tags;

    @Ignore
    public ItemEntity(@NotNull UUID id, String name, Integer totPortions, ItemStatus status, String barcode, LocalDateTime checkDate, String note) {
        super();
        this.id = id;
        this.name = name;
        this.totPortions = totPortions;
        this.status = status;
        this.barcode = barcode;
        this.checkDate = checkDate;
        this.note = note;
    }

    public ItemEntity() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getTotPortions() {
        return totPortions;
    }

    public void setTotPortions(Integer totPortions) {
        this.totPortions = totPortions;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public LocalDateTime getCheckDate() {
        return checkDate;
    }

    public void setCheckDate(LocalDateTime checkDate) {
        this.checkDate = checkDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<ItemTagEntity> getTags() {
        return tags;
    }

    public void setTags(List<ItemTagEntity> tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "ItemEntity{" + "id=" + id +
                ", name='" + name + '\'' +
                ", totPortions=" + totPortions +
                ", status=" + status +
                ", barcode='" + barcode + '\'' +
                ", checkDate=" + checkDate +
                ", note='" + note + '\'' +
                '}';
    }
}