package it.pioppi.business;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import it.pioppi.R;
import it.pioppi.business.adapter.EnumAdapter;
import it.pioppi.business.dto.ItemDetailDto;
import it.pioppi.business.dto.ItemDto;
import it.pioppi.business.dto.ItemWithDetailAndProviderAndQuantityTypeDto;
import it.pioppi.business.dto.ItemWithDetailAndProviderDto;
import it.pioppi.business.dto.ProviderDto;
import it.pioppi.business.dto.QuantityTypeDto;
import it.pioppi.database.AppDatabase;
import it.pioppi.database.dao.ItemEntityDao;
import it.pioppi.database.mapper.EntityDtoMapper;
import it.pioppi.database.model.QuantityType;
import it.pioppi.database.model.entity.ItemWithDetailAndProviderAndQuantityTypeEntity;

public class ItemDetailFragment extends Fragment {


    private AppDatabase appDatabase;
    private ExecutorService executorService;
    private ItemWithDetailAndProviderAndQuantityTypeDto itemWithDetailAndProviderAndQuantityTypeDto;

    private UUID itemId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appDatabase = AppDatabase.getInstance(getContext());
        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_item_detail, container, false);

        Bundle bundle = getArguments();
        if (bundle != null) {
            itemId = UUID.fromString(bundle.getString("itemId"));

            try {
                itemWithDetailAndProviderAndQuantityTypeDto = fetchItemWithDetailAndProviderAndQuantityTypeById(itemId);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        prefillFields(view, itemWithDetailAndProviderAndQuantityTypeDto);

        RecyclerView recyclerView = view.findViewById(R.id.enum_quantity_type_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(new EnumAdapter(itemWithDetailAndProviderAndQuantityTypeDto.getQuantityType()));

        Button addQuantityTypeButton = view.findViewById(R.id.add_quantity_type);
        addQuantityTypeButton.setOnClickListener(v -> {
            addQuantityType(inflater, recyclerView);
        });


        return view;
    }

    private void addQuantityType(LayoutInflater inflater, RecyclerView recyclerView) {
        View dialogView = inflater.inflate(R.layout.dialog_add_quantity_type, null);

        // Access the Spinner and EditText from the inflated layout
        Spinner spinner = dialogView.findViewById(R.id.quantity_type_spinner);
        EditText quantityAvailable = dialogView.findViewById(R.id.quantity_type_available);

        // Set up the Spinner with an adapter
        ArrayAdapter<QuantityType> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, Arrays.asList(QuantityType.values()));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Build and show the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(dialogView)
                .setPositiveButton("Aggiungi", (dialog, id) -> {
                    QuantityTypeDto quantityTypeDto = new QuantityTypeDto();
                    quantityTypeDto.setId(UUID.randomUUID());
                    quantityTypeDto.setItemId(itemWithDetailAndProviderAndQuantityTypeDto.getItem().getId());

                    if (spinner.getSelectedItem() != null) {
                        quantityTypeDto.setQuantityType((QuantityType) spinner.getSelectedItem());
                        quantityTypeDto.setDescription(((QuantityType) spinner.getSelectedItem()).getDescription());
                    }

                    String quantityText = quantityAvailable.getText().toString();
                    if (!quantityText.isEmpty()) {
                            quantityTypeDto.setQuantity(Integer.parseInt(quantityText));
                    }

                    List<QuantityTypeDto> quantityTypes = itemWithDetailAndProviderAndQuantityTypeDto.getQuantityType() == null ? new ArrayList<>() : itemWithDetailAndProviderAndQuantityTypeDto.getQuantityType();
                    boolean matched = quantityTypes.stream().noneMatch(quantityType -> quantityType.getQuantityType().equals(quantityTypeDto.getQuantityType()));

                    if(matched) {
                        quantityTypes.add(quantityTypeDto);
                        Objects.requireNonNull(recyclerView.getAdapter()).notifyItemInserted(quantityTypes.size() - 1);
                        recyclerView.smoothScrollToPosition(quantityTypes.size() - 1);
                    }

                })
                .setNegativeButton("Cancel", (dialog, id) -> {
                    dialog.cancel();
                });

        builder.show();
    }


    private ItemWithDetailAndProviderAndQuantityTypeDto fetchItemWithDetailAndProviderAndQuantityTypeById(UUID itemId) throws ExecutionException, InterruptedException {

        ItemWithDetailAndProviderAndQuantityTypeDto itemWithDetailAndProviderAndQuantityTypeDto = new ItemWithDetailAndProviderAndQuantityTypeDto();

        Future<?> future = executorService.submit(() -> {
            ItemEntityDao entityDao = appDatabase.itemEntityDao();
            ItemWithDetailAndProviderAndQuantityTypeEntity itemWithDetailAndProviderAndQuantityType = entityDao.getItemsWithDetailsAndProviderAndQuantityType(itemId);
            Log.d("ItemDetailFragment", "ItemWithDetailAndProviderEntity: " + itemWithDetailAndProviderAndQuantityType);
            if (itemWithDetailAndProviderAndQuantityType != null) {

                ItemDto itemDto = null;
                if(itemWithDetailAndProviderAndQuantityType.item != null){
                    itemDto = EntityDtoMapper.entityToDto(itemWithDetailAndProviderAndQuantityType.item);
                }

                ItemDetailDto itemDetailDto = null;
                if (itemWithDetailAndProviderAndQuantityType.itemDetail != null){
                    itemDetailDto = EntityDtoMapper.detailEntityToDto(itemWithDetailAndProviderAndQuantityType.itemDetail, itemId);
                }

                ProviderDto providerDto = null;
                if(itemWithDetailAndProviderAndQuantityType.provider != null){
                    providerDto = EntityDtoMapper.entityToDto(itemWithDetailAndProviderAndQuantityType.provider);
                }


                List<QuantityTypeDto> quantityTypeDto = null;
                if(itemWithDetailAndProviderAndQuantityType.quantityType != null){
                    quantityTypeDto = EntityDtoMapper.entitiesToDtos(itemWithDetailAndProviderAndQuantityType.quantityType);
                }

                itemWithDetailAndProviderAndQuantityTypeDto.setItem(itemDto);
                itemWithDetailAndProviderAndQuantityTypeDto.setItemDetail(itemDetailDto);
                itemWithDetailAndProviderAndQuantityTypeDto.setProvider(providerDto);
                itemWithDetailAndProviderAndQuantityTypeDto.setQuantityType(quantityTypeDto);


            } else throw new IllegalArgumentException("Item not found");
        });

        future.get();

        return itemWithDetailAndProviderAndQuantityTypeDto;
    }

    protected void prefillFields(View view, ItemWithDetailAndProviderAndQuantityTypeDto itemWithDetailAndProviderAndQuantityTypeDto) {
        TextView providerNameTextView = view.findViewById(R.id.provider_name);
        if(itemWithDetailAndProviderAndQuantityTypeDto.getProvider() != null){
            providerNameTextView.setText(itemWithDetailAndProviderAndQuantityTypeDto.getProvider().getName());
        }

        if(itemWithDetailAndProviderAndQuantityTypeDto.getItem() != null){
            TextView itemNameTextView = view.findViewById(R.id.item_name);
            itemNameTextView.setText(itemWithDetailAndProviderAndQuantityTypeDto.getItem().getName());
        }

        ItemDetailDto itemDetail = itemWithDetailAndProviderAndQuantityTypeDto.getItemDetail();
        if (itemDetail != null) {

            TextView quantityToBeOrderedTextView = view.findViewById(R.id.quantity_to_be_ordered);
            quantityToBeOrderedTextView.setText(String.valueOf(itemDetail.getQuantityToBeOrdered()));

            CalendarView deliveryDateCalendarView = view.findViewById(R.id.delivery_date);
            LocalDateTime deliveryDate = itemDetail.getDeliveryDate();
            if(deliveryDate != null){
                deliveryDateCalendarView.setDate(deliveryDate.toEpochSecond(ZoneOffset.UTC));
            } else {
                deliveryDateCalendarView.setDate(LocalDate.now().toEpochDay());
            }

        }

    }

}