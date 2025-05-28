package it.pioppi.business.manager;

import android.content.Context;
import android.widget.LinearLayout;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import it.pioppi.R;
import it.pioppi.business.dto.item.ItemDto;
import it.pioppi.business.dto.item.quantity.QuantityTypeDto;
import it.pioppi.database.model.ItemStatus;
import it.pioppi.database.model.QuantityPurpose;
import it.pioppi.database.model.QuantityTypeEnum;

public class ItemUtilityManager {

    public static Long calculateTotPortions(List<QuantityTypeDto> quantityTypeDtos, QuantityPurpose purpose) {
        AtomicReference<Long> totPortions = new AtomicReference<>(0L);
        quantityTypeDtos.stream()
                .filter(quantityTypeDto -> purpose.equals(quantityTypeDto.getPurpose()))
                .forEach(quantityTypeDto -> totPortions.updateAndGet(v -> v + QuantityTypeEnum.calculateTotalPortions(quantityTypeDto.getUnitsPerQuantityType(), quantityTypeDto.getQuantity())));
        return totPortions.get();
    }

    public static String normalizeText(String text) {
        return text.replaceFirst("^0+(?!$)", "");
    }

    public static void updateItemStatus(Context context, ItemDto item, CardView cardView, Long totPortions, int weekendRequirement) {

        item.setTotPortions(totPortions);

        if (totPortions >= weekendRequirement) {
            item.setStatus(ItemStatus.GREEN);
            cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.green));
        } else {
            item.setStatus(ItemStatus.RED);
            cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.red));
        }
    }

    public static void updateItemStatus(Context context, ItemDto item, LinearLayout cardView, Long totPortions, int weekendRequirement) {

        item.setTotPortions(totPortions);

        if (totPortions >= weekendRequirement) {
            item.setStatus(ItemStatus.GREEN);
            cardView.setBackgroundColor(ContextCompat.getColor(context, R.color.green));
        } else {
            item.setStatus(ItemStatus.RED);
            cardView.setBackgroundColor(ContextCompat.getColor(context, R.color.red));
        }
    }

}
