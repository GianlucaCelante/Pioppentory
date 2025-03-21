package it.pioppi.utils;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class LogUploadWorker extends Worker {

    public LogUploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Leggi il contenuto del log
        String logContent = LoggerManager.getInstance().readLogFile(); // Assicurati di avere un metodo per leggere il file
        if (logContent == null || logContent.isEmpty()) {
            // Non c'è niente da caricare
            return Result.success();
        }
        try {
            // Esegui l'upload del log
            LoggerManager.getInstance().uploadLog();
            // Se l'upload va a buon fine, svuota il file
            LoggerManager.getInstance().clearLogFile();
            return Result.success();
        } catch (Exception e) {
            LoggerManager.getInstance().log("Errore durante l'upload: " + e.getMessage(), "ERROR");
            return Result.retry();
        }
    }
}
