package it.pioppi.business.dto.bluetooth;

import java.time.ZonedDateTime;
import java.util.UUID;

public class BluetoothDeviceDto {

    private UUID id;
    private String name;
    private String address;
    private boolean detected;
    private boolean connected;
    private ZonedDateTime creationDate;
    private ZonedDateTime lastUpdateDate;

    public BluetoothDeviceDto() {
    }

    public BluetoothDeviceDto(UUID id, String name, String address, boolean detected, boolean connected, ZonedDateTime creationDate, ZonedDateTime lastUpdateDate) {
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
