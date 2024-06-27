package it.pioppi.business.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import it.pioppi.R;
import it.pioppi.business.dto.QuantityTypeDto;

public class EnumAdapter extends RecyclerView.Adapter<EnumAdapter.EnumViewHolder> {

    private List<QuantityTypeDto> quantityTypesDto;
    private final OnItemLongClickListener listener;

    public EnumAdapter(List<QuantityTypeDto> quantityTypesDto, OnItemLongClickListener listener) {
        this.quantityTypesDto = quantityTypesDto;
        this.listener = listener;
    }

    public interface OnItemLongClickListener {
        UUID onItemLongClick(QuantityTypeDto quantityTypeDto) throws ExecutionException, InterruptedException;
    }

    @NonNull
    @Override
    public EnumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_enum_quantity_type, parent, false);
        return new EnumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EnumViewHolder holder, int position) {
        QuantityTypeDto quantityType = quantityTypesDto.get(position);
        holder.enumQuantityType.setText(quantityType.getQuantityType().name());
        holder.enumTotPortions.setText(String.valueOf(quantityType.getQuantity()));

        holder.itemView.setOnClickListener(v -> {
            try {
                UUID deletedId = listener.onItemLongClick(quantityType);
                quantityTypesDto.removeIf(q -> q.getId().equals(deletedId));
                notifyItemRemoved(position);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });


    }

    @Override
    public int getItemCount() {
        return quantityTypesDto.size();
    }


    public static class EnumViewHolder extends RecyclerView.ViewHolder {
        TextView enumQuantityType;
        EditText enumTotPortions;

        EnumViewHolder(View itemView) {
            super(itemView);
            enumQuantityType = itemView.findViewById(R.id.enum_quantity_type);
            enumTotPortions = itemView.findViewById(R.id.enum_tot_portions);

        }
    }

    public void setQuantityTypes(List<QuantityTypeDto> quantityTypes) {
        this.quantityTypesDto = quantityTypes;
        notifyItemChanged(quantityTypes.size() - 1);
    }

}
