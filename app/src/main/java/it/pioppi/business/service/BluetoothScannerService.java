package it.pioppi.business.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.io.InputStream;

import it.pioppi.ConstantUtils;
import it.pioppi.R;

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

        // Avvia il servizio come foreground service
        Notification notification = createNotification();  // Definisci una notifica adeguata qui
        startForeground(1, notification);

        if (deviceAddress != null) {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            new Thread(() -> startScannerMonitor(device)).start();  // Esegui la connessione in un thread separato
        }

        return START_STICKY;
    }

    // Metodo per creare una notifica per il foreground service
    private Notification createNotification() {
        NotificationChannel channel = new NotificationChannel(
                "BluetoothScannerChannel",
                "Bluetooth Scanner",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

        return new NotificationCompat.Builder(this, "BluetoothScannerChannel")
                .setContentTitle("Bluetooth Scanner")
                .setContentText("Il servizio di scansione Bluetooth è attivo")
                .setSmallIcon(R.drawable.bluetooth_signal_icon)
                .build();
    }

    // Metodo per avviare il monitoraggio della pistola scanner
    public void startScannerMonitor(BluetoothDevice device) {
        try {
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
                Log.d("BluetoothScannerService", "Scoperta fermata");
            }

            // Connessione al socket RFCOMM usando UUID SPP
            Log.d("BluetoothScannerService", "Tentativo di connessione al dispositivo: " + device.getAddress());
            bluetoothSocket = device.createRfcommSocketToServiceRecord(ConstantUtils.SPP_UUID);
            bluetoothSocket.connect();
            Log.d("BluetoothScannerService", "Connesso a: " + device.getName());

            inputStream = bluetoothSocket.getInputStream();
            Log.d("BluetoothScannerService", "InputStream aperto");

            // Thread per leggere dati dalla pistola scanner
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {
                try {
                    // Legge i dati scansionati
                    bytes = inputStream.read(buffer);
                    String scannedCode = new String(buffer, 0, bytes).trim();

                    // Manda un broadcast globale con il codice scansionato
                    Intent intent = new Intent(ConstantUtils.ACTION_CODE_SCANNED);
                    intent.putExtra(ConstantUtils.SCANNED_CODE, scannedCode);
                    Log.d("Intent", "Intent: " + intent.getAction() + " - " + intent.getStringExtra(ConstantUtils.SCANNED_CODE));
                    Log.d("ScannerService", "Scanned code: " + scannedCode);

                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

                } catch (IOException e) {
                    Log.e("BluetoothScannerService", "Errore durante la lettura del socket", e);
                    break;  // Esci dal loop se c'è un errore nella lettura
                }
            }

        } catch (IOException e) {
            Log.e("BluetoothScannerService", "Errore durante la connessione", e);
        } catch (SecurityException e) {
            Log.e("BluetoothScannerService", "Errore di sicurezza", e);
        } finally {
            stopSelf();
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
