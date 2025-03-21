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
    public void uploadLogFile(String filePath) {
        File logFile = new File(filePath);
        String content = readFileContent(logFile);
        googleDriveManager.uploadFile(logFile.getName(), content, context, GoogleDriveManager.MIME_TYPE_TEXT);
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
