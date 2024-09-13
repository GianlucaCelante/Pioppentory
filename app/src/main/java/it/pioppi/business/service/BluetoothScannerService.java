package it.pioppi.business.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
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
import it.pioppi.business.dto.BluetoothSocketHolder;

public class BluetoothScannerService extends Service {

    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private boolean isRunning = true;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = createNotification();
        startForeground(1, notification);

        bluetoothSocket = BluetoothSocketHolder.getInstance().getSocket();

        if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
            new Thread(this::startScannerMonitor).start();
        } else {
            Log.e("BluetoothScannerService", "Socket Bluetooth nullo o non connesso");
            stopSelf();
        }

        return START_STICKY;
    }

    public void startScannerMonitor() {
        try {
            inputStream = bluetoothSocket.getInputStream();

            byte[] buffer = new byte[1024];
            int bytes;
            while (isRunning) {
                bytes = inputStream.read(buffer);
                String scannedCode = new String(buffer, 0, bytes).trim();

                Intent codeIntent = new Intent(ConstantUtils.ACTION_CODE_SCANNED);
                codeIntent.putExtra(ConstantUtils.SCANNED_CODE, scannedCode);
                LocalBroadcastManager.getInstance(this).sendBroadcast(codeIntent);
            }

        } catch (IOException e) {
            Log.e("BluetoothScannerService", "Errore durante la lettura del socket", e);
        } finally {
            stopSelf();
        }
    }

    private Notification createNotification() {
        String channelId = "BluetoothScannerChannel";
        String channelName = "Bluetooth Scanner";

        NotificationChannel channel = new NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Bluetooth Scanner")
                .setContentText("Il servizio di scansione Bluetooth è attivo")
                .setSmallIcon(R.drawable.bluetooth_signal_icon) // Assicurati che l'icona esista
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
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
