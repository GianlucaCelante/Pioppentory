package it.pioppi.business.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import it.pioppi.DateTimeUtils;
import it.pioppi.R;
import it.pioppi.business.dto.ItemDetailDto;
import it.pioppi.business.dto.ItemDto;
import it.pioppi.business.dto.ItemTagDto;

public class ItemsInTagAdapter extends RecyclerView.Adapter<ItemsInTagAdapter.ItemTagsViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(ItemDto item) throws ExecutionException, InterruptedException;
        void onRemoveItemFromTag(ItemDto item, ItemTagDto tag) throws ExecutionException, InterruptedException;
    }

    private final ItemTagDto currentTag;
    private List<ItemDto> itemDtos;
    private List<ItemDetailDto> itemDetailDtos;
    private final OnItemClickListener listener;
    private final Context context;

    public ItemsInTagAdapter(ItemTagDto currentTag, List<ItemDto> itemDtos, List<ItemDetailDto> itemDetailDtos, OnItemClickListener listener, Context context) {
        this.currentTag = currentTag;
        this.itemDtos = itemDtos;
        this.itemDetailDtos = itemDetailDtos;
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public ItemTagsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_items_in_tag, parent, false);
        return new ItemTagsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemTagsViewHolder holder, int position) {
        ItemDto itemDto = itemDtos.get(position);
        ItemDetailDto itemDetailDto = itemDetailDtos.stream()
                .filter(detail -> detail.getItemId().equals(itemDto.getId()))
                .findFirst()
                .orElse(null);

        holder.tagName.setText(itemDto.getName() != null ? itemDto.getName() : "");
        holder.totPortions.setText(String.valueOf(itemDto.getTotPortions()));

        if(itemDetailDto != null) {
            holder.portionsNeededOnWeekendTextView.setText(String.valueOf(itemDetailDto.getPortionsPerWeekend()));
            holder.lastUpdate.setText(DateTimeUtils.formatForDisplay(itemDetailDto.getLastUpdateDate()));
        } else {
            holder.portionsNeededOnWeekendTextView.setText("");
            holder.lastUpdate.setText("");
        }

        if (itemDetailDto != null) {
            int bgColor = itemDto.getTotPortions() >= itemDetailDto.getPortionsPerWeekend() ? R.color.green : R.color.red;
            holder.itemContainer.setBackgroundColor(ContextCompat.getColor(context, bgColor));
        }

        holder.itemContainer.setOnClickListener(v -> {
            try {
                listener.onItemClick(itemDto);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        holder.removeItemFromTagButton.setOnClickListener(v -> {
            try {
                listener.onRemoveItemFromTag(itemDto, currentTag);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.itemDtos.size();
    }

    public void setItemDtos(List<ItemDto> itemDtos) {
        this.itemDtos = new ArrayList<>(itemDtos);
        notifyDataSetChanged();
    }

    public void setItemDetailDtos(List<ItemDetailDto> itemDetailDtos) {
        this.itemDetailDtos = new ArrayList<>(itemDetailDtos);
        notifyDataSetChanged();
    }

    public static class ItemTagsViewHolder extends RecyclerView.ViewHolder {
        public ConstraintLayout itemContainer;
        public TextView tagName;
        public TextView portionsNeededOnWeekendTextView;
        public TextView totPortions;
        public TextView lastUpdate;
        public ImageButton removeItemFromTagButton;

        public ItemTagsViewHolder(View itemView) {
            super(itemView);
            itemContainer = itemView.findViewById(R.id.item_container);
            tagName = itemView.findViewById(R.id.text_item_name);
            portionsNeededOnWeekendTextView = itemView.findViewById(R.id.text_portions_per_weekend_value);
            totPortions = itemView.findViewById(R.id.text_tot_portions_value);
            lastUpdate = itemView.findViewById(R.id.text_last_update_value);
            removeItemFromTagButton = itemView.findViewById(R.id.remove_item_from_tag_button);
        }
    }
}
