package it.pioppi.business.manager;

import java.util.List;
import it.pioppi.DateTimeUtils;
import it.pioppi.business.dto.ItemHistoryGroupDto;
import it.pioppi.business.dto.ItemHistoryDto;

public class ExportToCsvManager {

    private static final String CSV_HEADER = "Data Chiusura,Nome Item\n";

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
                            .append("\n");
                }
            }
        }
        return csvContent.toString();
    }
}
