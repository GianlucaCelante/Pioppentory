package it.pioppi.business.manager;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.List;
import java.util.Objects;
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

    public static void clearFocusAndHideKeyboard(Context context, View view) {
        if (view != null) {
            view.clearFocus();
        }
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            View currentFocus = Objects.requireNonNull(view).getRootView().findFocus();
            if (currentFocus != null) {
                imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            }
        }
    }

}
