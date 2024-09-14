package it.pioppi.business.dto;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import it.pioppi.database.entity.ItemStatus;

public class ItemDto implements Parcelable {

    private UUID id;
    private Integer ftsId;
    private String name;
    private Long totPortions;
    private ItemStatus status;
    private String barcode;
    private LocalDateTime checkDate;
    private String note;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdateDate;

    public ItemDto(UUID id, Integer ftsId, String name, Long totPortions, ItemStatus status, String barcode, LocalDateTime checkDate, String note, LocalDateTime creationDate, LocalDateTime lastUpdateDate) {
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
        id = in.readByte() == 0 ? null : UUID.fromString(in.readString());
        ftsId = in.readByte() == 0 ? null : in.readInt();
        name = in.readString();
        totPortions = in.readByte() == 0 ? null : in.readLong();
        status = in.readByte() == 0 ? null : ItemStatus.valueOf(in.readString());
        barcode = in.readString();
        note = in.readString();
        checkDate = in.readByte() == 0 ? null : LocalDateTime.parse(in.readString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        creationDate = in.readByte() == 0 ? null : LocalDateTime.parse(in.readString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        lastUpdateDate = in.readByte() == 0 ? null : LocalDateTime.parse(in.readString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
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
        if (id == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeString(id.toString());
        }
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
            parcel.writeLong(totPortions);
        }
        if (status == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeString(status.name());
        }
        parcel.writeString(barcode);
        parcel.writeString(note);
        if (checkDate == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeString(checkDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (creationDate == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeString(creationDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (lastUpdateDate == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeString(lastUpdateDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
    }
}
