package it.pioppi.business.dto;

import android.bluetooth.BluetoothSocket;

public class BluetoothSocketHolder {
    private static BluetoothSocketHolder instance;
    private BluetoothSocket socket;

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
}


