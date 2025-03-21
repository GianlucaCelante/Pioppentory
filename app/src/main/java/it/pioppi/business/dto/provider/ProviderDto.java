package it.pioppi.business.dto.provider;

import java.time.ZonedDateTime;
import java.util.UUID;

public class ProviderDto {

    private UUID id;
    private String name;
    private String address;
    private String phoneNumber;
    private String email;
    private ZonedDateTime creationDate;
    private ZonedDateTime lastUpdateDate;
    private UUID itemId;

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
    public UUID getItemId() {
        return itemId;
    }
    public void setItemId(UUID itemId) {
        this.itemId = itemId;
    }

    public ProviderDto() {
    }

    public ProviderDto(UUID id, String name, String address, String phoneNumber, String email, ZonedDateTime creationDate, ZonedDateTime lastUpdateDate, UUID itemId) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.creationDate = creationDate;
        this.lastUpdateDate = lastUpdateDate;
        this.itemId = itemId;
    }
}
