package it.pioppi.business.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import it.pioppi.DateTimeUtils;
import it.pioppi.R;
import it.pioppi.business.dto.ItemHistoryDto;

public class ItemHistoryItemAdapter extends RecyclerView.Adapter<ItemHistoryItemAdapter.EntryViewHolder> {

    private final List<ItemHistoryDto> itemHistoryList;

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
        holder.textViewQuantityPresent.setText("Quantità presente: " + itemHistory.getQuantityPresent());
        holder.textViewQuantityOrdered.setText("Quantità da ordinare: " + itemHistory.getQuantityOrdered());
        holder.textViewPortionsPerWeekend.setText("Fabbisogno weekend: " + itemHistory.getPortionsPerWeekend());
        holder.textViewDeliveryDate.setText("Data consegna: " + DateTimeUtils.formatForDisplay(itemHistory.getDeliveryDate()));
        holder.textViewProviderName.setText("Fornitore: " + itemHistory.getProviderName());
        holder.textViewNote.setText("Note: " + itemHistory.getNote());
    }

    @Override
    public int getItemCount() {
        return itemHistoryList.size();
    }

    public static class EntryViewHolder extends RecyclerView.ViewHolder {
        TextView textViewItemName;
        TextView textViewQuantityPresent;
        TextView textViewQuantityOrdered;
        TextView textViewNote;
        TextView textViewPortionsPerWeekend;
        TextView textViewDeliveryDate;
        TextView textViewProviderName;

        public EntryViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewItemName = itemView.findViewById(R.id.text_view_item_name);
            textViewQuantityPresent = itemView.findViewById(R.id.text_view_quantity_present);
            textViewQuantityOrdered = itemView.findViewById(R.id.text_view_quantity_ordered);
            textViewNote = itemView.findViewById(R.id.text_view_note);
            textViewPortionsPerWeekend = itemView.findViewById(R.id.text_view_portions_per_weekend);
            textViewDeliveryDate = itemView.findViewById(R.id.text_view_delivery_date);
            textViewProviderName = itemView.findViewById(R.id.text_view_provider_name);
        }
    }
}

