package it.pioppi.business.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class BluetoothDeviceDto {

    private UUID id;
    private String name;
    private String address;
    private boolean detected;
    private boolean connected;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdateDate;

    public BluetoothDeviceDto() {
    }

    public BluetoothDeviceDto(UUID id, String name, String address, boolean detected, boolean connected, LocalDateTime creationDate, LocalDateTime lastUpdateDate) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.detected = detected;
        this.connected = connected;
        this.creationDate = creationDate;
        this.lastUpdateDate = lastUpdateDate;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isDetected() {
        return detected;
    }

    public void setDetected(boolean detected) {
        this.detected = detected;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
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
    public String toString() {
        return "BluetoothDeviceDto{" +
                "id=" + id +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", detected=" + detected +
                ", connected=" + connected +
                ", creationDate=" + creationDate +
                ", lastUpdateDate=" + lastUpdateDate +
                '}';
    }
}
