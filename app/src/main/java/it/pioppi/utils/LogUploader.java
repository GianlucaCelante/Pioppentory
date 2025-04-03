package it.pioppi.utils;

public interface LogUploader {
    /**
     * Carica il file di log su Google Drive.
     * @param filePath Il percorso assoluto del file di log.
     * @return true se l'upload è riuscito, false altrimenti.
     * @throws Exception in caso di errori gravi durante l'upload.
     */
    boolean uploadLogFile(String filePath) throws Exception;
}
