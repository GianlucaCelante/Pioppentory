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
        try {
            // Esegue l'upload del log tramite LoggerManager
            LoggerManager.getInstance().uploadLog();
            return Result.success();
        } catch (Exception e) {
            LoggerManager.getInstance().log("Errore durante l'upload: " + e.getMessage(), "ERROR");
            return Result.retry();
        }
    }
}
