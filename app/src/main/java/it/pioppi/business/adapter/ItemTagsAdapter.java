package it.pioppi.business.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import it.pioppi.R;
import it.pioppi.business.dto.ItemDto;
import it.pioppi.business.dto.ItemTagDto;

public class ItemTagsAdapter extends RecyclerView.Adapter<ItemTagsAdapter.ItemTagsViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(ItemTagDto item) throws ExecutionException, InterruptedException;
    }

    private List<ItemTagDto> itemTagDtos;
    private final OnItemClickListener listener;
    private final Context context;

    public ItemTagsAdapter(List<ItemTagDto> itemTagDtos, OnItemClickListener listener, Context context) {
        this.itemTagDtos = itemTagDtos;
        this.listener = listener;
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
        holder.cardView.setOnClickListener(v -> {
            try {
                listener.onItemClick(itemTagDto);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.itemTagDtos.size();
    }

    public void setItemTagDtos(List<ItemTagDto> itemTagDtos) {
        this.itemTagDtos = new ArrayList<>(itemTagDtos);
        notifyDataSetChanged();
    }

    public static class ItemTagsViewHolder extends RecyclerView.ViewHolder {
        public TextView tagName;
        public MaterialCheckBox checkBox;
        public MaterialCardView cardView;

        public ItemTagsViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view_tag);
            tagName = itemView.findViewById(R.id.tag_name);
            checkBox = itemView.findViewById(R.id.selected_tag);

        }
    }
}
