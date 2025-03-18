package it.pioppi.business.manager;

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
    }

    /**
     * Carica un file su Google Drive usando il fileMetadata fornito.
     *
     * @param fileMetadata Oggetto File contenente il nome, il MIME type e, se necessario, il parent.
     * @param fileContent  Contenuto del file in byte.
     * @param callback     Callback per il risultato dell'upload.
     */
    public void uploadFile(File fileMetadata, byte[] fileContent, UploadCallback callback) {
        executorService.execute(() -> {
            try {
                ByteArrayContent content = new ByteArrayContent(fileMetadata.getMimeType(), fileContent);
                File uploadedFile = driveService.files().create(fileMetadata, content)
                        .setFields("id")
                        .execute();
                callback.onUploadSuccess(uploadedFile.getId());
            } catch (IOException e) {
                e.printStackTrace();
                callback.onUploadFailed(e);
            }
        });
    }

    public interface UploadCallback {
        void onUploadSuccess(String fileId);
        void onUploadFailed(Exception e);
    }
}
