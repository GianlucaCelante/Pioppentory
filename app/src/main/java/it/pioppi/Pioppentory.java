package it.pioppi;

import android.app.Application;
import android.util.Log;

import androidx.work.BackoffPolicy;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

import it.pioppi.utils.LogUploadWorker;
import it.pioppi.utils.LoggerManager;

public class Pioppentory extends Application {
    private Thread.UncaughtExceptionHandler defaultUEH;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Pioppentory", "onCreate: scheduling PeriodicLogUpload worker");

        // Inizializza LoggerManager: qui viene creato il file di log e verranno registrati i log di eccezione
        LoggerManager.init(getApplicationContext(), "app_log.txt", true, null);

        // Salva il default uncaught exception handler
        defaultUEH = Thread.getDefaultUncaughtExceptionHandler();

        // Imposta il nuovo default handler che registra l'eccezione e attende un breve lasso di tempo
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            // Registra l'eccezione
            LoggerManager.getInstance().logException(new Exception(throwable));

            try {
                // Attendi un breve periodo per consentire la scrittura del log
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Log.e("Pioppentory", "Sleep interrotto in exception handler", e);
            }

            // Delega al default handler originale
            defaultUEH.uncaughtException(thread, throwable);
        });

        // Pianifica il worker periodico per l'upload del log (in questo esempio ogni 1 giorno)
        PeriodicWorkRequest periodicUploadRequest =
                new PeriodicWorkRequest.Builder(LogUploadWorker.class, 1, TimeUnit.DAYS)
                        .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
                        .build();

        WorkManager.getInstance(getApplicationContext()).enqueueUniquePeriodicWork(
                "PeriodicLogUpload",
                ExistingPeriodicWorkPolicy.REPLACE,
                periodicUploadRequest
        );
    }
}
