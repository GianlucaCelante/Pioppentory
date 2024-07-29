package it.pioppi.database.model.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Entity(
        tableName = "item_tag_join",
        primaryKeys = {"itemId", "tagId"},
        foreignKeys = {
                @ForeignKey(entity = ItemEntity.class,
                        parentColumns = "id",
                        childColumns = "itemId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = ItemTagEntity.class,
                        parentColumns = "id",
                        childColumns = "tagId",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {
                @Index(value = "itemId"),
                @Index(value = "tagId")
        }
)
public class ItemTagJoinEntity {
    @NotNull
    @ColumnInfo(name = "itemId")
    private UUID itemId;

    @NotNull
    @ColumnInfo(name = "tagId")
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
