package it.pioppi.business.manager;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import it.pioppi.business.dto.QuantityTypeDto;
import it.pioppi.database.model.QuantityPurpose;
import it.pioppi.database.model.QuantityType;

public class ItemDetailFragmentManager {

    public static Integer calculateTotPortions(List<QuantityTypeDto> quantityTypeDtos, QuantityPurpose purpose) {
        AtomicReference<Integer> totPortions = new AtomicReference<>(0);
        quantityTypeDtos.stream()
                .filter(quantityTypeDto -> purpose.equals(quantityTypeDto.getPurpose()))
                .forEach(quantityTypeDto -> totPortions.updateAndGet(v -> v + QuantityType.getTotPortions(quantityTypeDto.getQuantityType(), quantityTypeDto.getQuantity())));
        return totPortions.get();
    }

    public static String normalizeText(String text) {
        return text.replaceFirst("^0+(?!$)", "");
    }


}
