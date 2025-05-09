package it.pioppi.utils;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import it.pioppi.business.manager.GoogleDriveManager;

public class GoogleDriveLogUploader implements LogUploader {
    private final GoogleDriveManager googleDriveManager;
    private final Context context;

    public GoogleDriveLogUploader(GoogleDriveManager googleDriveManager, Context context) {
        this.googleDriveManager = googleDriveManager;
        this.context = context;
    }

    @Override
    public boolean uploadLogFile(String filePath) {
        try {

            File file = new File(filePath);
            googleDriveManager.uploadLog(file.getName(),
                    readFileContent(file),
                    context);
            LoggerManager.getInstance().log("GoogleDriveLogUploader", "Caricamento del file " + filePath + " su Google Drive...");
            return true;
        } catch (Exception e) {
            LoggerManager.getInstance().log("GoogleDriveLogUploader", "Errore durante l'upload: " + e.getMessage());
            return false;
        }
    }

    // Legge il contenuto del file in una stringa
    private String readFileContent(File file) {
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, read);
            }
            return bos.toString(StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            LoggerManager.getInstance().logException(e);
            return "";
        }
    }
}
