package it.pioppi;

import android.app.Application;
import android.util.Log;

import androidx.work.BackoffPolicy;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import it.pioppi.business.manager.GoogleDriveManager;
import it.pioppi.utils.GoogleDriveLogUploader;
import it.pioppi.utils.LogUploadWorker;
import it.pioppi.utils.LoggerManager;

public class Pioppentory extends Application {
    private Thread.UncaughtExceptionHandler defaultUEH;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Pioppentory", "onCreate: scheduling PeriodicLogUpload worker");

        LoggerManager.init(getApplicationContext(), "app_log.txt", true, null);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (account != null) {
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                    this, Collections.singleton("https://www.googleapis.com/auth/drive")
            );
            credential.setSelectedAccount(account.getAccount());
            Drive driveService = new Drive.Builder(
                    new NetHttpTransport(),
                    new GsonFactory(),
                    credential)
                    .setApplicationName("Pioppentory")
                    .build();

            GoogleDriveManager googleDriveManager = new GoogleDriveManager(driveService);
            GoogleDriveLogUploader logUploader = new GoogleDriveLogUploader(googleDriveManager, this);
            // Aggiorna LoggerManager con l'istanza corretta di LogUploader.
            LoggerManager.getInstance().setLogUploader(logUploader);
            Log.d("Pioppentory", "LogUploader inizializzato con Drive service");
        } else {
            Log.w("Pioppentory", "Utente non loggato; LogUploader non impostato");
        }

        defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            LoggerManager.getInstance().logException(new Exception(throwable));
            try {
                Thread.sleep(3000); // Attende per consentire la scrittura del log
            } catch (InterruptedException e) {
                Log.e("Pioppentory", "Sleep interrotto nell'handler di eccezione", e);
            }
            defaultUEH.uncaughtException(thread, throwable);
        });

        PeriodicWorkRequest periodicUploadRequest =
                new PeriodicWorkRequest.Builder(LogUploadWorker.class, 1, TimeUnit.DAYS)
                        .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
                        .build();

        WorkManager.getInstance(getApplicationContext()).enqueueUniquePeriodicWork(
                "PeriodicLogUpload",
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicUploadRequest
        );
    }
}
