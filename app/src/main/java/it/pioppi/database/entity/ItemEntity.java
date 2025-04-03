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
import java.util.List;
import java.util.UUID;

import it.pioppi.utils.ConstantUtils;
import it.pioppi.database.model.ItemStatus;

@Entity(
        tableName = ConstantUtils.ITEM_TABLE_NAME,
        foreignKeys = @ForeignKey(
                entity = ProviderEntity.class,
                parentColumns = "id",
                childColumns = "provider_id",
                onDelete = ForeignKey.SET_NULL
        ),
        indices = {
                @Index("id"),
                @Index("provider_id")
        }
)
public class ItemEntity extends BaseEntity {

    @NotNull
    @PrimaryKey
    @ColumnInfo(name = "id")
    private UUID id;
    @ColumnInfo(name = "fts_id")
    private Integer ftsId;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "tot_portions")
    private Long totPortions;
    @ColumnInfo(name = "status")
    private ItemStatus status;
    @ColumnInfo(name = "checked")
    private boolean checked;
    @ColumnInfo(name = "barcode")
    private String barcode;
    @ColumnInfo(name = "check_date")
    private ZonedDateTime checkDate;
    @ColumnInfo(name = "note")
    private String note;
    @ColumnInfo(name = "image_url")
    private String imageUrl;
    @ColumnInfo(name = "provider_id")
    private UUID providerId;

    @Ignore
    private ItemDetailEntity detail;
    @Ignore
    private List<ItemTagEntity> tags;

    @Ignore
    public ItemEntity(@NotNull UUID id, Integer ftsId, String name, Long totPortions,
                      ItemStatus status, boolean checked, String barcode, ZonedDateTime checkDate, String note, String imageUrl, UUID providerId) {
        super();
        this.id = id;
        this.ftsId = ftsId;
        this.name = name;
        this.totPortions = totPortions;
        this.status = status;
        this.checked = checked;
        this.barcode = barcode;
        this.checkDate = checkDate;
        this.note = note;
        this.imageUrl = imageUrl;
        this.providerId = providerId;
    }

    public ItemEntity() {}

    @NonNull
    public UUID getId() {
        return id;
    }

    public void setId(@NonNull UUID id) {
        this.id = id;
    }

    public Integer getFtsId() {
        return ftsId;
    }

    public void setFtsId(Integer ftsId) {
        this.ftsId = ftsId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getTotPortions() {
        return totPortions;
    }

    public void setTotPortions(Long totPortions) {
        this.totPortions = totPortions;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public ZonedDateTime getCheckDate() {
        return checkDate;
    }

    public void setCheckDate(ZonedDateTime checkDate) {
        this.checkDate = checkDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public UUID getProviderId() {
        return providerId;
    }

    public void setProviderId(UUID providerId) {
        this.providerId = providerId;
    }

    public List<ItemTagEntity> getTags() {
        return tags;
    }

    public void setTags(List<ItemTagEntity> tags) {
        this.tags = tags;
    }

    public ItemDetailEntity getDetail() {
        return detail;
    }

    public void setDetail(ItemDetailEntity detail) {
        this.detail = detail;
    }

    @Override
    public String toString() {
        return "ItemEntity{" + "id=" + id +
                ", name='" + name + '\'' +
                ", totPortions=" + totPortions +
                ", status=" + status +
                ", checked=" + checked +
                ", barcode='" + barcode + '\'' +
                ", checkDate=" + checkDate +
                ", note='" + note + '\'' +
                ", providerId=" + providerId +
                '}';
    }
}