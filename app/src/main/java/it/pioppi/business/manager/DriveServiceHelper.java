package it.pioppi.business.manager;

import it.pioppi.utils.LoggerManager;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DriveServiceHelper {
    private final Drive driveService;
    private final ExecutorService executorService;

    public DriveServiceHelper(Drive driveService) {
        this.driveService = driveService;
        executorService = Executors.newSingleThreadExecutor();
        LoggerManager.getInstance().log("DriveServiceHelper instantiated", "INFO");
    }

    /**
     * Carica un file su Google Drive usando il fileMetadata fornito.
     *
     * @param fileMetadata Oggetto File contenente nome, MIME type e, se necessario, il parent.
     * @param fileContent  Contenuto del file in byte.
     * @param callback     Callback per il risultato dell'upload.
     */
    public void uploadFile(File fileMetadata, byte[] fileContent, UploadCallback callback) {
        LoggerManager.getInstance().log("uploadFile started for file: " + fileMetadata.getName(), "INFO");
        executorService.execute(() -> {
            try {
                ByteArrayContent content = new ByteArrayContent(fileMetadata.getMimeType(), fileContent);
                LoggerManager.getInstance().log("Starting file upload for file: " + fileMetadata.getName(), "DEBUG");
                File uploadedFile = driveService.files().create(fileMetadata, content)
                        .setFields("id")
                        .execute();
                LoggerManager.getInstance().log("Upload successful, fileId: " + uploadedFile.getId(), "INFO");
                callback.onUploadSuccess(uploadedFile.getId());
            } catch (IOException e) {
                LoggerManager.getInstance().logException(e);
                callback.onUploadFailed(e);
            }
        });
    }

    public interface UploadCallback {
        void onUploadSuccess(String fileId);
        void onUploadFailed(Exception e);
    }
}
