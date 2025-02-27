package it.pioppi.business.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import it.pioppi.R;
import it.pioppi.business.dto.ItemDetailDto;
import it.pioppi.business.dto.ItemDto;
import it.pioppi.business.dto.ItemTagDto;
import it.pioppi.database.typeconverters.Converters;

public class ItemsInTagAdapter extends RecyclerView.Adapter<ItemsInTagAdapter.ItemTagsViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(ItemDto item) throws ExecutionException, InterruptedException;
    }

    private List<ItemTagDto> itemTagsDtos;
    private List<ItemDto> itemDtos;
    private List<ItemDetailDto> itemDetailDtos;
    private final OnItemClickListener listener;
    private final Context context;

    public ItemsInTagAdapter(List<ItemTagDto> itemTagsDtos, List<ItemDto> itemDtos, List<ItemDetailDto> itemDetailDto, OnItemClickListener listener, Context context) {
        this.itemTagsDtos = itemTagsDtos;
        this.itemDtos = itemDtos;
        this.itemDetailDtos = itemDetailDto;
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
                .filter(itemDetailDto1 -> itemDetailDto1.getItemId().equals(itemDto.getId()))
                .findFirst()
                .orElse(null);

        holder.tagName.setText(itemDto.getName() != null ? itemDto.getName() : "");
        holder.totPortions.setText(String.valueOf(itemDto.getTotPortions()));

        if(itemDetailDto != null) {
            holder.portionsNeededOnWeekendTextView.setText(String.valueOf(itemDetailDto.getPortionsPerWeekend()));
            holder.lastUpdate.setText(Converters.toDateString(itemDetailDto.getLastUpdateDate()));
        } else {
            holder.portionsNeededOnWeekendTextView.setText("");
            holder.lastUpdate.setText("");
        }

        if (itemDetailDto != null) {
            if (itemDto.getTotPortions() >= itemDetailDto.getPortionsPerWeekend()) {
                holder.itemContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.green));
            } else {
                holder.itemContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.red));
            }
        }

        holder.itemContainer.setOnClickListener(v -> {
            try {
                listener.onItemClick(itemDto);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });

    }

    @Override
    public int getItemCount() {
        return this.itemDtos.size();
    }

    public void setItemTags(List<ItemTagDto> itemTagDtos) {
        this.itemTagsDtos = new ArrayList<>(itemTagDtos);
        notifyDataSetChanged();
    }

    public void setItemDtos(List<ItemDto> itemTagDtos) {
        this.itemDtos = new ArrayList<>(itemTagDtos);
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

        public ItemTagsViewHolder(View itemView) {
            super(itemView);
            itemContainer = itemView.findViewById(R.id.item_container);
            tagName = itemView.findViewById(R.id.text_item_name);
            portionsNeededOnWeekendTextView = itemView.findViewById(R.id.text_portions_per_weekend_value);
            totPortions = itemView.findViewById(R.id.text_tot_portions_value);
            lastUpdate = itemView.findViewById(R.id.text_last_update_value);

        }
    }
}
