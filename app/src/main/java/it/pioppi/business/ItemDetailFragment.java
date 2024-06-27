package it.pioppi.business;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import it.pioppi.R;
import it.pioppi.business.adapter.EnumAdapter;
import it.pioppi.business.dto.ItemDetailDto;
import it.pioppi.business.dto.ItemDto;
import it.pioppi.business.dto.ItemWithDetailAndProviderAndQuantityTypeDto;
import it.pioppi.business.dto.ProviderDto;
import it.pioppi.business.dto.QuantityTypeDto;
import it.pioppi.database.AppDatabase;
import it.pioppi.database.dao.ItemEntityDao;
import it.pioppi.database.dao.QuantityTypeEntityDao;
import it.pioppi.database.mapper.EntityDtoMapper;
import it.pioppi.database.model.QuantityPurpose;
import it.pioppi.database.model.QuantityType;
import it.pioppi.database.model.entity.ItemWithDetailAndProviderAndQuantityTypeEntity;
import it.pioppi.database.model.entity.QuantityTypeEntity;

public class ItemDetailFragment extends Fragment implements EnumAdapter.OnItemLongClickListener {


    private AppDatabase appDatabase;
    private ExecutorService executorService;
    private ItemWithDetailAndProviderAndQuantityTypeDto itemWithDetailAndProviderAndQuantityTypeDto;

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
            UUID itemId = UUID.fromString(bundle.getString("itemId"));

            try {
                itemWithDetailAndProviderAndQuantityTypeDto = fetchItemWithDetailAndProviderAndQuantityTypeById(itemId);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        prefillFields(view, itemWithDetailAndProviderAndQuantityTypeDto);
        setupRecyclerViewAndButtonForQuantityTypes(view, inflater, R.id.recycler_view_quantity_available, R.id.add_quantity_type_available, QuantityPurpose.AVAILABLE);
        setupRecyclerViewAndButtonForQuantityTypes(view, inflater, R.id.recycler_view_quantity_to_be_ordered, R.id.add_quantity_type_to_be_ordered, QuantityPurpose.TO_BE_ORDERED);


        // After setup
        Button calculateQuantityToBeOrderedButton = view.findViewById(R.id.calculate_quantity_to_be_ordered);
        calculateQuantityToBeOrderedButton.setOnClickListener(v -> calculateNeededPortions(view, itemWithDetailAndProviderAndQuantityTypeDto));

        Button resetCalculatedQuantityToeOrderedButton = view.findViewById(R.id.reset_quantity_to_be_ordered);
        resetCalculatedQuantityToeOrderedButton.setOnClickListener(v -> {


        });



        return view;
    }

