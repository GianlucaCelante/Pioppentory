package it.pioppi.business.dto;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class BluetoothSocketHolder {
    private static BluetoothSocketHolder instance;
    private BluetoothSocket socket;
    private BluetoothDevice connectedDevice;

    private BluetoothSocketHolder() {}

    public static synchronized BluetoothSocketHolder getInstance() {
        if (instance == null) {
            instance = new BluetoothSocketHolder();
        }
        return instance;
    }

    public synchronized void setSocket(BluetoothSocket socket) {
        this.socket = socket;
    }

    public synchronized BluetoothSocket getSocket() {
        return socket;
    }

    public synchronized void setConnectedDevice(BluetoothDevice device) {
        this.connectedDevice = device;
    }

    public synchronized BluetoothDevice getConnectedDevice() {
        return connectedDevice;
    }

    public synchronized void clear() {
        this.socket = null;
        this.connectedDevice = null;
    }
}



