package it.pioppi.business.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.io.InputStream;

import it.pioppi.R;
import it.pioppi.business.dto.bluetooth.BluetoothSocketHolder;
import it.pioppi.utils.ConstantUtils;
import it.pioppi.utils.LoggerManager;

public class BluetoothScannerService extends Service {

    private BluetoothSocket bluetoothSocket;
    private boolean isRunning = true;

    @Override
    public void onCreate() {
        LoggerManager.getInstance().log("BluetoothScannerService onCreate started", "INFO");
        super.onCreate();
        LoggerManager.getInstance().log("BluetoothScannerService onCreate completed", "INFO");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LoggerManager.getInstance().log("BluetoothScannerService onStartCommand started", "INFO");

        Notification notification = createNotification();
        startForeground(1, notification);

        bluetoothSocket = BluetoothSocketHolder.getInstance().getSocket();

        if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
            LoggerManager.getInstance().log("Bluetooth socket is connected. Starting scanner monitor.", "DEBUG");
            new Thread(this::startScannerMonitor).start();
        } else {
            LoggerManager.getInstance().log("Socket Bluetooth nullo o non connesso", "ERROR");
            stopSelf();
        }

        LoggerManager.getInstance().log("BluetoothScannerService onStartCommand completed", "INFO");
        return START_STICKY;
    }

    public void startScannerMonitor() {
        LoggerManager.getInstance().log("startScannerMonitor started", "INFO");
        try {
            InputStream inputStream = bluetoothSocket.getInputStream();

            byte[] buffer = new byte[1024];
            int bytes;
            while (isRunning) {
                bytes = inputStream.read(buffer);
                String scannedCode = new String(buffer, 0, bytes).trim();
                LoggerManager.getInstance().log("Scanned code: " + scannedCode, "DEBUG");

                Intent codeIntent = new Intent(ConstantUtils.ACTION_CODE_SCANNED);
                codeIntent.putExtra(ConstantUtils.SCANNED_CODE, scannedCode);
                LocalBroadcastManager.getInstance(this).sendBroadcast(codeIntent);
            }
        } catch (IOException e) {
            LoggerManager.getInstance().logException(e);
        } finally {
            LoggerManager.getInstance().log("Scanner monitor finished, stopping service.", "INFO");
            stopSelf();
        }
    }

    private Notification createNotification() {
        LoggerManager.getInstance().log("Creating notification for BluetoothScannerService", "DEBUG");
        String channelId = "BluetoothScannerChannel";
        String channelName = "Bluetooth Scanner";

        NotificationChannel channel = new NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Bluetooth Scanner")
                .setContentText("Il servizio di scansione Bluetooth è attivo")
                .setSmallIcon(R.drawable.bluetooth_signal_icon)
                .build();
        LoggerManager.getInstance().log("Notification created for BluetoothScannerService", "DEBUG");
        return notification;
    }

    @Override
    public void onDestroy() {
        LoggerManager.getInstance().log("BluetoothScannerService onDestroy started", "INFO");
        super.onDestroy();
        isRunning = false;
        try {
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
                LoggerManager.getInstance().log("Bluetooth socket closed", "DEBUG");
            }
        } catch (IOException e) {
            LoggerManager.getInstance().logException(e);
        }
        LoggerManager.getInstance().log("BluetoothScannerService onDestroy completed", "INFO");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
