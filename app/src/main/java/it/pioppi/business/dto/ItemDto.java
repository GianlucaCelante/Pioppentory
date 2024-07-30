package it.pioppi.business.dto;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.time.LocalDateTime;
import java.util.UUID;

import it.pioppi.database.model.entity.ItemStatus;

public class ItemDto implements Parcelable {

    private UUID id;
    private Integer ftsId;
    private String name;
    private Integer totPortions;
    private ItemStatus status;
    private String barcode;
    private LocalDateTime checkDate;
    private String note;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdateDate;

    public ItemDto(UUID id, Integer ftsId, String name, Integer totPortions, ItemStatus status, String barcode, LocalDateTime checkDate, String note, LocalDateTime creationDate, LocalDateTime lastUpdateDate) {
        this.id = id;
        this.ftsId = ftsId;
        this.name = name;
        this.totPortions = totPortions;
        this.status = status;
        this.barcode = barcode;
        this.checkDate = checkDate;
        this.note = note;
        this.creationDate = creationDate;
        this.lastUpdateDate = lastUpdateDate;
    }

    public ItemDto() {}

    protected ItemDto(Parcel in) {
        if (in.readByte() == 0) {
            ftsId = null;
        } else {
            ftsId = in.readInt();
        }
        name = in.readString();
        if (in.readByte() == 0) {
            totPortions = null;
        } else {
            totPortions = in.readInt();
        }
        barcode = in.readString();
        note = in.readString();
    }

    public static final Creator<ItemDto> CREATOR = new Creator<ItemDto>() {
        @Override
        public ItemDto createFromParcel(Parcel in) {
            return new ItemDto(in);
        }

        @Override
        public ItemDto[] newArray(int size) {
            return new ItemDto[size];
        }
    };

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
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

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(LocalDateTime lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        if (ftsId == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(ftsId);
        }
        parcel.writeString(name);
        if (totPortions == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(totPortions);
        }
        parcel.writeString(barcode);
        parcel.writeString(note);
    }
}
