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

import com.google.android.material.card.MaterialCardView;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import it.pioppi.R;
import it.pioppi.business.dto.ItemDto;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(ItemDto item);
    }

    public interface OnLongItemClickListener {
        void onLongItemClick(ItemDto item) throws ExecutionException, InterruptedException;
    }

    private List<ItemDto> itemList;
    private final OnItemClickListener listener;
    private final OnLongItemClickListener longListener;
    private final Context context;

    public ItemAdapter(List<ItemDto> itemList, OnItemClickListener listener, OnLongItemClickListener longListener, Context context) {
        this.itemList = itemList;
        this.listener = listener;
        this.longListener = longListener;
        this.context = context;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        ItemDto item = itemList.get(position);
        holder.itemName.setText(item.getName() != null ? item.getName() : "");
        holder.totPortions.setText(String.valueOf(item.getTotPortions() != null ? item.getTotPortions() : 0));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formattedDateTime = item.getCheckDate() != null ? item.getCheckDate().format(formatter) : "";
        holder.checkDate.setText(formattedDateTime);

        holder.hasNote.setVisibility(item.getNote() != null && !item.getNote().isEmpty() ? View.VISIBLE : View.INVISIBLE);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        holder.itemView.setOnLongClickListener(v -> {
            try {
                longListener.onLongItemClick(item);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            return true;
        });

        if (item.getStatus() != null) {
            switch (item.getStatus()) {
                case WHITE:
                    holder.status.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white));
                    break;
                case BLUE:
                    holder.status.setCardBackgroundColor(ContextCompat.getColor(context, R.color.blue));
                    break;
                case GREEN:
                    holder.status.setCardBackgroundColor(ContextCompat.getColor(context, R.color.green));
                    break;
                case RED:
                    holder.status.setCardBackgroundColor(ContextCompat.getColor(context, R.color.red));
                    break;
                default:
            }
        }
    }

    @Override
    public int getItemCount() {
        return this.itemList.size();
    }

    public List<ItemDto> getItemList() {
        return itemList;
    }

    public void setItemList(List<ItemDto> itemList) {
        this.itemList = new ArrayList<>(itemList);
        notifyDataSetChanged();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        public TextView itemName;
        public TextView totPortions;
        public TextView checkDate;
        public ImageView hasNote;
        public MaterialCardView status;

        public ItemViewHolder(View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.item_name_card);
            totPortions = itemView.findViewById(R.id.tot_portions);
            checkDate = itemView.findViewById(R.id.check_date);
            hasNote = itemView.findViewById(R.id.has_note);
            status = itemView.findViewById(R.id.card_item);
        }
    }
}
