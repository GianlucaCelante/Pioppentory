package it.pioppi.business.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import it.pioppi.R;
import it.pioppi.business.dto.item.ItemDto;
import it.pioppi.business.dto.item.detail.ItemDetailDto;
import it.pioppi.business.dto.item.quantity.QuantityTypeDto;
import it.pioppi.business.dto.provider.ProviderDto;
import it.pioppi.business.manager.ItemUtilityManager;
import it.pioppi.database.model.QuantityPurpose;
import it.pioppi.database.model.QuantityTypeEnum;
import it.pioppi.utils.SwipeGestureListener;

public class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_VIEW_TYPE = 0;
    private static final int HEADER_VIEW_TYPE = 1;

    public interface OnItemClickListener {
        void onItemClick(ItemDto item);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(ItemDto item, View anchor);
    }

    public interface OnQuantityTypeChangeListener {
        void onQuantityTypeChanged(QuantityTypeDto updatedQuantityType);
    }

    private final List<Object> itemListWithHeaders = new ArrayList<>();
    private List<QuantityTypeDto> quantityTypes = new ArrayList<>();
    private List<ItemDetailDto> itemDetailsDto = new ArrayList<>();
    private final OnItemClickListener listener;
    private final OnItemLongClickListener longListener;
    private final OnQuantityTypeChangeListener quantityTypeChangeListener;
    private final Context context;
    private final Map<UUID, Integer> currentQuantityIndices = new HashMap<>();
    private boolean groupByProvider = false;



    public ItemAdapter(List<ItemDto> itemList, List<ProviderDto> providers, OnItemClickListener listener, OnItemLongClickListener longListener, OnQuantityTypeChangeListener quantityTypeChangeListener, Context context) {
        this.context = context;
        this.listener = listener;
        this.longListener = longListener;
        this.quantityTypeChangeListener = quantityTypeChangeListener;
        setHasStableIds(true);
        setItemList(itemList, providers);
    }

    private List<Object> generateItemListWithHeaders(List<ItemDto> items) {
        List<Object> itemListWithHeaders = new ArrayList<>();
        char currentHeader = 0;
        for (ItemDto item : items) {
            char firstChar = item.getName().toUpperCase(Locale.ROOT).charAt(0);
            if (currentHeader != firstChar) {
                currentHeader = firstChar;
                itemListWithHeaders.add(String.valueOf(currentHeader));
            }
            itemListWithHeaders.add(item);
        }
        return itemListWithHeaders;
    }

    @Override
    public int getItemViewType(int position) {
        return itemListWithHeaders.get(position) instanceof String ? HEADER_VIEW_TYPE : ITEM_VIEW_TYPE;
    }

    @Override
    public long getItemId(int position) {
        Object item = itemListWithHeaders.get(position);
        if (item instanceof String) {
            return item.hashCode();
        } else if (item instanceof ItemDto) {
            return ((ItemDto) item).getId().hashCode();
        }
        return position;
    }

    public List<Object> getItemListWithHeaders() {
        return new ArrayList<>(itemListWithHeaders);
    }

    public void setItemList(List<ItemDto> itemList, List<ProviderDto> allProviders) {
        if (itemList == null) return;
        itemListWithHeaders.clear();
        if (groupByProvider) {
            // usa il raggruppamento per provider
            itemListWithHeaders.addAll(generateItemListByProvider(itemList, allProviders));
        } else {
            // raggruppamento alfabetico
            itemListWithHeaders.addAll(generateItemListWithHeaders(itemList));
        }
        notifyDataSetChanged();
    }


    public void setQuantityTypes(List<QuantityTypeDto> quantityTypes) {
        this.quantityTypes = quantityTypes;
        notifyDataSetChanged();
    }

    public void setItemDetails(List<ItemDetailDto> itemDetailsDto) {
        this.itemDetailsDto = itemDetailsDto;
        notifyDataSetChanged();
    }

    public void setGroupByProvider(boolean groupByProvider) {
        this.groupByProvider = groupByProvider;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == HEADER_VIEW_TYPE) {
            View headerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sticky_header, parent, false);
            return new HeaderViewHolder(headerView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
            return new ItemViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position,
                                 @NonNull List<Object> payloads) {
        if (!payloads.isEmpty() && holder instanceof ItemViewHolder) {
            //  ✓ Qui gestisci solo la parte di UI che cambia:
            QuantityTypeDto updatedQt = (QuantityTypeDto) payloads.get(0);
            ItemViewHolder h = (ItemViewHolder) holder;

            // Ricava la lista dei quantityType per questo item
            List<QuantityTypeDto> list = quantityTypes.stream()
                    .filter(q -> q.getItemId().equals(updatedQt.getItemId())
                            && QuantityPurpose.AVAILABLE.equals(q.getPurpose()))
                    .collect(Collectors.toList());

            // Trova l’indice del quantityType aggiornato
            int idx = 0;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getId().equals(updatedQt.getId())) {
                    idx = i;
                    break;
                }
            }
            h.currentQuantityTypeIndex = idx;

            // Aggiorna solo la UI di quantità e totPortions
            updateQuantityTypeUI(h, list);
            long tot = ItemUtilityManager.calculateTotPortions(list, QuantityPurpose.AVAILABLE);
            h.totPortions.setText(String.valueOf(tot));

        } else {
            // payloads è vuoto → fall back al binding completo
            onBindViewHolder(holder, position);
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            String headerText = (String) itemListWithHeaders.get(position);
            headerHolder.headerTitle.setText(headerText);
        } else {
            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            ItemDto item = (ItemDto) itemListWithHeaders.get(position);
            List<QuantityTypeDto> quantityTypeDtos = quantityTypes.stream()
                    .filter(q -> q.getItemId().equals(item.getId()) && QuantityPurpose.AVAILABLE.equals(q.getPurpose()))
                    .collect(Collectors.toList());

            itemHolder.itemName.setText(item.getName());
            itemHolder.totPortions.setText(String.valueOf(item.getTotPortions()));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            String formattedDateTime = item.getCheckDate() != null ? item.getCheckDate().format(formatter) : "";
            itemHolder.checkDate.setText(formattedDateTime);

            itemHolder.hasNote.setVisibility(item.getNote() != null && !item.getNote().isEmpty() ? View.VISIBLE : View.INVISIBLE);

            itemHolder.itemView.setOnClickListener(v -> listener.onItemClick(item));
            itemHolder.itemView.setOnLongClickListener(v -> {
                longListener.onItemLongClick(item, v);
                return true;
            });


            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                Glide.with(context).load(item.getImageUrl()).placeholder(R.drawable.placeholder_thin).into(itemHolder.itemImage);
            } else {
                itemHolder.itemImage.setImageResource(R.drawable.placeholder_thin);
            }

            if(item.getStatus() == null) {
                itemHolder.materialCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white));
            } else {
                switch (item.getStatus()) {
                    case WHITE:
                        itemHolder.materialCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white));
                        break;
                    case GREEN:
                        itemHolder.materialCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.green));
                        break;
                    case RED:
                        itemHolder.materialCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.red));
                        break;
                    default:
                        itemHolder.materialCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white));
                        break;
                }
            }

            if(item.isChecked()) {
                itemHolder.materialCardView.setStrokeWidth(10);
                itemHolder.materialCardView.setStrokeColor(ContextCompat.getColor(context, R.color.connected_device_background));
            } else {
                itemHolder.materialCardView.setStrokeWidth(0);
            }

            int currentIndex = currentQuantityIndices.getOrDefault(item.getId(), 0);
            if (currentIndex >= quantityTypeDtos.size()) {
                currentIndex = 0;
                currentQuantityIndices.put(item.getId(), currentIndex);
            }
            itemHolder.currentQuantityTypeIndex = currentIndex;

            updateQuantityTypeUI(itemHolder, quantityTypeDtos);

            GestureDetector gestureDetector = new GestureDetector(context, new SwipeGestureListener(new SwipeGestureListener.OnSwipeListener() {
                @Override
                public void onSwipeLeft() {
                    if (itemHolder.currentQuantityTypeIndex < quantityTypeDtos.size() - 1) {
                        itemHolder.currentQuantityTypeIndex++;
                        updateQuantityTypeUI(itemHolder, quantityTypeDtos);
                        currentQuantityIndices.put(item.getId(), itemHolder.currentQuantityTypeIndex);
                    }
                }
                @Override
                public void onSwipeRight() {
                    if (itemHolder.currentQuantityTypeIndex > 0) {
                        itemHolder.currentQuantityTypeIndex--;
                        updateQuantityTypeUI(itemHolder, quantityTypeDtos);
                        currentQuantityIndices.put(item.getId(), itemHolder.currentQuantityTypeIndex);
                    }
                }
            }));
            itemHolder.quantityTypeSection.setOnTouchListener((v, event) -> {
                gestureDetector.onTouchEvent(event);
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                        v.performClick();
                        break;
                }
                return true;
            });

            itemHolder.increaseQuantityType.setOnClickListener(v -> {
                if (quantityTypeDtos.isEmpty() || itemHolder.currentQuantityTypeIndex >= quantityTypeDtos.size()) {
                    return;
                }
                QuantityTypeDto currentQuantityType = quantityTypeDtos.get(itemHolder.currentQuantityTypeIndex);
                if (currentQuantityType == null) {
                    return;
                }
                int currentQuantity = currentQuantityType.getQuantity() != null ? currentQuantityType.getQuantity() : 0;
                currentQuantityType.setQuantity(currentQuantity + 1);
                updateQuantityTypeUI(itemHolder, quantityTypeDtos);
                currentQuantityIndices.put(item.getId(), itemHolder.currentQuantityTypeIndex);

                item.setChecked(true);

                Long totPortions = ItemUtilityManager.calculateTotPortions(quantityTypeDtos, QuantityPurpose.AVAILABLE);
                item.setTotPortions(totPortions);
                itemHolder.totPortions.setText(String.valueOf(totPortions));

                ItemDetailDto itemDetailDto = itemDetailsDto.stream()
                        .filter(detail -> detail.getItemId().equals(item.getId()))
                        .findFirst()
                        .orElse(null);

                int weekendRequirement = itemDetailDto != null && itemDetailDto.getPortionsPerWeekend() != null ? itemDetailDto.getPortionsPerWeekend() : 0;
                ItemUtilityManager.updateItemStatus(context, item, itemHolder.materialCardView, totPortions, weekendRequirement);


                if (quantityTypeChangeListener != null) {
                    quantityTypeChangeListener.onQuantityTypeChanged(currentQuantityType);
                }
            });

            itemHolder.decreaseQuantityType.setOnClickListener(v -> {
                if (quantityTypeDtos.isEmpty() || itemHolder.currentQuantityTypeIndex >= quantityTypeDtos.size()) {
                    return;
                }
                QuantityTypeDto currentQuantityType = quantityTypeDtos.get(itemHolder.currentQuantityTypeIndex);
                if (currentQuantityType == null) {
                    return;
                }
                int currentQuantity = currentQuantityType.getQuantity() != null ? currentQuantityType.getQuantity() : 0;
                if (currentQuantity > 0) {
                    currentQuantityType.setQuantity(currentQuantity - 1);
                    updateQuantityTypeUI(itemHolder, quantityTypeDtos);
                    currentQuantityIndices.put(item.getId(), itemHolder.currentQuantityTypeIndex);

                    item.setChecked(true);

                    Long totPortions = ItemUtilityManager.calculateTotPortions(quantityTypeDtos, QuantityPurpose.AVAILABLE);
                    item.setTotPortions(totPortions);
                    itemHolder.totPortions.setText(String.valueOf(totPortions));

                    ItemDetailDto itemDetailDto = itemDetailsDto.stream()
                            .filter(detail -> detail.getItemId().equals(item.getId()))
                            .findFirst()
                            .orElse(null);

                    int weekendRequirement = itemDetailDto != null ? itemDetailDto.getPortionsPerWeekend() : 0;
                    ItemUtilityManager.updateItemStatus(context, item, itemHolder.materialCardView, totPortions, weekendRequirement);

                    if (quantityTypeChangeListener != null) {
                        quantityTypeChangeListener.onQuantityTypeChanged(currentQuantityType);
                    }
                }
            });

        }
    }

    public void updateSingleQuantity(QuantityTypeDto updatedQt) {
        // 1. aggiorna la lista interna quantityTypes
        for (int i = 0; i < quantityTypes.size(); i++) {
            if (quantityTypes.get(i).getId().equals(updatedQt.getId())) {
                quantityTypes.set(i, updatedQt);
                break;
            }
        }

        // 2. trova la posizione dell’ItemDto corrispondente nella lista con header
        UUID itemId = updatedQt.getItemId();
        for (int pos = 0; pos < itemListWithHeaders.size(); pos++) {
            Object o = itemListWithHeaders.get(pos);
            if (o instanceof ItemDto && ((ItemDto) o).getId().equals(itemId)) {
                // 3. notifica solo quell’item
                notifyItemChanged(pos, updatedQt);
                return;
            }
        }
    }


    private void updateQuantityTypeUI(ItemViewHolder holder, List<QuantityTypeDto> quantityTypeDtos) {
        if (quantityTypeDtos == null || quantityTypeDtos.isEmpty()) {
            holder.quantityType.setText("");
            holder.quantityTypeValue.setText("0");
            holder.dotsIndicator.removeAllViews();
            return;
        }
        int index = holder.currentQuantityTypeIndex;

        // Aggiorna il nome del quantity type
        QuantityTypeEnum quantityTypeName = quantityTypeDtos.get(index).getQuantityType();
        if(quantityTypeName != null) {
            holder.quantityType.setText(quantityTypeName.getDescription());
        }

        // Aggiorna il valore
        Integer quantity = quantityTypeDtos.get(index).getQuantity() != null ? quantityTypeDtos.get(index).getQuantity() : 0;
        holder.quantityTypeValue.setText(String.valueOf(quantity));

        // Popola il dotsIndicator se necessario
        if (holder.dotsIndicator.getChildCount() != quantityTypeDtos.size()) {
            holder.dotsIndicator.removeAllViews();
            for (int i = 0; i < quantityTypeDtos.size(); i++) {
                View dot = new View(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(6), dpToPx(6));
                params.setMargins(dpToPx(2), 0, dpToPx(2), 0);
                dot.setLayoutParams(params);
                // Imposta lo stato iniziale: non selezionato
                dot.setBackgroundResource(R.drawable.dot_unselected);
                holder.dotsIndicator.addView(dot);
            }
        }

        // Aggiorna il dot indicator in base all'indice corrente
        int count = holder.dotsIndicator.getChildCount();
        for (int i = 0; i < count; i++) {
            View dot = holder.dotsIndicator.getChildAt(i);
            if (i == index) {
                dot.setBackgroundResource(R.drawable.dot_selected);
            } else {
                dot.setBackgroundResource(R.drawable.dot_unselected);
            }
        }
    }

    // Metodo di utilità per convertire dp in pixel
    private int dpToPx(int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private List<Object> generateItemListByProvider(List<ItemDto> items, List<ProviderDto> providers) {
        Map<UUID, String> providerNames = providers.stream()
                .collect(Collectors.toMap(ProviderDto::getId, ProviderDto::getName));

        // raggruppa items per providerId (null → “Senza fornitore”)
        Map<String, List<ItemDto>> grouped = new HashMap<>();
        for (ItemDto item : items) {
            String name = providerNames.getOrDefault(item.getProviderId(), "Senza fornitore");
            grouped.computeIfAbsent(name, k -> new ArrayList<>()).add(item);
        }

        // ordina i gruppi alfabeticamente per nome provider
        List<String> headers = new ArrayList<>(grouped.keySet());
        headers.sort(String.CASE_INSENSITIVE_ORDER);

        // ricompone la lista: per ogni header → header + suoi item (ordinati A→Z)
        List<Object> result = new ArrayList<>();
        for (String header : headers) {
            result.add(header);
            List<ItemDto> list = grouped.get(header);
            Objects.requireNonNull(list).sort(Comparator.comparing(ItemDto::getName, String.CASE_INSENSITIVE_ORDER));
            result.addAll(list);
        }
        return result;
    }


    @Override
    public int getItemCount() {
        return itemListWithHeaders.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, totPortions, checkDate, quantityType, quantityTypeValue;
        ImageView hasNote, itemImage;
        MaterialCardView materialCardView;
        LinearLayout quantityTypeSection    , dotsIndicator;
        ImageButton increaseQuantityType, decreaseQuantityType;
        int currentQuantityTypeIndex = 0;

        ItemViewHolder(View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.item_name_card);
            totPortions = itemView.findViewById(R.id.tot_portions);
            checkDate = itemView.findViewById(R.id.check_date);
            hasNote = itemView.findViewById(R.id.has_note);
            materialCardView = itemView.findViewById(R.id.card_item);
            itemImage = itemView.findViewById(R.id.item_image);
            quantityTypeSection = itemView.findViewById(R.id.quantity_type_section);
            quantityType = itemView.findViewById(R.id.tv_quantity_type);
            quantityTypeValue = itemView.findViewById(R.id.tv_quantity_value);
            dotsIndicator = itemView.findViewById(R.id.dots_indicator);
            increaseQuantityType = itemView.findViewById(R.id.btn_increase_quantity);
            decreaseQuantityType = itemView.findViewById(R.id.btn_decrease_quantity);
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerTitle;

        HeaderViewHolder(View itemView) {
            super(itemView);
            headerTitle = itemView.findViewById(R.id.sticky_header_textview);
        }
    }
}
