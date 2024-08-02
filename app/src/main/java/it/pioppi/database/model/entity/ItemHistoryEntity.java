package it.pioppi.database.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public class ItemHistoryEntity extends BaseEntity {

    @NotNull
    @PrimaryKey
    @ColumnInfo(name = "id")
    private UUID id;
    @ColumnInfo(name = "item_id")
    private UUID itemId;

    @ColumnInfo(name = "provider_name")
    private String providerName;

    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "tot_portions")
    private Long totPortions;
    @ColumnInfo(name = "status")
    private ItemStatus status;

    @ColumnInfo(name = "check_date")
    private LocalDateTime checkDate;
    @ColumnInfo(name = "note")
    private String note;
}
