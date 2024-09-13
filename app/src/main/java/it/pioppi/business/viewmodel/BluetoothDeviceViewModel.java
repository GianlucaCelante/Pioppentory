package it.pioppi.business.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import it.pioppi.business.dto.BluetoothDeviceDto;

public class BluetoothDeviceViewModel extends ViewModel {

    private final MutableLiveData<List<BluetoothDeviceDto>> pairedBluetoothDevices = new MutableLiveData<>();
    private final MutableLiveData<List<BluetoothDeviceDto>> nearbyBluetoothDevices = new MutableLiveData<>();
    private BluetoothDeviceDto lastClickedDevice;

    public void setLastClickedDevice(BluetoothDeviceDto device) {
        this.lastClickedDevice = device;
    }

    public BluetoothDeviceDto getLastClickedDevice() {
        return lastClickedDevice;
    }

    public LiveData<List<BluetoothDeviceDto>> getPairedBluetoothDevices() {
        return pairedBluetoothDevices;
    }

    public void setPairedBluetoothDevices(List<BluetoothDeviceDto> pairedBluetoothDevices) {
        this.pairedBluetoothDevices.setValue(pairedBluetoothDevices);
    }

    public LiveData<List<BluetoothDeviceDto>> getNearbyBluetoothDevices() {
        return nearbyBluetoothDevices;
    }

    public void setNearbyBluetoothDevices(List<BluetoothDeviceDto> nearbyBluetoothDevices) {
        this.nearbyBluetoothDevices.setValue(nearbyBluetoothDevices);
    }

}
