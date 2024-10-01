package it.pioppi.business.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import it.pioppi.R;
import it.pioppi.business.dto.ItemHistoryDto;

public class ItemHistoryItemAdapter extends RecyclerView.Adapter<ItemHistoryItemAdapter.EntryViewHolder> {

    private List<ItemHistoryDto> itemHistoryList;

    public ItemHistoryItemAdapter(List<ItemHistoryDto> itemHistoryList) {
        this.itemHistoryList = itemHistoryList;
    }

    @NonNull
    @Override
    public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_item, parent, false);
        return new EntryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntryViewHolder holder, int position) {
        ItemHistoryDto itemHistory = itemHistoryList.get(position);
        holder.textViewItemName.setText(itemHistory.getItemName());
        holder.textViewQuantityPresent.setText("Quantity Present: " + itemHistory.getQuantityPresent());
        holder.textViewQuantityOrdered.setText("Quantity Ordered: " + itemHistory.getQuantityOrdered());
        holder.textViewNote.setText("Note: " + itemHistory.getNote());
    }

    @Override
    public int getItemCount() {
        return itemHistoryList.size();
    }

    static class EntryViewHolder extends RecyclerView.ViewHolder {
        TextView textViewItemName;
        TextView textViewQuantityPresent;
        TextView textViewQuantityOrdered;
        TextView textViewNote;

        public EntryViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewItemName = itemView.findViewById(R.id.text_view_item_name);
            textViewQuantityPresent = itemView.findViewById(R.id.text_view_quantity_present);
            textViewQuantityOrdered = itemView.findViewById(R.id.text_view_quantity_ordered);
            textViewNote = itemView.findViewById(R.id.text_view_note);
        }
    }
}

