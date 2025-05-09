package it.pioppi.business.manager;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import it.pioppi.utils.ImageListCallback;
import it.pioppi.utils.ImageUploadCallback;
import it.pioppi.utils.LoggerManager;

public class GoogleDriveManager {
    public static final String MIME_TYPE_CSV = "text/csv";
    public static final String MIME_TYPE_TEXT = "text/plain";
    private static final String MIME_TYPE_IMAGE = "image/*";
    private static final String MIME_TYPE_FOLDER = "application/vnd.google-apps.folder";

    private static final String FOLDER_NAME = "Pioppentory";
    private static final String IMAGES_FOLDER_NAME = "images";


    private final Drive driveService;
    private final DriveServiceHelper driveServiceHelper;
    private String folderId; // ID della cartella "Pioppentory"
    private String imagesFolderId; // Cartella "Pioppentory/images"

    public GoogleDriveManager(Drive driveService) {
        this.driveService = driveService;
        this.driveServiceHelper = new DriveServiceHelper(driveService);
        LoggerManager.getInstance().log("GoogleDriveManager instantiated", "INFO");
    }

    // Metodo per garantire che la cartella principale "Pioppentory" esista
    public void ensureMainFolderExists() throws IOException {
        String query = "mimeType = '" + MIME_TYPE_FOLDER + "' and name = '" + FOLDER_NAME + "' and trashed = false";
        FileList result = driveService.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute();
        if (result.getFiles() != null && !result.getFiles().isEmpty()) {
            folderId = result.getFiles().get(0).getId();
            LoggerManager.getInstance().log("Folder '" + FOLDER_NAME + "' already exists with ID: " + folderId, "DEBUG");
        } else {
            LoggerManager.getInstance().log("Folder '" + FOLDER_NAME + "' not found. Creating new folder.", "DEBUG");
            File fileMetadata = new File();
            fileMetadata.setName(FOLDER_NAME);
            fileMetadata.setMimeType(MIME_TYPE_FOLDER);
            File folder = driveService.files().create(fileMetadata)
                    .setFields("id")
                    .execute();
            folderId = folder.getId();
            LoggerManager.getInstance().log("Folder created with ID: " + folderId, "DEBUG");
        }
    }

    // Metodo per garantire che la cartella "images" esista all'interno di "Pioppentory"
    public void ensureImagesFolderExists() throws IOException {
        // Assicurati che la cartella principale esista
        if (folderId == null) {
            ensureMainFolderExists();
        }
        if (imagesFolderId != null) return; // già inizializzata

        String query = "mimeType = '" + MIME_TYPE_FOLDER + "' and name = '" + IMAGES_FOLDER_NAME + "' and '" + folderId + "' in parents and trashed = false";
        FileList result = driveService.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute();
        if (result.getFiles() != null && !result.getFiles().isEmpty()) {
            imagesFolderId = result.getFiles().get(0).getId();
            LoggerManager.getInstance().log("Images folder already exists with ID: " + imagesFolderId, "DEBUG");
        } else {
            LoggerManager.getInstance().log("Images folder not found. Creating new folder.", "DEBUG");
            File fileMetadata = new File();
            fileMetadata.setName(IMAGES_FOLDER_NAME);
            fileMetadata.setMimeType(MIME_TYPE_FOLDER);
            fileMetadata.setParents(Collections.singletonList(folderId));
            File folder = driveService.files().create(fileMetadata)
                    .setFields("id")
                    .execute();
            imagesFolderId = folder.getId();
            LoggerManager.getInstance().log("Images folder created with ID: " + imagesFolderId, "DEBUG");
        }
    }

    /**
     * Scarica il contenuto di un file dato il suo ID.
     *
     * @param fileId ID del file su Drive.
     * @return Il contenuto del file come String.
     */
    private String downloadFileContent(String fileId) throws IOException {
        LoggerManager.getInstance().log("downloadFileContent started for fileId: " + fileId, "INFO");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
        String content = outputStream.toString(StandardCharsets.UTF_8.name());
        LoggerManager.getInstance().log("downloadFileContent completed (content length: " + content.length() + ")", "DEBUG");
        return content;
    }

