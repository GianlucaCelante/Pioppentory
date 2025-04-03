package it.pioppi.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import it.pioppi.utils.ConstantUtils;

@Entity(
        tableName = ConstantUtils.ITEM_TAG_TABLE_NAME,
        indices = {@Index("id")}
)
public class ItemTagEntity extends BaseEntity {
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NotNull
    private UUID id;

    @ColumnInfo(name = "name")
    private String name;

    public ItemTagEntity(@NonNull UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public ItemTagEntity() {
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
}

