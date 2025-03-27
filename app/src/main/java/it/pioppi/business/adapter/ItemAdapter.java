package it.pioppi.business.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.pioppi.R;
import it.pioppi.business.dto.item.ItemDto;

public class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_VIEW_TYPE = 0;
    private static final int HEADER_VIEW_TYPE = 1;

    public interface OnItemClickListener {
        void onItemClick(ItemDto item);
    }

    public interface OnLongItemClickListener {
        void onLongItemClick(ItemDto item);
    }

    private final List<Object> itemListWithHeaders;
    private final OnItemClickListener listener;
    private final OnLongItemClickListener longListener;
    private final Context context;

    public ItemAdapter(List<ItemDto> itemList, OnItemClickListener listener, OnLongItemClickListener longListener, Context context) {
        this.context = context;
        this.listener = listener;
        this.longListener = longListener;
        this.itemListWithHeaders = generateItemListWithHeaders(itemList);
    }

    private List<Object> generateItemListWithHeaders(List<ItemDto> items) {
        items.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
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

    public void setItemList(List<ItemDto> itemList) {
        itemListWithHeaders.clear();
        itemListWithHeaders.addAll(generateItemListWithHeaders(itemList));
        notifyDataSetChanged();
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
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            String headerText = (String) itemListWithHeaders.get(position);
            headerHolder.headerTitle.setText(headerText);
        } else {
            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            ItemDto item = (ItemDto) itemListWithHeaders.get(position);

            itemHolder.itemName.setText(item.getName());
            itemHolder.totPortions.setText(String.valueOf(item.getTotPortions()));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            String formattedDateTime = item.getCheckDate() != null ? item.getCheckDate().format(formatter) : "";
            itemHolder.checkDate.setText(formattedDateTime);

            itemHolder.hasNote.setVisibility(item.getNote() != null && !item.getNote().isEmpty() ? View.VISIBLE : View.INVISIBLE);

            itemHolder.itemView.setOnClickListener(v -> listener.onItemClick(item));
            itemHolder.itemView.setOnLongClickListener(v -> {
                longListener.onLongItemClick(item);
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
            }
        }
    }

    @Override
    public int getItemCount() {
        return itemListWithHeaders.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, totPortions, checkDate;
        ImageView hasNote, itemImage;
        MaterialCardView materialCardView;

        ItemViewHolder(View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.item_name_card);
            totPortions = itemView.findViewById(R.id.tot_portions);
            checkDate = itemView.findViewById(R.id.check_date);
            hasNote = itemView.findViewById(R.id.has_note);
            materialCardView = itemView.findViewById(R.id.card_item);
            itemImage = itemView.findViewById(R.id.item_image);
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