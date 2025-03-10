package it.pioppi.business.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import it.pioppi.R;
import it.pioppi.business.dto.ItemDetailDto;
import it.pioppi.business.dto.ItemDto;
import it.pioppi.business.dto.ItemTagDto;

public class ItemTagsAdapter extends RecyclerView.Adapter<ItemTagsAdapter.ItemTagsViewHolder> {

    public interface OnItemClickListener {
        void onAddItemsToTag(ItemTagDto item);
    }

    private List<ItemTagDto> itemTagDtos;
    private List<ItemDto> itemDtos;
    private List<ItemDetailDto> itemDetailDtos;
    private final Map<UUID, Set<UUID>> itemTagJoins;
    private final OnItemClickListener listener;
    private final ItemsInTagAdapter.OnItemClickListener listenerInTags;
    private final Context context;

    public ItemTagsAdapter(List<ItemTagDto> itemTagDtos, List<ItemDto> itemDtos, List<ItemDetailDto> itemDetailDtos, Map<UUID, Set<UUID>> itemTagJoins, OnItemClickListener listener,
                           ItemsInTagAdapter.OnItemClickListener listenerInTags, Context context) {
        this.itemTagDtos = itemTagDtos;
        this.itemDtos = itemDtos;
        this.itemDetailDtos = itemDetailDtos;
        this.itemTagJoins = itemTagJoins;
        this.listener = listener;
        this.listenerInTags = listenerInTags;
        this.context = context;
    }

    @NonNull
    @Override
    public ItemTagsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item_tags, parent, false);
        return new ItemTagsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemTagsViewHolder holder, int position) {
        ItemTagDto itemTagDto = itemTagDtos.get(position);

        holder.tagName.setText(itemTagDto.getName() != null ? itemTagDto.getName() : "");
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> itemTagDto.setSelected(isChecked));
        holder.checkBox.setChecked(itemTagDto.isSelected());

        List<ItemDto> filteredItems = itemDtos.stream()
                .filter(item -> itemBelongsToTag(item, itemTagDto))
                .collect(Collectors.toList());

        List<ItemDetailDto> filteredDetails = itemDetailDtos.stream()
                .filter(detail -> filteredItems.stream().anyMatch(item -> item.getId().equals(detail.getItemId())))
                .collect(Collectors.toList());

        if (holder.recyclerView.getLayoutManager() == null) {
            holder.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        }

        ItemsInTagAdapter nestedAdapter = new ItemsInTagAdapter(itemTagDto, filteredItems, filteredDetails, listenerInTags, context);
        holder.nestedAdapter = nestedAdapter;
        holder.recyclerView.setAdapter(nestedAdapter);

        holder.tagName.setOnClickListener(v -> {
            if(holder.recyclerView.getVisibility() == View.VISIBLE){
                holder.recyclerView.setVisibility(View.GONE);
            } else {
                holder.recyclerView.setVisibility(View.VISIBLE);
            }
        });

        holder.addItemsToTagButton.setOnClickListener(v -> listener.onAddItemsToTag(itemTagDto));
    }

    @Override
    public int getItemCount() {
        return this.itemTagDtos.size();
    }

    public void setItemTagDtos(List<ItemTagDto> itemTagDtos) {
        this.itemTagDtos = new ArrayList<>(itemTagDtos);
        notifyDataSetChanged();
    }

    public void setItemDtos(List<ItemDto> itemDtos) {
        this.itemDtos = new ArrayList<>(itemDtos);
        notifyDataSetChanged();
    }

    public void setItemDetailDtos(List<ItemDetailDto> itemDetailDtos) {
        this.itemDetailDtos = new ArrayList<>(itemDetailDtos);
        notifyDataSetChanged();
    }

    private boolean itemBelongsToTag(ItemDto item, ItemTagDto tag) {
        Set<UUID> uuids = itemTagJoins.get(tag.getId());
        return uuids != null && uuids.contains(item.getId());
    }


    public static class ItemTagsViewHolder extends RecyclerView.ViewHolder {
        public TextView tagName;
        public MaterialCheckBox checkBox;
        public RecyclerView recyclerView;
        public ItemsInTagAdapter nestedAdapter;
        public ImageButton addItemsToTagButton;

        public ItemTagsViewHolder(View itemView) {
            super(itemView);
            tagName = itemView.findViewById(R.id.tag_name);
            checkBox = itemView.findViewById(R.id.selected_tag);
            recyclerView = itemView.findViewById(R.id.recycler_view_items_in_tag);
            addItemsToTagButton = itemView.findViewById(R.id.add_item_to_tag);
        }
    }
}
