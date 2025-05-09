package it.pioppi.business.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Comparator;
import java.util.List;

import it.pioppi.business.dto.history.ItemHistoryDto;
import it.pioppi.utils.DateTimeUtils;
import it.pioppi.R;
import it.pioppi.business.dto.history.ItemHistoryGroupDto;
import it.pioppi.business.manager.ExportToCsvManager;
import it.pioppi.business.manager.GoogleDriveManager;

public class ItemHistoryGroupAdapter extends RecyclerView.Adapter<ItemHistoryGroupAdapter.GroupViewHolder> {

    public interface OnExportClickListener {
        void onExport(ItemHistoryGroupDto group);
    }

    private final List<ItemHistoryGroupDto> groupList;
    private final OnExportClickListener exportListener;

    public ItemHistoryGroupAdapter(List<ItemHistoryGroupDto> groupList, OnExportClickListener exportListener) {
        this.groupList = groupList;
        this.exportListener = exportListener;
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
        holder.textViewDate.setText(DateTimeUtils.formatForDisplayLocalDate(group.getInventoryClosureDate()));

        List<ItemHistoryDto> itemHistories = group.getItemHistories();
        itemHistories.sort(Comparator.comparing(ItemHistoryDto::getItemName));

        ItemHistoryItemAdapter entryAdapter = new ItemHistoryItemAdapter(itemHistories);
        holder.recyclerViewItems.setLayoutManager(new LinearLayoutManager(holder.recyclerViewItems.getContext(), LinearLayoutManager.HORIZONTAL, false));
        holder.recyclerViewItems.setAdapter(entryAdapter);

        holder.exportToExcelButton.setOnClickListener(v -> new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                .setTitle("Conferma esportazione")
                .setMessage("Sei sicuro di voler generare ed esportare il file CSV?")
                .setPositiveButton("Sì", (dialog, which) -> exportListener.onExport(group))
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show());

    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDate;
        RecyclerView recyclerViewItems;
        ImageButton exportToExcelButton;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDate = itemView.findViewById(R.id.text_view_inventory_date);
            recyclerViewItems = itemView.findViewById(R.id.recycler_view_items_by_date);
            exportToExcelButton = itemView.findViewById(R.id.export_to_excel);
        }
    }
}
