package it.pioppi.business.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;

import it.pioppi.ConstantUtils;

public class BluetoothScannerService extends Service {


    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;

    @Override
    public void onCreate() {
        super.onCreate();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String deviceAddress = intent.getStringExtra(ConstantUtils.DEVICE_ADDRESS);

        if (deviceAddress != null) {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            startScannerMonitor(device); // Avvia il monitoraggio del dispositivo
        }

        return START_STICKY;
    }

    // Metodo per avviare il monitoraggio della pistola scanner
    public void startScannerMonitor(BluetoothDevice device) {
        try {
            // Crea un socket RFCOMM usando UUID SPP
            bluetoothSocket = device.createRfcommSocketToServiceRecord(ConstantUtils.SPP_UUID);
            bluetoothSocket.connect();
            inputStream = bluetoothSocket.getInputStream();

            // Thread per leggere dati dalla pistola scanner
            new Thread(() -> {
                byte[] buffer = new byte[1024];
                int bytes;
                while (true) {
                    try {
                        // Legge i dati scansionati
                        bytes = inputStream.read(buffer);
                        String scannedCode = new String(buffer, 0, bytes);

                        // Manda un broadcast globale con il codice scansionato
                        Intent intent = new Intent(ConstantUtils.ACTION_CODE_SCANNED);
                        intent.putExtra(ConstantUtils.SCANNED_CODE, scannedCode);
                        sendBroadcast(intent);

                    } catch (IOException e) {
                        break;
                    }
                }
            }).start();

        } catch (IOException | SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
