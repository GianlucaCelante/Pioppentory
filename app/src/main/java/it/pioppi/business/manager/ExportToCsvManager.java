package it.pioppi.business.manager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import it.pioppi.utils.DateTimeUtils;
import it.pioppi.business.dto.history.ItemHistoryGroupDto;
import it.pioppi.business.dto.history.ItemHistoryDto;

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

    public static String mergeCsvContents(String existingContent, String newContent) {
        if (existingContent == null || existingContent.isEmpty()) {
            return newContent;
        }
        if (newContent == null || newContent.isEmpty()) {
            return existingContent;
        }
        // Qui viene eseguita una semplice concatenazione con separazione a capo
        return existingContent + "\n" + newContent;
    }


}
