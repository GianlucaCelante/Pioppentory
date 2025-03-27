package it.pioppi.business.adapter;


import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import it.pioppi.R;
import it.pioppi.business.dto.item.quantity.QuantityTypeDto;
import it.pioppi.business.manager.ItemDetailFragmentManager;
import it.pioppi.database.model.QuantityType;

public class EnumAdapter extends RecyclerView.Adapter<EnumAdapter.EnumViewHolder> {

    private List<QuantityTypeDto> quantityTypesDto;
    private final OnItemLongClickListener listener;
    private final OnTextChangeListener textChangeListener;

    public EnumAdapter(List<QuantityTypeDto> quantityTypesDto, OnItemLongClickListener listener, OnTextChangeListener textChangeListener) {
        this.quantityTypesDto = quantityTypesDto;
        this.listener = listener;
        this.textChangeListener = textChangeListener;
        sortQuantityTypes();
    }

    public interface OnItemLongClickListener {
        UUID onItemLongClick(QuantityTypeDto quantityTypeDto) throws ExecutionException, InterruptedException;
    }

    public interface OnTextChangeListener {
        void onTextChanged();
    }

    @NonNull
    @Override
    public EnumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_enum_quantity_type, parent, false);
        return new EnumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EnumViewHolder holder, int position) {
        QuantityTypeDto currentQuantityType = quantityTypesDto.get(position);
        holder.enumQuantityType.setText(currentQuantityType.getQuantityType().name());
        holder.enumTotPortions.setText(String.valueOf(currentQuantityType.getQuantity()));
        holder.enumUnitsPerQuantityType.setText(String.valueOf(currentQuantityType.getUnitsPerQuantityType()));

        holder.itemView.setOnLongClickListener((v -> {
            try {
                UUID deletedId = listener.onItemLongClick(currentQuantityType);
                int index = getItemPositionById(deletedId);
                if (index != RecyclerView.NO_POSITION) {
                    quantityTypesDto.remove(index);
                    sortQuantityTypes();
                    notifyDataSetChanged();

                }
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            return false;
        }));

        if (holder.totPortionsTextWatcher != null) {
            holder.enumTotPortions.removeTextChangedListener(holder.totPortionsTextWatcher);
        }

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Nothing needed here
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String normalizedText = ItemDetailFragmentManager.normalizeText(s.toString());

                if (!normalizedText.equals(s.toString())) {
                    // Save the cursor position
                    int selectionStart = holder.enumTotPortions.getSelectionStart();
                    int selectionEnd = holder.enumTotPortions.getSelectionEnd();

                    holder.enumTotPortions.removeTextChangedListener(this);
                    holder.enumTotPortions.setText(normalizedText);
                    holder.enumTotPortions.setSelection(Math.min(selectionStart, normalizedText.length()), Math.min(selectionEnd, normalizedText.length()));
                    holder.enumTotPortions.addTextChangedListener(this);
                }

                // Update the quantity in the QuantityTypeDto
                if (!normalizedText.isEmpty()) {
                    quantityTypesDto.get(holder.getBindingAdapterPosition()).setQuantity(Integer.parseInt(normalizedText));
                } else {
                    quantityTypesDto.get(holder.getBindingAdapterPosition()).setQuantity(0);
                }

                // Call the text change listener
                textChangeListener.onTextChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Nothing needed here
            }
        };

        holder.enumTotPortions.addTextChangedListener(textWatcher);
        holder.totPortionsTextWatcher = textWatcher;

        if(holder.unitsPerPackagingTextWatcher != null) {
            holder.enumUnitsPerQuantityType.removeTextChangedListener(holder.unitsPerPackagingTextWatcher);
        }

        TextWatcher unitsPerPackagingTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Nothing needed here
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String normalizedText = ItemDetailFragmentManager.normalizeText(s.toString());

                if (!normalizedText.equals(s.toString())) {
                    // Save the cursor position
                    int selectionStart = holder.enumUnitsPerQuantityType.getSelectionStart();
                    int selectionEnd = holder.enumUnitsPerQuantityType.getSelectionEnd();

                    holder.enumUnitsPerQuantityType.removeTextChangedListener(this);
                    holder.enumUnitsPerQuantityType.setText(normalizedText);
                    holder.enumUnitsPerQuantityType.setSelection(Math.min(selectionStart, normalizedText.length()), Math.min(selectionEnd, normalizedText.length()));
                    holder.enumUnitsPerQuantityType.addTextChangedListener(this);
                }

                // Update the quantity in the QuantityTypeDto
                if (!normalizedText.isEmpty()) {
                    quantityTypesDto.get(holder.getBindingAdapterPosition()).setUnitsPerQuantityType(Integer.parseInt(normalizedText));
                } else {
                    quantityTypesDto.get(holder.getBindingAdapterPosition()).setUnitsPerQuantityType(0);
                }

                // Call the text change listener
                textChangeListener.onTextChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Nothing needed here
            }
        };

        holder.enumUnitsPerQuantityType.addTextChangedListener(unitsPerPackagingTextWatcher);
        holder.unitsPerPackagingTextWatcher = unitsPerPackagingTextWatcher;

    }



    @Override
    public int getItemCount() {
        return quantityTypesDto.size();
    }

    public void setQuantityTypes(List<QuantityTypeDto> quantityTypes) {
        this.quantityTypesDto = new ArrayList<>(quantityTypes);
        sortQuantityTypes();
        notifyDataSetChanged();
    }

    public List<QuantityTypeDto> getQuantityTypes() {
        return this.quantityTypesDto;
    }

    public static class EnumViewHolder extends RecyclerView.ViewHolder {
        TextView enumQuantityType;
        EditText enumTotPortions;
        EditText enumUnitsPerQuantityType;
        TextWatcher totPortionsTextWatcher;
        TextWatcher unitsPerPackagingTextWatcher;



        EnumViewHolder(View itemView) {
            super(itemView);
            enumQuantityType = itemView.findViewById(R.id.enum_quantity_type);
            enumTotPortions = itemView.findViewById(R.id.enum_tot_portions);
            enumUnitsPerQuantityType = itemView.findViewById(R.id.units_per_quantity_type_edit);
        }
    }

    private int getItemPositionById(UUID id) {
        for (int i = 0; i < quantityTypesDto.size(); i++) {
            if (quantityTypesDto.get(i).getId().equals(id)) {
                return i;
            }
        }
        return RecyclerView.NO_POSITION;
    }

    public boolean containsQuantityType(QuantityTypeDto quantityTypeDto) {
        for (QuantityTypeDto dto : quantityTypesDto) {
            if (dto.getQuantityType().equals(quantityTypeDto.getQuantityType()) && dto.getPurpose().equals(quantityTypeDto.getPurpose())) {
                return true;
            }
        }
        return false;
    }

    public void addQuantityType(QuantityTypeDto quantityTypeDto) {
        quantityTypesDto.add(quantityTypeDto);
        sortQuantityTypes();
        notifyDataSetChanged();
    }

    private void sortQuantityTypes() {
        quantityTypesDto.sort((o1, o2) -> {
            List<QuantityType> order = Arrays.asList(QuantityType.values());
            return Integer.compare(order.indexOf(o1.getQuantityType()), order.indexOf(o2.getQuantityType()));
        });
    }
}
