package it.pioppi.business.manager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import it.pioppi.DateTimeUtils;
import it.pioppi.business.dto.ItemHistoryGroupDto;
import it.pioppi.business.dto.ItemHistoryDto;

public class ExportToCsvManager {

    private static final String CSV_HEADER = "Data Chiusura,Nome,Fornitore,Quantità presente,Quantità ordinata,Fabbisogno weekend,Data di consegna,Note\n";

    /**
     * Genera il contenuto CSV a partire dalla lista di ItemHistoryGroupDto.
     *
     * @param groupList la lista dei gruppi di storici
     * @return il contenuto CSV come String
     */
    public static String generateCsvContent(List<ItemHistoryGroupDto> groupList) {
        StringBuilder csvContent = new StringBuilder();
        csvContent.append(CSV_HEADER);
        if (groupList != null) {
            for (ItemHistoryGroupDto group : groupList) {
                if (group == null || group.getInventoryClosureDate() == null) {
                    continue;
                }
                String closureDate = DateTimeUtils.formatForDisplay(group.getInventoryClosureDate());
                List<ItemHistoryDto> itemHistories = group.getItemHistories();
                if (itemHistories != null) {
                    for (ItemHistoryDto itemHistory : itemHistories) {
                        if (itemHistory != null && itemHistory.getItemName() != null) {
                            csvContent.append(closureDate)
                                    .append(",")
                                    .append(itemHistory.getItemName())
                                    .append("\n");
                        }
                    }
                }
            }
        }
        return csvContent.toString();
    }

    /**
     * Genera il contenuto CSV per un singolo gruppo.
     *
     * @param group il gruppo di storici
     * @return il contenuto CSV come String
     */
    public static String generateCsvContentForSingleGroup(ItemHistoryGroupDto group) {
        StringBuilder csvContent = new StringBuilder();
        csvContent.append(CSV_HEADER);
        if (group == null || group.getInventoryClosureDate() == null) {
            return csvContent.toString();
        }
        String closureDate = DateTimeUtils.formatForDisplay(group.getInventoryClosureDate());
        List<ItemHistoryDto> itemHistories = group.getItemHistories();
        if (itemHistories != null) {
            for (ItemHistoryDto itemHistory : itemHistories) {
                if (itemHistory != null && itemHistory.getItemName() != null) {
                    csvContent.append(closureDate)
                            .append(",")
                            .append(itemHistory.getItemName())
                            .append(",")
                            .append(itemHistory.getProviderName())
                            .append(",")
                            .append(itemHistory.getQuantityPresent())
                            .append(",")
                            .append(itemHistory.getQuantityOrdered())
                            .append(",")
                            .append(itemHistory.getPortionsPerWeekend())
                            .append(",")
                            .append(DateTimeUtils.formatForDisplay(itemHistory.getDeliveryDate()))
                            .append(",")
                            .append(itemHistory.getNote())
                            .append("\n");
                }
            }
        }
        return csvContent.toString();
    }

    public static String mergeCsvContents(String existingContent, String newCsvContent) {
        // Definisce l'header atteso
        String header = CSV_HEADER;

        // Rimuove l'header dal contenuto esistente e dal nuovo contenuto (se presente)
        String existingData = existingContent.startsWith(header) ? existingContent.substring(header.length()) : existingContent;
        String newData = newCsvContent.startsWith(header) ? newCsvContent.substring(header.length()) : newCsvContent;

        // Utilizza una LinkedHashMap per mantenere l'ordine (opzionale)
        // La chiave è il nome dell'item (campo 2, indice 1) e il valore è la riga completa
        Map<String, String> mergedRows = new LinkedHashMap<>();

        // Aggiunge le righe esistenti
        if (existingData != null && !existingData.trim().isEmpty()) {
            String[] existingRows = existingData.split("\n");
            for (String row : existingRows) {
                if (row.trim().isEmpty()) continue;
                String[] fields = row.split(",");
                if (fields.length < 2) continue; // salta righe incomplete
                String key = fields[1].trim(); // campo "Nome"
                mergedRows.put(key, row);
            }
        }

        // Aggiunge le nuove righe, sovrascrivendo quelle esistenti in caso di duplicato
        if (newData != null && !newData.trim().isEmpty()) {
            String[] newRows = newData.split("\n");
            for (String row : newRows) {
                if (row.trim().isEmpty()) continue;
                String[] fields = row.split(",");
                if (fields.length < 2) continue;
                String key = fields[1].trim();
                mergedRows.put(key, row); // se esiste già, il valore verrà sovrascritto
            }
        }

        // Ricostruisce il CSV unito: header + righe
        StringBuilder mergedCsv = new StringBuilder();
        mergedCsv.append(header);
        for (String row : mergedRows.values()) {
            mergedCsv.append(row).append("\n");
        }
        return mergedCsv.toString();
    }


}
