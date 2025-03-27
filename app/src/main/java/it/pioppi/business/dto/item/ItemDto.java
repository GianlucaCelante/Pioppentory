package it.pioppi.business.dto.item;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import it.pioppi.database.model.ItemStatus;

public class ItemDto implements Parcelable {

    private UUID id;
    private Integer ftsId;
    private String name;
    private Long totPortions;
    private ItemStatus status;
    private boolean checked;
    private String barcode;
    private ZonedDateTime checkDate;
    private String note;
    private String imageUrl;
    private ZonedDateTime creationDate;
    private ZonedDateTime lastUpdateDate;
    private UUID providerId;

    public ItemDto(UUID id, Integer ftsId, String name, Long totPortions, ItemStatus status, boolean checked,
                   String barcode, ZonedDateTime checkDate, String note, String imageUrl,
                   ZonedDateTime creationDate, ZonedDateTime lastUpdateDate, UUID providerId) {
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
        this.creationDate = creationDate;
        this.lastUpdateDate = lastUpdateDate;
        this.providerId = providerId;
    }

    public ItemDto() {}

    protected ItemDto(Parcel in) {
        id = in.readByte() == 0 ? null : UUID.fromString(in.readString());
        ftsId = in.readByte() == 0 ? null : in.readInt();
        name = in.readString();
        totPortions = in.readByte() == 0 ? null : in.readLong();
        status = in.readByte() == 0 ? null : ItemStatus.valueOf(in.readString());
        checked = in.readByte() != 0;
        barcode = in.readString();
        note = in.readString();
        imageUrl = in.readString();
        checkDate = in.readByte() == 0 ? null : ZonedDateTime.parse(in.readString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        creationDate = in.readByte() == 0 ? null : ZonedDateTime.parse(in.readString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        lastUpdateDate = in.readByte() == 0 ? null : ZonedDateTime.parse(in.readString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        providerId = in.readByte() == 0 ? null : UUID.fromString(in.readString());

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

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(ZonedDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public ZonedDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(ZonedDateTime lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public UUID getProviderId() {
        return providerId;
    }

    public void setProviderId(UUID providerId) {
        this.providerId = providerId;
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
        parcel.writeByte((byte) (checked ? 1 : 0));
        parcel.writeString(barcode);
        parcel.writeString(note);
        parcel.writeString(imageUrl);
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

        if (providerId == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeString(providerId.toString());
        }
    }
}
