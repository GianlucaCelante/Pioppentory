package it.pioppi.business.dto.item.tag;

import java.util.UUID;

public class ItemTagJoinDto {

    private UUID itemId;
    private UUID tagId;

    public ItemTagJoinDto(UUID itemId, UUID tagId) {
        this.itemId = itemId;
        this.tagId = tagId;
    }

    public UUID getItemId() {
        return itemId;
    }

    public void setItemId(UUID itemId) {
        this.itemId = itemId;
    }

    public UUID getTagId() {
        return tagId;
    }

    public void setTagId(UUID tagId) {
        this.tagId = tagId;
    }

}
