package it.pioppi.business.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProviderDto {

    private UUID id;
    private String name;
    private String address;
    private String phoneNumber;
    private String email;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdateDate;

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

    public ProviderDto() {
    }

    public ProviderDto(UUID id, String name, String address, String phoneNumber, String email, LocalDateTime creationDate, LocalDateTime lastUpdateDate) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.creationDate = creationDate;
        this.lastUpdateDate = lastUpdateDate;
    }
}