    private void calculateNeededPortions(View view, ItemWithDetailAndProviderAndQuantityTypeDto itemWithDetailAndProviderAndQuantityTypeDto) {

        CardView itemNameAndTotPortionsCardView = view.findViewById(R.id.item_name_tot_portions);
        TextView totPortionsTextView = view.findViewById(R.id.tot_portions_detail);

        Integer portionsPerWeekend = itemWithDetailAndProviderAndQuantityTypeDto.getItemDetail().getPortionsPerWeekend();

        List<QuantityTypeDto> quantityTypeToBeOrdered = itemWithDetailAndProviderAndQuantityTypeDto.getQuantityType().stream()
                .filter(q -> QuantityPurpose.TO_BE_ORDERED.equals(q.getPurpose()))
                .collect(Collectors.toList());

        List<QuantityTypeDto> quantityTypeAvailable = itemWithDetailAndProviderAndQuantityTypeDto.getQuantityType().stream()
                .filter(q -> QuantityPurpose.AVAILABLE.equals(q.getPurpose()))
                .collect(Collectors.toList());

        Integer totPortionsAvailable = calculateTotPortions(quantityTypeAvailable, QuantityPurpose.AVAILABLE);
        Integer totPortionsToBeOrdered = calculateTotPortions(quantityTypeToBeOrdered, QuantityPurpose.TO_BE_ORDERED);
        Integer totPortions = totPortionsAvailable + totPortionsToBeOrdered;
        totPortionsTextView.setText(String.valueOf(totPortions));

        if(totPortions >= portionsPerWeekend){
            itemNameAndTotPortionsCardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green));
        } else {
            itemNameAndTotPortionsCardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red));
        }


    }

    private void setupRecyclerViewAndButtonForQuantityTypes(View view, LayoutInflater inflater, int recyclerViewId, int buttonId, QuantityPurpose purpose) {
        List<QuantityTypeDto> filteredQuantityTypes = itemWithDetailAndProviderAndQuantityTypeDto.getQuantityType().stream()
                .filter(quantityTypeDto -> purpose.equals(quantityTypeDto.getPurpose()))
                .collect(Collectors.toList());

        RecyclerView recyclerView = view.findViewById(recyclerViewId);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(new EnumAdapter(filteredQuantityTypes, this));

        Button addButton = view.findViewById(buttonId);
        addButton.setOnClickListener(v -> addQuantityType(inflater, recyclerView, purpose));
    }

    private void addQuantityType(LayoutInflater inflater, RecyclerView recyclerView, QuantityPurpose purpose) {
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
                    quantityTypeDto.setPurpose(purpose);
                    quantityTypeDto.setCreationDate(LocalDateTime.now());

                    if (spinner.getSelectedItem() != null) {
                        quantityTypeDto.setQuantityType((QuantityType) spinner.getSelectedItem());
                        quantityTypeDto.setDescription(((QuantityType) spinner.getSelectedItem()).getDescription());
                    }

                    String quantityText = quantityAvailable.getText().toString();
                    if (!quantityText.isEmpty()) {
                            quantityTypeDto.setQuantity(Integer.parseInt(quantityText));
                    } else {
                        quantityTypeDto.setQuantity(0);
                    }

                    List<QuantityTypeDto> quantityTypes = itemWithDetailAndProviderAndQuantityTypeDto.getQuantityType() == null ? new ArrayList<>() : itemWithDetailAndProviderAndQuantityTypeDto.getQuantityType();
                    boolean matched = quantityTypes.stream()
                            .filter(quantityType -> purpose.equals(quantityType.getPurpose()))
                            .noneMatch(quantityType -> quantityType.getQuantityType().equals(quantityTypeDto.getQuantityType()));

                    if(matched) {
                        quantityTypes.add(quantityTypeDto);
                        itemWithDetailAndProviderAndQuantityTypeDto.setQuantityType(quantityTypes);

                        EnumAdapter enumAdapter = (EnumAdapter) recyclerView.getAdapter();
                        Objects.requireNonNull(enumAdapter).setQuantityTypes(quantityTypes.stream()
                                .filter(q -> purpose.equals(q.getPurpose()))
                                .collect(Collectors.toList()));
                        Objects.requireNonNull(enumAdapter).notifyItemInserted(quantityTypes.size() - 1);
                        recyclerView.smoothScrollToPosition(quantityTypes.size() - 1);
                    }

                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());

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
        ProviderDto provider = itemWithDetailAndProviderAndQuantityTypeDto.getProvider();
        if(provider != null){
            providerNameTextView.setText(provider.getName());
        }

        ItemDto item = itemWithDetailAndProviderAndQuantityTypeDto.getItem();
        if(item != null){
            TextView itemNameTextView = view.findViewById(R.id.item_name);
            itemNameTextView.setText(item.getName());

            //TextView itemBarcodeTextView = view.findViewById(R.id.item_barcode);
        }

        ItemDetailDto itemDetail = itemWithDetailAndProviderAndQuantityTypeDto.getItemDetail();
        if (itemDetail != null) {
            EditText portionsRequiredOnSaturdayEditText = view.findViewById(R.id.portions_required_on_saturday);
            Integer portionsRequiredOnSaturday = itemDetail.getPortionsRequiredOnSaturday();
            portionsRequiredOnSaturdayEditText.setText(String.valueOf(portionsRequiredOnSaturday));

            EditText portionsRequiredOnSundayEditText = view.findViewById(R.id.portions_required_on_sunday);
            Integer portionsRequiredOnSunday = itemDetail.getPortionsRequiredOnSunday();
            portionsRequiredOnSundayEditText.setText(String.valueOf(portionsRequiredOnSunday));

            TextView portionsPerWeekendTextView = view.findViewById(R.id.portions_per_weekend);
            Integer portionsPerWeekend = portionsRequiredOnSaturday + portionsRequiredOnSunday;
            portionsPerWeekendTextView.setText(String.valueOf(portionsPerWeekend));
            itemDetail.setPortionsPerWeekend(portionsPerWeekend);

            EditText portionsOnHoliday = view.findViewById(R.id.portions_on_holiday);
            portionsOnHoliday.setText(String.valueOf(itemDetail.getPortionsOnHoliday()));

            EditText maxPortionsSold = view.findViewById(R.id.max_portions_sold);
            maxPortionsSold.setText(String.valueOf(itemDetail.getMaxPortionsSold()));

        }

        List<QuantityTypeDto> quantityTypeAvailable = itemWithDetailAndProviderAndQuantityTypeDto.getQuantityType().stream()
                .filter(quantityType -> QuantityPurpose.AVAILABLE.equals(quantityType.getPurpose()))
                .collect(Collectors.toList());

        if (!quantityTypeAvailable.isEmpty()) {
            Integer totPortions = calculateTotPortions(quantityTypeAvailable, QuantityPurpose.AVAILABLE);
            TextView totPortionsTextView = view.findViewById(R.id.tot_portions_detail);
            totPortionsTextView.setText(String.valueOf(totPortions));
        }


    }

    protected Integer calculateTotPortions(List<QuantityTypeDto> quantityTypeDtos, QuantityPurpose purpose) {
        AtomicReference<Integer> totPortions = new AtomicReference<>(0);
        quantityTypeDtos.stream()
                .filter(quantityTypeDto -> purpose.equals(quantityTypeDto.getPurpose()))
                .forEach(quantityTypeDto -> totPortions.updateAndGet(v -> v + QuantityType.getTotPortions(quantityTypeDto.getQuantityType(), quantityTypeDto.getQuantity())));
        return totPortions.get();
    }

    @Override
    public UUID onItemLongClick(QuantityTypeDto quantityTypeDto) throws ExecutionException, InterruptedException{

        if(quantityTypeDto == null) {
            return null;
        }

        Future<?> future = executorService.submit(() -> {
            QuantityTypeEntityDao quantityTypeEntityDao = appDatabase.quantityTypeEntityDao();
            QuantityTypeEntity quantityTypeEntity = EntityDtoMapper.dtoToEntity(quantityTypeDto);
            quantityTypeEntityDao.delete(quantityTypeEntity);

        });
        future.get();

        Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show();
        return quantityTypeDto.getId();

    }
}