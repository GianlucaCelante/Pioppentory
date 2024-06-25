package it.pioppi.business.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import it.pioppi.R;
import it.pioppi.business.dto.ItemDto;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(ItemDto item);
    }

    private final List<ItemDto> itemList;
    private final OnItemClickListener listener;


    public ItemAdapter(List<ItemDto> itemList, OnItemClickListener listener) {
        this.itemList = itemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        ItemDto item = itemList.get(position);
        holder.itemName.setText(item.getName());
        holder.totPortions.setText(String.valueOf(item.getTotPortions()));
        holder.checkDate.setText(item.getCheckDate().toLocalDate().toString());
        holder.hasNote.setVisibility(item.hasNote() ? View.VISIBLE : View.INVISIBLE);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return this.itemList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        public TextView itemName;
        public TextView itemCountString;
        public TextView totPortions;
        public TextView checkDate;
        public ImageView hasNote;

        public ItemViewHolder(View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.item_name);
            itemCountString = itemView.findViewById(R.id.item_count_string);
            totPortions = itemView.findViewById(R.id.tot_portions);
            checkDate = itemView.findViewById(R.id.check_date);
            hasNote = itemView.findViewById(R.id.has_note);

            itemView.setOnClickListener(v -> {


            });
        }
    }
}

