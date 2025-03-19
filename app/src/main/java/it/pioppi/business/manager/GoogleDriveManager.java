package it.pioppi.business.manager;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

public class GoogleDriveManager {
    private static final String MIME_TYPE_CSV = "text/csv";
    private static final String FOLDER_NAME = "Pioppentory";
    private static final String MIME_TYPE_FOLDER = "application/vnd.google-apps.folder";

    private final Drive driveService;
    private final DriveServiceHelper driveServiceHelper;
    private String folderId; // ID della cartella "Pioppentory"

    public GoogleDriveManager(Drive driveService) {
        this.driveService = driveService;
        this.driveServiceHelper = new DriveServiceHelper(driveService);
    }

    /**
     * Verifica se la cartella "Pioppentory" esiste. Se non esiste, la crea.
     */
    public void ensureFolderExists() throws IOException {
        String query = "mimeType = '" + MIME_TYPE_FOLDER + "' and name = '" + FOLDER_NAME + "' and trashed = false";
        FileList result = driveService.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute();

        if (result.getFiles() != null && !result.getFiles().isEmpty()) {
            folderId = result.getFiles().get(0).getId();
        } else {
            File fileMetadata = new File();
            fileMetadata.setName(FOLDER_NAME);
            fileMetadata.setMimeType(MIME_TYPE_FOLDER);
            File folder = driveService.files().create(fileMetadata)
                    .setFields("id")
                    .execute();
            folderId = folder.getId();
        }
    }

    /**
     * Scarica il contenuto di un file dato il suo ID.
     *
     * @param fileId ID del file su Drive.
     * @return Il contenuto del file come String.
     * @throws IOException
     */
    private String downloadFileContent(String fileId) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
        return outputStream.toString("UTF-8");
    }

    /**
     * Esegue l'upload del CSV su Google Drive, inserendolo nella cartella "Pioppentory".
     * Se esiste già un file con lo stesso nome, unisce i contenuti e aggiorna il file.
     * Il file verrà creato con il nome specificato (es. "2025-03-17-report.csv").
     *
     * @param fileName   Il nome del file.
     * @param csvContent Il nuovo contenuto CSV.
     * @param context    Il contesto (usato per mostrare Toast sul main thread).
     */
    public void uploadCsv(String fileName, String csvContent, Context context) {
        new Thread(() -> {
            try {
                // Assicurati che la cartella esista
                ensureFolderExists();

                // Verifica se esiste già un file con lo stesso nome nella cartella
                String query = "name = '" + fileName + "' and '" + folderId + "' in parents and trashed = false";
                FileList fileList = driveService.files().list()
                        .setQ(query)
                        .setSpaces("drive")
                        .setFields("files(id, name)")
                        .execute();

                if (fileList.getFiles() != null && !fileList.getFiles().isEmpty()) {
                    // Il file esiste: scarica il contenuto e uniscilo con il nuovo CSV
                    String existingFileId = fileList.getFiles().get(0).getId();
                    String existingContent = downloadFileContent(existingFileId);
                    String mergedContent = ExportToCsvManager.mergeCsvContents(existingContent, csvContent);

                    // Aggiorna il file esistente con il contenuto unito
                    ByteArrayContent updatedContent = new ByteArrayContent(MIME_TYPE_CSV, mergedContent.getBytes());
                    File updatedFile = driveService.files().update(existingFileId, null, updatedContent)
                            .setFields("id")
                            .execute();
                    final String updatedFileId = updatedFile.getId();
                    ((Activity) context).runOnUiThread(() ->
                            Toast.makeText(context, "CSV aggiornato su Drive: " + fileName, Toast.LENGTH_LONG).show());
                } else {
                    // Il file non esiste: crea un nuovo file con il parent impostato
                    byte[] fileContent = csvContent.getBytes();
                    File fileMetadata = new File();
                    fileMetadata.setName(fileName);
                    fileMetadata.setMimeType(MIME_TYPE_CSV);
                    if (folderId != null) {
                        fileMetadata.setParents(Collections.singletonList(folderId));
                    }
                    driveServiceHelper.uploadFile(fileMetadata, fileContent, new DriveServiceHelper.UploadCallback() {
                        @Override
                        public void onUploadSuccess(String fileId) {
                            ((Activity) context).runOnUiThread(() ->
                                    Toast.makeText(context, "CSV caricato su Drive: " + fileName, Toast.LENGTH_LONG).show());
                        }

                        @Override
                        public void onUploadFailed(Exception e) {
                            ((Activity) context).runOnUiThread(() ->
                                    Toast.makeText(context, "Errore durante l'upload su Drive", Toast.LENGTH_SHORT).show());
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
                ((Activity) context).runOnUiThread(() ->
                        Toast.makeText(context, "Errore nella creazione della cartella su Drive", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
