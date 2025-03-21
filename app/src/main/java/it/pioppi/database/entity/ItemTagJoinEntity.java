package it.pioppi.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import it.pioppi.utils.ConstantUtils;

@Entity(
        tableName = ConstantUtils.ITEM_TAG_JOIN_TABLE_NAME,
        primaryKeys = {"item_id", "tag_id"},
        foreignKeys = {
                @ForeignKey(entity = ItemEntity.class,
                        parentColumns = "id",
                        childColumns = "item_id"
                ),
                @ForeignKey(entity = ItemTagEntity.class,
                        parentColumns = "id",
                        childColumns = "tag_id",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {
                @Index(value = "item_id"),
                @Index(value = "tag_id")
        }
)
public class ItemTagJoinEntity {
    @NotNull
    @ColumnInfo(name = "item_id")
    private UUID itemId;

    @NotNull
    @ColumnInfo(name = "tag_id")
    private UUID tagId;

    public ItemTagJoinEntity(@NonNull UUID itemId, @NonNull UUID tagId) {
        this.itemId = itemId;
        this.tagId = tagId;
    }

    @NonNull
    public UUID getItemId() {
        return itemId;
    }

    public void setItemId(@NonNull UUID itemId) {
        this.itemId = itemId;
    }

    @NonNull
    public UUID getTagId() {
        return tagId;
    }

    public void setTagId(@NonNull UUID tagId) {
        this.tagId = tagId;
    }
}
