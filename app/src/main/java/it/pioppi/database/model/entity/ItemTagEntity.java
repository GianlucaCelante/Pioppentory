package it.pioppi.database.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Entity(
        tableName = "item_tag",
        indices = {@Index("id")}
)
public class ItemTagEntity extends BaseEntity {
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NotNull
    private UUID id;

    @ColumnInfo(name = "name")
    private String name;

    public ItemTagEntity(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public ItemTagEntity() {

    }

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
}

