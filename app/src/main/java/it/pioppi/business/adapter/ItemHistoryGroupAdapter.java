package it.pioppi.business.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import it.pioppi.DateTimeUtils;
import it.pioppi.R;
import it.pioppi.business.dto.ItemHistoryGroupDto;

public class ItemHistoryGroupAdapter extends RecyclerView.Adapter<ItemHistoryGroupAdapter.GroupViewHolder> {

    private final List<ItemHistoryGroupDto> groupList;

    public ItemHistoryGroupAdapter(List<ItemHistoryGroupDto> groupList) {
        this.groupList = groupList;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        ItemHistoryGroupDto group = groupList.get(position);
        holder.textViewDate.setText(DateTimeUtils.formatForDisplay(group.getInventoryClosureDate()));

        // Set up the child RecyclerView
        ItemHistoryItemAdapter entryAdapter = new ItemHistoryItemAdapter(group.getItemHistories());
        holder.recyclerViewItems.setLayoutManager(new LinearLayoutManager(holder.recyclerViewItems.getContext(), LinearLayoutManager.HORIZONTAL, false));
        holder.recyclerViewItems.setAdapter(entryAdapter);
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDate;
        RecyclerView recyclerViewItems;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDate = itemView.findViewById(R.id.text_view_inventory_date);
            recyclerViewItems = itemView.findViewById(R.id.recycler_view_items_by_date);
        }
    }
}