    public void uploadLog(String fileName, String logContent, Context context) {
        LoggerManager.getInstance().log("uploadLog started for file: " + fileName, "INFO");

        new Thread(() -> {
            // 1) Assicuro che la cartella principale esista
            try {
                ensureMainFolderExists();
            } catch (IOException e) {
                LoggerManager.getInstance().logException(e);
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context,
                                "Errore creazione/verifica cartella Drive:\n" + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
                return;
            }

            try {
                // 2a) Controllo se esiste già un file con quel nome
                String query = "name = '" + fileName + "' and '" + folderId + "' in parents and trashed = false";
                FileList fileList = driveService.files()
                        .list()
                        .setQ(query)
                        .setSpaces("drive")
                        .setFields("files(id,name)")
                        .execute();

                if (fileList.getFiles() != null && !fileList.getFiles().isEmpty()) {
                    // 2b) File esistente → download + append
                    String existingId = fileList.getFiles().get(0).getId();
                    String existingContent = downloadFileContent(existingId);

                    // semplicemente appendo il nuovo log
                    String merged = existingContent
                            + (existingContent.endsWith("\n") ? "" : "\n")
                            + logContent;

                    ByteArrayContent updated = new ByteArrayContent(
                            MIME_TYPE_TEXT,
                            merged.getBytes(StandardCharsets.UTF_8)
                    );
                    driveService.files()
                            .update(existingId, null, updated)
                            .setFields("id")
                            .execute();

                    LoggerManager.getInstance().log("Log file aggiornato su Drive: " + fileName, "INFO");
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(context,
                                    "Log aggiornato su Drive: " + fileName,
                                    Toast.LENGTH_LONG
                            ).show()
                    );

                } else {
                    // 2c) File non esiste → upload nuovo
                    File meta = new File();
                    meta.setName(fileName);
                    meta.setMimeType(MIME_TYPE_TEXT);
                    meta.setParents(Collections.singletonList(folderId));

                    driveServiceHelper.uploadFile(meta, logContent.getBytes(StandardCharsets.UTF_8),
                            new DriveServiceHelper.UploadCallback() {
                                @Override
                                public void onUploadSuccess(String newFileId) {
                                    LoggerManager.getInstance().log(
                                            "Log file caricato con successo: " + newFileId, "INFO"
                                    );
                                    new Handler(Looper.getMainLooper()).post(() ->
                                            Toast.makeText(context,
                                                    "Log caricato su Drive: " + fileName,
                                                    Toast.LENGTH_LONG
                                            ).show()
                                    );
                                }
                                @Override
                                public void onUploadFailed(Exception ex) {
                                    LoggerManager.getInstance().log(
                                            "Upload log fallito per file: " + fileName, "ERROR"
                                    );
                                    new Handler(Looper.getMainLooper()).post(() ->
                                            Toast.makeText(context,
                                                    "Upload log fallito: " + ex.getMessage(),
                                                    Toast.LENGTH_LONG
                                            ).show()
                                    );
                                }
                            }
                    );
                }

            } catch (IOException ioe) {
                // gestisce errori di I/O (list, download, update)
                LoggerManager.getInstance().logException(ioe);
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context,
                                "Errore I/O durante upload log su Drive:\n" + ioe.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
            }

            LoggerManager.getInstance().log("uploadLog completed for file: " + fileName, "INFO");
        }).start();
    }


    public void uploadFile(String fileName, String fileContent, Context context) {
        LoggerManager.getInstance().log("uploadFile started for file: " + fileName, "INFO");

        new Thread(() -> {
            // 1) Assicuro che la cartella principale esista
            try {
                ensureMainFolderExists();
            } catch (IOException e) {
                LoggerManager.getInstance().logException(e);
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context,
                                "Errore creazione/verifica cartella Drive:\n" + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
                return;
            }

            // 2) Provo a listare, fare merge e upload
            try {
                // 2a) Verifica esistenza del file
                String query = "name = '" + fileName + "' and '" + folderId + "' in parents and trashed = false";
                FileList fileList = driveService.files()
                        .list()
                        .setQ(query)
                        .setSpaces("drive")
                        .setFields("files(id,name)")
                        .execute();

                if (fileList.getFiles() != null && !fileList.getFiles().isEmpty()) {
                    // 2b) File esistente → download + merge
                    String existingId = fileList.getFiles().get(0).getId();
                    String existingContent = downloadFileContent(existingId);

                    String mergedContent;
                    try {
                        mergedContent = ExportToCsvManager.mergeCsvContentsViaDto(existingContent, fileContent);
                    } catch (IOException e) {
                        throw new RuntimeException(
                                "Errore parsing CSV: controlla formato e header", e
                        );
                    }

                    // 2c) Upload del CSV unito
                    ByteArrayContent updated = new ByteArrayContent(
                            MIME_TYPE_CSV,
                            mergedContent.getBytes(StandardCharsets.UTF_8)
                    );
                    driveService.files()
                            .update(existingId, null, updated)
                            .setFields("id")
                            .execute();

                    LoggerManager.getInstance().log(
                            "File aggiornato con successo su Drive: " + fileName, "INFO"
                    );
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(context,
                                    "File aggiornato su Drive: " + fileName,
                                    Toast.LENGTH_LONG
                            ).show()
                    );

                } else {
                    // 2d) File non esiste → upload nuovo
                    File meta = new File();
                    meta.setName(fileName);
                    meta.setMimeType(MIME_TYPE_CSV);
                    meta.setParents(Collections.singletonList(folderId));

                    driveServiceHelper.uploadFile(meta, fileContent.getBytes(StandardCharsets.UTF_8),
                            new DriveServiceHelper.UploadCallback() {
                                @Override
                                public void onUploadSuccess(String newFileId) {
                                    LoggerManager.getInstance().log(
                                            "File caricato con successo su Drive: " + newFileId, "INFO"
                                    );
                                    new Handler(Looper.getMainLooper()).post(() ->
                                            Toast.makeText(context,
                                                    "File caricato su Drive: " + fileName,
                                                    Toast.LENGTH_LONG
                                            ).show()
                                    );
                                }
                                @Override
                                public void onUploadFailed(Exception ex) {
                                    LoggerManager.getInstance().log(
                                            "Upload fallito per file: " + fileName, "ERROR"
                                    );
                                    new Handler(Looper.getMainLooper()).post(() ->
                                            Toast.makeText(context,
                                                    "Upload fallito: " + ex.getMessage(),
                                                    Toast.LENGTH_LONG
                                            ).show()
                                    );
                                }
                            }
                    );
                }

            } catch (RuntimeException rte) {
                // qui finiscono gli errori di parsing CSV rilanciati sopra
                LoggerManager.getInstance().logException(rte);
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context,
                                "Configurazione parser CSV errata:\n" + rte.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
                return;

            } catch (IOException ioe) {
                // qui finiscono gli errori di I/O (list, download, update)
                LoggerManager.getInstance().logException(ioe);
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context,
                                "Errore I/O durante upload su Drive:\n" + ioe.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
                return;
            }

            LoggerManager.getInstance().log("uploadFile completed for file: " + fileName, "INFO");
        }).start();
    }



    public void uploadImage(String fileName, byte[] imageContent, ImageUploadCallback callback) {
        new Thread(() -> {
            try {
                ensureImagesFolderExists();
                LoggerManager.getInstance().log("Uploading image '" + fileName + "' in folder 'images'", "INFO");
                File fileMetadata = new File();
                fileMetadata.setName(fileName);
                fileMetadata.setMimeType(MIME_TYPE_IMAGE);
                fileMetadata.setParents(Collections.singletonList(imagesFolderId));
                driveServiceHelper.uploadFile(fileMetadata, imageContent, new DriveServiceHelper.UploadCallback() {
                    @Override
                    public void onUploadSuccess(String fileId) {
                        LoggerManager.getInstance().log("Image uploaded successfully. File ID: " + fileId, "INFO");
                        // Imposta i permessi pubblici
                        boolean permissionSet = setPublicPermission(fileId);
                        if (!permissionSet) {
                            // Se vuoi gestire il caso di fallimento nell'impostazione dei permessi, puoi farlo qui.
                            LoggerManager.getInstance().log("Non sono riuscito a impostare i permessi pubblici per il file: " + fileId, "ERROR");
                        }
                        callback.onSuccess(fileId);
                    }
                    @Override
                    public void onUploadFailed(Exception e) {
                        LoggerManager.getInstance().log("Image upload failed for file: " + fileName, "ERROR");
                        callback.onFailure(e);
                    }
                });
            } catch (Exception e) {
                LoggerManager.getInstance().log("Error uploading image: " + e.getMessage(), "ERROR");
                callback.onFailure(e);
            }
        }).start();
    }


    public void listImages(ImageListCallback callback) {
        new Thread(() -> {
            try {
                ensureImagesFolderExists();
                Log.d("GoogleDriveManager", "imagesFolderId: " + imagesFolderId);
                String query = "mimeType contains 'image/' and '" + imagesFolderId + "' in parents and trashed = false";
                Log.d("GoogleDriveManager", "Query: " + query);
                FileList result = driveService.files().list()
                        .setQ(query)
                        .setSpaces("drive")
                        .setFields("files(id, name, webContentLink, mimeType)")
                        .execute();
                Log.d("GoogleDriveManager", "Files trovati: " + (result.getFiles() != null ? result.getFiles().size() : 0));
                if(result.getFiles() != null) {
                    for (com.google.api.services.drive.model.File f : result.getFiles()) {
                        Log.d("GoogleDriveManager", "File: " + f.getName() + ", mimeType: " + f.getMimeType());
                    }
                }
                callback.onResult(result.getFiles());
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    /**
     * Imposta i permessi pubblici sul file per renderlo accessibile senza autenticazione.
     * @param fileId ID del file su Drive.
     * @return true se i permessi sono stati impostati correttamente, false altrimenti.
     */
    public boolean setPublicPermission(String fileId) {
        try {
            Permission permission = new Permission();
            permission.setRole("reader");
            permission.setType("anyone");
            driveService.permissions().create(fileId, permission).execute();
            LoggerManager.getInstance().log("Permessi impostati per il file: " + fileId, "DEBUG");
            return true;
        } catch (IOException e) {
            LoggerManager.getInstance().log("Errore nell'impostazione dei permessi per il file: " + fileId + " - " + e.getMessage(), "ERROR");
            return false;
        }
    }


}
