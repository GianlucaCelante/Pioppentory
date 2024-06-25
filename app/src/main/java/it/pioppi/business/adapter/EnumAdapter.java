package it.pioppi.business.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import it.pioppi.R;
import it.pioppi.business.dto.ItemWithDetailAndProviderAndQuantityTypeDto;
import it.pioppi.business.dto.ItemWithDetailAndProviderDto;
import it.pioppi.business.dto.QuantityTypeDto;
import it.pioppi.database.model.QuantityType;

public class EnumAdapter extends RecyclerView.Adapter<EnumAdapter.EnumViewHolder> {

    private final List<QuantityTypeDto> quantityTypeDto;

    public EnumAdapter(List<QuantityTypeDto> quantityTypeDto) {
        this.quantityTypeDto = quantityTypeDto;
    }

    @NonNull
    @Override
    public EnumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_enum_quantity_type, parent, false);
        return new EnumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EnumViewHolder holder, int position) {
        QuantityTypeDto quantityType = quantityTypeDto.get(position);
        holder.enumQuantityType.setText(quantityType.getQuantityType().name());
        holder.enumTotPortions.setText(String.valueOf(quantityType.getQuantity()));
    }

    @Override
    public int getItemCount() {
        return quantityTypeDto.size();
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
}
