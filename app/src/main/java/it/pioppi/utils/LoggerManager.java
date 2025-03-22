package it.pioppi.utils;

import android.content.Context;
import android.util.Log;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LoggerManager {
    private static LoggerManager instance;
    private final Context context;
    private final String logFileName;
    private final boolean uploadToDrive;
    private final List<String> logMessages;
    private LogUploader logUploader;

    // Costruttore privato con dependency injection del LogUploader
    private LoggerManager(Context context, String logFileName, boolean uploadToDrive, LogUploader logUploader) {
        this.context = context.getApplicationContext();
        this.logFileName = logFileName;
        this.uploadToDrive = uploadToDrive;
        this.logUploader = logUploader;
        this.logMessages = new ArrayList<>();
        initLogFile();
    }

    public static synchronized void init(Context context, String logFileName, boolean uploadToDrive, LogUploader logUploader) {
        if (instance == null) {
            instance = new LoggerManager(context, logFileName, uploadToDrive, logUploader);
        }
    }

    public static synchronized LoggerManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("LoggerManager non è stato inizializzato. Chiamare init() prima.");
        }
        return instance;
    }

    // Metodo setter per aggiornare il LogUploader
    public void setLogUploader(LogUploader logUploader) {
        this.logUploader = logUploader;
    }

    // Inizializza il file di log con un header contenente la data corrente
    private void initLogFile() {
        String header = "--- Log file iniziato il " +
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date()) +
                " ---\n";
        logMessages.add(header);
        writeToFile(header);
    }

    // Logga un messaggio con timestamp e livello
    public void log(String message, String level) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date());
        String logEntry = "[" + timestamp + "] [" + level + "] " + message + "\n";
        logMessages.add(logEntry);
        writeToFile(logEntry);
    }

    // Logga un'eccezione con stack trace
    public void logException(Exception e) {
        log("Eccezione sollevata: " + e.getMessage(), "ERROR");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        log(stackTrace, "ERROR");

        if (uploadToDrive) {
            scheduleImmediateUpload();
        }
    }

    // Scrive in append sul file di log
    private void writeToFile(String text) {
        try (FileOutputStream fos = context.openFileOutput(logFileName, Context.MODE_APPEND)) {
            fos.write(text.getBytes());
        } catch (Exception e) {
            Log.e("LoggerManager", "Errore nella scrittura del log: " + e.getMessage());
        }
    }

    // Schedula l’upload tramite WorkManager
    private void scheduleImmediateUpload() {
        OneTimeWorkRequest uploadRequest = new OneTimeWorkRequest.Builder(LogUploadWorker.class).build();
        WorkManager.getInstance(context).enqueue(uploadRequest);
    }

    // Metodo invocato dal Worker per eseguire l'upload del log
    public void uploadLog() {
        try {
            File file = new File(context.getFilesDir(), logFileName);
            String content = readLogFile();
            if (content == null || content.isEmpty()) {
                log("Nessun log da caricare.", "DEBUG");
                return;
            }
            Log.d("LoggerManager", "Caricamento del log su Google Drive in corso...");
            boolean success = logUploader.uploadLogFile(file.getAbsolutePath());
            if (success) {
                log("Log file caricato su Google Drive con successo.", "INFO");
                clearLogFile();
            } else {
                log("Upload del log non riuscito.", "ERROR");
            }
        } catch (Exception e) {
            log("Errore nel caricamento del log su Google Drive: " + e.getMessage(), "ERROR");
        }
    }


    // Svuota il contenuto del file di log sovrascrivendolo con una stringa vuota
    public void clearLogFile() {
        try (FileOutputStream fos = context.openFileOutput(logFileName, Context.MODE_PRIVATE)) {
            // Apertura in MODE_PRIVATE sovrascrive il file, quindi scrivendo una stringa vuota lo svuoti
            fos.write("".getBytes());
            log("Contenuto del log svuotato.", "DEBUG");
        } catch (Exception e) {
            log("Errore nello svuotamento del log: " + e.getMessage(), "ERROR");
        }
    }

    // Legge il contenuto del file di log e lo restituisce come String
    public String readLogFile() {
        StringBuilder sb = new StringBuilder();
        try (FileInputStream fis = context.openFileInput(logFileName)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) != -1) {
                sb.append(new String(buffer, 0, length));
            }
        } catch (Exception e) {
            log("Errore nella lettura del log: " + e.getMessage(), "ERROR");
        }
        return sb.toString();
    }

}
