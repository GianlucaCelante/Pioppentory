package it.pioppi.business.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import it.pioppi.R;
import it.pioppi.business.dto.item.ItemDto;

public class PreviewItemsAdapter extends RecyclerView.Adapter<PreviewItemsAdapter.ViewHolder> {

    private final List<ItemDto> itemList;

    public PreviewItemsAdapter(List<ItemDto> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public PreviewItemsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_preview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PreviewItemsAdapter.ViewHolder holder, int position) {
        ItemDto item = itemList.get(position);
        String note = item.getNote() != null ? item.getNote() : "";
        holder.itemNameTextView.setText(item.getName());
        holder.itemTotPortionsTextView.setText("Totale Porzioni: " + item.getTotPortions());
        holder.itemNoteTextView.setText("Note: " + note);

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemNameTextView;
        TextView itemTotPortionsTextView;
        TextView itemNoteTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemNameTextView = itemView.findViewById(R.id.item_name_text_view);
            itemTotPortionsTextView = itemView.findViewById(R.id.item_tot_portions_text_view);
            itemNoteTextView = itemView.findViewById(R.id.item_note_text_view);
        }
    }
}

