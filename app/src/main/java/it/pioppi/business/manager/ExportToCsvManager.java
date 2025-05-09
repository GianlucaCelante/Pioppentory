package it.pioppi.business.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import it.pioppi.business.dto.item.quantity.QuantityTypeDto;
import it.pioppi.utils.DateTimeUtils;
import it.pioppi.business.dto.history.ItemHistoryGroupDto;
import it.pioppi.business.dto.history.ItemHistoryDto;

public class ExportToCsvManager {

    private static final String CSV_HEADER = "Data Chiusura,Nome,Fornitore,Quantità totale,Dettagli Quantità,Quantità ordinata,Fabbisogno weekend,Data di consegna,Note\n";

    private static final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    ;
    /**
     * Helper per "escapare" un campo CSV.
     * Se il campo contiene virgole, virgolette o newline, viene racchiuso tra doppi apici.
     *
     * @param field il valore del campo
     * @return il campo processato
     */
    private static String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
            field = field.replace("\"", "\"\"");
            return "\"" + field + "\"";
        }
        return field;
    }

    /**
     * Genera il contenuto CSV da più gruppi.
     */
    public static String generateCsvContent(List<ItemHistoryGroupDto> groupList) {
        StringBuilder csv = new StringBuilder(CSV_HEADER);
        if (groupList == null) return csv.toString();

        for (ItemHistoryGroupDto group : groupList) {
            if (group == null || group.getInventoryClosureDate() == null) continue;
            String closureDate = DateTimeUtils.formatForDisplayLocalDate(group.getInventoryClosureDate());

            for (ItemHistoryDto item : group.getItemHistories()) {
                if (item == null || item.getItemName() == null) continue;
                csv.append(buildCsvLine(closureDate, item));
            }
        }
        return csv.toString();
    }

    /**
     * Genera il contenuto CSV per un singolo gruppo.
     */
    public static String generateCsvContentForSingleGroup(ItemHistoryGroupDto group) {
        StringBuilder csv = new StringBuilder(CSV_HEADER);
        if (group == null || group.getInventoryClosureDate() == null) return csv.toString();
        String closureDate = DateTimeUtils.formatForDisplayLocalDate(group.getInventoryClosureDate());

        for (ItemHistoryDto item : group.getItemHistories()) {
            if (item == null || item.getItemName() == null) continue;
            csv.append(buildCsvLine(closureDate, item));
        }
        return csv.toString();
    }

    /**
     * Costruisce una singola riga CSV per un ItemHistoryDto.
     */
    private static String buildCsvLine(String closureDate, ItemHistoryDto item) {
        // Filtra solo i campi necessari
        List<Map<String, Object>> filtered = new ArrayList<>();
        if (item.getQuantityTypes() != null) {
            for (QuantityTypeDto qt : item.getQuantityTypes()) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("Tipo", qt.getDescription());
                map.put("Quantità", qt.getQuantity());
                map.put("Unità per Quantità", qt.getUnitsPerQuantityType());
                filtered.add(map);
            }
        }
        // Serializza in JSON
        String detailsJson;
        try {
            detailsJson = objectMapper.writeValueAsString(filtered);
        } catch (JsonProcessingException e) {
            detailsJson = "[]";
        }

        // Costruisci i campi comuni
        return escapeCsvField(closureDate) + "," +
                escapeCsvField(item.getItemName()) + "," +
                escapeCsvField(item.getProviderName()) + "," +
                (item.getQuantityPresent() != null ? item.getQuantityPresent() : 0) + "," +
                escapeCsvField(detailsJson) + "," +
                (item.getQuantityOrdered() != null ? item.getQuantityOrdered() : 0) + "," +
                (item.getPortionsPerWeekend() != null ? item.getPortionsPerWeekend() : 0) + "," +
                escapeCsvField(DateTimeUtils.formatForDisplayLocalDate(item.getDeliveryDate())) + "," +
                escapeCsvField(item.getNote()) + "\n";
    }


    /**
     * Parsing del CSV generato, ricostruisce i gruppi e le liste quantityTypes.
     */
    public static List<ItemHistoryGroupDto> parseCsvToGroups(String csvContent) throws IOException {
        if (csvContent == null || csvContent.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String headerLine = CSV_HEADER.trim();
        String[] allLines = csvContent.split("\\r?\\n");
        List<String> csvLines = new ArrayList<>();
        boolean foundHeader = false;
        for (String line : allLines) {
            if (!foundHeader) {
                if (line.trim().equals(headerLine)) {
                    foundHeader = true;
                    csvLines.add(line);
                }
            } else {
                csvLines.add(line);
            }
        }
        if (!foundHeader) {
            throw new IOException("Intestazione CSV non trovata: cercata `" + headerLine + "`");
        }
        String cleaned = String.join("\n", csvLines);
        CSVParser parser = CSVParser.parse(
                new StringReader(cleaned),
                CSVFormat.DEFAULT.withHeader().withSkipHeaderRecord()
        );

        Map<LocalDate, ItemHistoryGroupDto> map = new LinkedHashMap<>();
        TypeReference<List<Map<String, Object>>> typeRef = new TypeReference<List<Map<String, Object>>>(){};

        for (CSVRecord rec : parser) {
            LocalDate date = DateTimeUtils.parse(rec.get("Data Chiusura"));
            ItemHistoryDto dto = new ItemHistoryDto();
            dto.setInventoryClosureDate(date);
            dto.setItemName(rec.get("Nome"));
            dto.setProviderName(rec.get("Fornitore"));
            dto.setQuantityPresent(Long.valueOf(rec.get("Quantità totale")));
            String detailsJson = rec.get("Dettagli Quantità");
            List<Map<String, Object>> list = detailsJson != null && !detailsJson.isEmpty()
                    ? objectMapper.readValue(detailsJson, typeRef)
                    : Collections.emptyList();
            List<QuantityTypeDto> qts = new ArrayList<>();
            for (Map<String, Object> m : list) {
                QuantityTypeDto qt = new QuantityTypeDto();
                qt.setDescription((String) m.get("Tipo"));
                qt.setQuantity((Integer) m.get("Quantità"));
                qt.setUnitsPerQuantityType(((Integer) m.get("Unità per Quantità")));
                qts.add(qt);
            }
            dto.setQuantityTypes(qts);

            dto.setQuantityOrdered(Long.valueOf(rec.get("Quantità ordinata")));
            dto.setPortionsPerWeekend(Long.valueOf(rec.get("Fabbisogno weekend")));
            dto.setDeliveryDate(DateTimeUtils.parse(rec.get("Data di consegna")));
            dto.setNote(rec.get("Note"));

            ItemHistoryGroupDto group = map.computeIfAbsent(date, d -> {
                ItemHistoryGroupDto g = new ItemHistoryGroupDto();
                g.setInventoryClosureDate(d);
                g.setItemHistories(new ArrayList<>());
                return g;
            });
            group.getItemHistories().add(dto);
        }
        return new ArrayList<>(map.values());
    }


    /**
     * 2) Unisce due liste di gruppi, de-duplicando per itemName e
     *    prendendo sempre il DTO “newer” in caso di conflitto.
     */
    public static List<ItemHistoryGroupDto> mergeGroups( List<ItemHistoryGroupDto> existing, List<ItemHistoryGroupDto> newer) {

        Map<LocalDate, ItemHistoryGroupDto> map = new LinkedHashMap<>();

        // prepopolo con i gruppi esistenti (facendo una copia “leggera”)
        for (ItemHistoryGroupDto g : existing) {
            ItemHistoryGroupDto copy = new ItemHistoryGroupDto();
            copy.setInventoryClosureDate(g.getInventoryClosureDate());
            copy.setItemHistories(new ArrayList<>(g.getItemHistories()));
            map.put(g.getInventoryClosureDate(), copy);
        }

        // itero i gruppi nuovi
        for (ItemHistoryGroupDto g : newer) {
            LocalDate date = g.getInventoryClosureDate();

            // se è un data mai vista, la inserisco
            if (!map.containsKey(date)) {
                ItemHistoryGroupDto copy = new ItemHistoryGroupDto();
                copy.setInventoryClosureDate(date);
                copy.setItemHistories(new ArrayList<>(g.getItemHistories()));
                map.put(date, copy);

            } else {
                // merge con de‑duplicazione
                ItemHistoryGroupDto target = map.get(date);

                // creo mappa itemName → dto (mantengo quelli esistenti)
                Map<String, ItemHistoryDto> byName = Objects.requireNonNull(target).getItemHistories()
                        .stream()
                        .collect(Collectors.toMap(
                                ItemHistoryDto::getItemName,
                                Function.identity(),
                                (oldDto, newDto) -> oldDto  // in caso di doppione, mantengo il primo
                        ));

                // poi aggiungo/sovrascrivo con i nuovi DTO (più affidabili)
                for (ItemHistoryDto dto : g.getItemHistories()) {
                    byName.put(dto.getItemName(), dto);
                }

                // rimetto la lista aggiornata
                List<ItemHistoryDto> mergedList = new ArrayList<>(byName.values());
                mergedList.sort(Comparator.comparing(ItemHistoryDto::getItemName));
                target.setItemHistories(mergedList);

            }
        }

        return new ArrayList<>(map.values());
    }

    /**
     * 3) Nuovo metodo di merge “alto livello”:
     *    parse → mergeGroups → rigenera CSV con un unico header.
     */
    public static String mergeCsvContentsViaDto(String existingContent, String newContent) throws IOException {
        List<ItemHistoryGroupDto> oldGr = parseCsvToGroups(existingContent);
        List<ItemHistoryGroupDto> newGr = parseCsvToGroups(newContent);
        List<ItemHistoryGroupDto> merged = mergeGroups(oldGr, newGr);
        return generateCsvContent(merged);

    }



}