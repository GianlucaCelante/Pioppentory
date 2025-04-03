package it.pioppi.business.fragment;

import static it.pioppi.business.manager.ItemUtilityManager.calculateTotPortions;
import static it.pioppi.business.manager.ItemUtilityManager.normalizeText;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import it.pioppi.business.manager.ItemUtilityManager;
import it.pioppi.database.model.QuantityTypeEnum;
import it.pioppi.utils.ConstantUtils;
import it.pioppi.R;
import it.pioppi.business.adapter.QuantityTypeAdapter;
import it.pioppi.business.dto.item.detail.ItemDetailDto;
import it.pioppi.business.dto.item.ItemDto;
import it.pioppi.business.dto.item.ItemWithDetailAndQuantityTypeDto;
import it.pioppi.business.dto.provider.ProviderDto;
import it.pioppi.business.dto.item.quantity.QuantityTypeDto;
import it.pioppi.business.viewmodel.GeneralItemViewModel;
import it.pioppi.database.AppDatabase;
import it.pioppi.database.dao.ItemDetailEntityDao;
import it.pioppi.database.dao.ItemEntityDao;
import it.pioppi.database.dao.ProviderEntityDao;
import it.pioppi.database.dao.QuantityTypeEntityDao;
import it.pioppi.database.entity.ItemWithDetailAndQuantityTypeEntity;
import it.pioppi.database.mapper.EntityDtoMapper;
import it.pioppi.database.model.QuantityPurpose;
import it.pioppi.database.entity.ItemDetailEntity;
import it.pioppi.database.entity.ItemEntity;
import it.pioppi.database.model.ItemStatus;
import it.pioppi.database.entity.ProviderEntity;
import it.pioppi.database.entity.QuantityTypeEntity;
import it.pioppi.database.repository.ItemEntityRepository;
import it.pioppi.utils.LoggerManager;

public class ItemDetailFragment extends Fragment implements QuantityTypeAdapter.OnItemLongClickListener, QuantityTypeAdapter.OnTextChangeListener  {

    private AppDatabase appDatabase;
    private ExecutorService executorService;
    private RecyclerView quantityTypesAvailable;
    private RecyclerView quantityTypesToBeOrdered;
    private ItemEntityRepository itemEntityRepository;
    private GeneralItemViewModel generalItemViewModel;

    private UUID itemId;
    private long selectedDateMillis;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appDatabase = AppDatabase.getInstance(getContext());
        executorService = Executors.newSingleThreadExecutor();
        generalItemViewModel = new ViewModelProvider(requireActivity()).get(GeneralItemViewModel.class);
        itemEntityRepository = new ItemEntityRepository(requireActivity().getApplication());
        setHasOptionsMenu(true);
        LoggerManager.getInstance().log("onCreate: Inizializzazione di ItemDetailFragment", "INFO");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        LoggerManager.getInstance().log("onCreateView: Inizio creazione della view", "INFO");
        View view = inflater.inflate(R.layout.fragment_item_detail, container, false);

        if (savedInstanceState != null) {
            EditText itemNameTextView = view.findViewById(R.id.item_name_card_detail);
            EditText noteEditText = view.findViewById(R.id.note);
            EditText barcodeEditText = view.findViewById(R.id.barcode);
            EditText portionsRequiredOnSaturdayEditText = view.findViewById(R.id.portions_required_on_saturday);
            EditText portionsRequiredOnSundayEditText = view.findViewById(R.id.portions_required_on_sunday);
            EditText portionsOnHolidayEditText = view.findViewById(R.id.portions_on_holiday);
            EditText maxPortionsSoldEditText = view.findViewById(R.id.max_portions_sold);

            itemNameTextView.setText(savedInstanceState.getString("item_name", ""));
            noteEditText.setText(savedInstanceState.getString("note", ""));
            barcodeEditText.setText(savedInstanceState.getString("barcode", ""));
            portionsRequiredOnSaturdayEditText.setText(savedInstanceState.getString("portions_required_on_saturday", ""));
            portionsRequiredOnSundayEditText.setText(savedInstanceState.getString("portions_required_on_sunday", ""));
            portionsOnHolidayEditText.setText(savedInstanceState.getString("portions_on_holiday", ""));
            maxPortionsSoldEditText.setText(savedInstanceState.getString("max_portions_sold", ""));

            // Ripristina la data selezionata, se necessario
            selectedDateMillis = savedInstanceState.getLong("selectedDateMillis", System.currentTimeMillis());
            CalendarView deliveryDateCalendarView = view.findViewById(R.id.delivery_date);
            deliveryDateCalendarView.setDate(selectedDateMillis);
        }

        Bundle bundle = getArguments();
        if (bundle != null) {
            try {
                if(bundle.getString(ConstantUtils.ITEM_ID) != null) {
                    itemId = UUID.fromString(bundle.getString(ConstantUtils.ITEM_ID));
                } else if(bundle.getString(ConstantUtils.SCANNED_CODE) != null) {
                    itemId = fetchItemWithDetailByBarcode(bundle.getString(ConstantUtils.SCANNED_CODE));
                } else {
                    throw new IllegalArgumentException("Item details not found");
                }
                LoggerManager.getInstance().log("onCreateView: ItemId ottenuto: " + itemId, "DEBUG");

                ItemWithDetailAndQuantityTypeDto dto = fetchItemWithDetailAndQuantityTypeById(itemId);
                LoggerManager.getInstance().log("onCreateView: DTO ottenuto per l'item", "DEBUG");

                generalItemViewModel.updateItem(dto.getItem());
                generalItemViewModel.updateItemDetail(dto.getItemDetail());
                generalItemViewModel.updateQuantityTypes(dto.getQuantityTypes());


            } catch (ExecutionException | InterruptedException e) {
                LoggerManager.getInstance().logException(e);
                throw new RuntimeException(e);
            } catch (IllegalArgumentException ex) {
                Toast.makeText(requireContext(), "Errore nel caricamento dell'item, Item non esiste", Toast.LENGTH_SHORT).show();
                LoggerManager.getInstance().log("onCreateView: Errore nel caricamento dell'item, Item non esiste", "ERROR");
                return view;
            }
        } else {
            Toast.makeText(requireContext(), "Errore nel caricamento dell'item, Item non esiste", Toast.LENGTH_SHORT).show();
            LoggerManager.getInstance().log("onCreateView: Errore nel caricamento dell'item, Bundle null", "ERROR");
            return view;
        }

        // Set up touch listener to hide keyboard when touch happens outside EditText
        setupUI(view);

        // PROVIDER
        List<String> providers;
        try {
            providers = executorService.submit(() -> appDatabase.providerEntityDao().getProviderNames()).get()
                    .stream().sorted().collect(Collectors.toList());
            LoggerManager.getInstance().log("onCreateView: Provider recuperati", "DEBUG");
        } catch (ExecutionException | InterruptedException e) {
            LoggerManager.getInstance().logException(e);
            throw new RuntimeException(e);
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, providers);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner providerSpinner = view.findViewById(R.id.provider_spinner);
        providerSpinner.setAdapter(spinnerAdapter);
        Button addProviderButton = view.findViewById(R.id.add_provider_button);
        addProviderButton.setOnClickListener(v -> {
            LoggerManager.getInstance().log("onCreateView: Clic sul pulsante per aggiungere fornitore", "DEBUG");
            addProviderItem(inflater, spinnerAdapter, providerSpinner);
        });

        quantityTypesAvailable = setupRecyclerViewAndButtonForQuantityTypes(view, inflater, R.id.recycler_view_quantity_available, R.id.add_quantity_type_available, QuantityPurpose.AVAILABLE);
        quantityTypesToBeOrdered = setupRecyclerViewAndButtonForQuantityTypes(view, inflater, R.id.recycler_view_quantity_to_be_ordered, R.id.add_quantity_type_to_be_ordered, QuantityPurpose.TO_BE_ORDERED);

        prefillFields(view);

        CalendarView deliveryDateCalendarView = view.findViewById(R.id.delivery_date);
        selectedDateMillis = deliveryDateCalendarView.getDate();

        deliveryDateCalendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            selectedDateMillis = calendar.getTimeInMillis();
            LoggerManager.getInstance().log("onCreateView: Data di consegna aggiornata", "DEBUG");
        });

        updatePortionsNeededForWeekendWhenPortionsRequiredOnSaturdayAndOnSundayChanged(view);

        FloatingActionButton saveButton = view.findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> {
            LoggerManager.getInstance().log("onCreateView: Clic sul pulsante di salvataggio", "INFO");
            try {
                saveAll(view);
            } catch (ExecutionException | InterruptedException e) {
                LoggerManager.getInstance().logException(e);
                throw new RuntimeException(e);
            }
        });

        Button itemTagsButton = view.findViewById(R.id.item_tags_button);
        itemTagsButton.setOnClickListener(v -> {
            LoggerManager.getInstance().log("onCreateView: Clic sul pulsante per item tags", "DEBUG");
            Bundle bundleItemTags = new Bundle();
            bundleItemTags.putString("itemId", itemId.toString());
            NavHostFragment.findNavController(this).navigate(R.id.action_itemDetailFragment_to_itemTagsFragment, bundleItemTags);
        });

        EditText itemNameTextView = view.findViewById(R.id.item_name_card_detail);
        String currentItemName = itemNameTextView.getText().toString();
        itemNameTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Niente da loggare
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Niente da loggare
            }

            @Override
            public void afterTextChanged(Editable s) {
                String newItemName = s.toString().trim();
                LoggerManager.getInstance().log("onCreateView: afterTextChanged: Nuovo nome item: " + newItemName, "DEBUG");
                try {
                    checkIfItemNameExists(newItemName, currentItemName, saveButton);
                } catch (ExecutionException | InterruptedException e) {
                    LoggerManager.getInstance().logException(e);
                    throw new RuntimeException(e);
                }
            }
        });


        ShapeableImageView imagePreview = view.findViewById(R.id.item_image_preview);
        String imageUrl = Objects.requireNonNull(generalItemViewModel.getItems().getValue()).stream()
                .filter(item -> item.getId().equals(itemId))
                .findAny().map(ItemDto::getImageUrl).orElse(null);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(requireContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.camera)
                    .into(imagePreview);
            LoggerManager.getInstance().log("onCreateView: Immagine caricata", "DEBUG");
        }

        imagePreview.setOnClickListener(v -> {
            LoggerManager.getInstance().log("onCreateView: Clic sull'immagine per visualizzazione full screen", "DEBUG");
            NavController navController = NavHostFragment.findNavController(this);
            Bundle bundleImageFragment = new Bundle();
            bundleImageFragment.putString("itemId", itemId.toString());
            bundleImageFragment.putString("imageUrl", imageUrl);
            navController.navigate(R.id.action_itemDetailFragment_to_fullScreenImageDialogFragment, bundleImageFragment);
        });


        LoggerManager.getInstance().log("onCreateView: Vista creata con successo", "INFO");
        return view;
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem != null) {
            searchItem.setVisible(false); // Nascondi l'elemento di ricerca
            LoggerManager.getInstance().log("onPrepareOptionsMenu: Elemento di ricerca nascosto", "DEBUG");
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Salva i valori degli EditText (o altri widget) che l'utente può aver modificato
        EditText itemNameTextView = requireView().findViewById(R.id.item_name_card_detail);
        EditText noteEditText = requireView().findViewById(R.id.note);
        EditText barcodeEditText = requireView().findViewById(R.id.barcode);
        EditText portionsRequiredOnSaturdayEditText = requireView().findViewById(R.id.portions_required_on_saturday);
        EditText portionsRequiredOnSundayEditText = requireView().findViewById(R.id.portions_required_on_sunday);
        EditText portionsOnHolidayEditText = requireView().findViewById(R.id.portions_on_holiday);
        EditText maxPortionsSoldEditText = requireView().findViewById(R.id.max_portions_sold);

        outState.putString("item_name", itemNameTextView.getText().toString());
        outState.putString("note", noteEditText.getText().toString());
        outState.putString("barcode", barcodeEditText.getText().toString());
        outState.putString("portions_required_on_saturday", portionsRequiredOnSaturdayEditText.getText().toString());
        outState.putString("portions_required_on_sunday", portionsRequiredOnSundayEditText.getText().toString());
        outState.putString("portions_on_holiday", portionsOnHolidayEditText.getText().toString());
        outState.putString("max_portions_sold", maxPortionsSoldEditText.getText().toString());

        // Se necessario, salva anche altri dati (ad esempio, la data selezionata)
        outState.putLong("selectedDateMillis", selectedDateMillis);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LoggerManager.getInstance().log("onViewCreated: Vista completata", "INFO");

        onTextChanged();

        generalItemViewModel.getQuantityTypes().observe(getViewLifecycleOwner(), quantityTypes -> {

            List<QuantityTypeDto> quantityTypeForItem = quantityTypes.stream()
                    .filter(q -> q.getItemId().equals(itemId))
                    .collect(Collectors.toList());

            Long totPortions = calculateTotPortions(quantityTypeForItem, QuantityPurpose.AVAILABLE);

            TextView totPortionsTextView = requireView().findViewById(R.id.tot_portions_avalaible);
            totPortionsTextView.setText(String.valueOf(totPortions));

            int weekendRequirement = 0;

            ItemDetailDto itemDetail = Objects.requireNonNull(generalItemViewModel.getItemDetails().getValue()).stream()
                    .filter(detail -> detail.getItemId().equals(itemId))
                    .findFirst().orElse(null);
            if(itemDetail != null && itemDetail.getPortionsPerWeekend() != null) {
                weekendRequirement = itemDetail.getPortionsPerWeekend();
            }

            ItemDto item = Objects.requireNonNull(generalItemViewModel.getItems().getValue()).stream()
                    .filter(i -> i.getId().equals(itemId))
                    .findFirst().orElse(null);

            CardView statusCard = requireView().findViewById(R.id.item_name_tot_portions);
            ItemUtilityManager.updateItemStatus(requireContext(), Objects.requireNonNull(item), statusCard, totPortions, weekendRequirement);
        });
    }

    private void saveAll(View view) throws ExecutionException, InterruptedException {
        LoggerManager.getInstance().log("saveAll: Inizio salvataggio dati", "INFO");

        QuantityTypeEntityDao quantityTypeEntityDao = appDatabase.quantityTypeEntityDao();
        ItemDetailEntityDao itemDetailEntityDao = appDatabase.itemDetailEntityDao();

        ItemDto item = Objects.requireNonNull(generalItemViewModel.getItems().getValue()).stream()
                .filter(itemDto -> itemDto.getId().equals(itemId))
                .findFirst().orElse(null);

        ItemDetailDto itemDetail = Objects.requireNonNull(generalItemViewModel.getItemDetails().getValue()).stream()
                .filter(detail -> detail.getItemId().equals(itemId))
                .findFirst().orElse(null);

        List<QuantityTypeDto> quantityTypes = Objects.requireNonNull(generalItemViewModel.getQuantityTypes().getValue()).stream()
                .filter(quantityTypeDto -> quantityTypeDto.getItemId().equals(itemId))
                .collect(Collectors.toList());

        TextView portionsPerWeekendTextView = view.findViewById(R.id.portions_per_weekend);
        TextView portionsRequiredOnSaturdayTextView = view.findViewById(R.id.portions_required_on_saturday);
        TextView portionsRequiredOnSundayTextView = view.findViewById(R.id.portions_required_on_sunday);
        TextView portionsOnHolidayTextView = view.findViewById(R.id.portions_on_holiday);
        TextView maxPortionsSoldTextView = view.findViewById(R.id.max_portions_sold);
        EditText notesEditText = view.findViewById(R.id.note);
        EditText itemNameTextView = view.findViewById(R.id.item_name_card_detail);
        Spinner providerSpinner = view.findViewById(R.id.provider_spinner);
        EditText barcodeEditText = view.findViewById(R.id.barcode);

        String providerName;
        if(providerSpinner.getSelectedItem() != null) {
            providerName = providerSpinner.getSelectedItem().toString();
        } else {
            providerName = "";
        }

        if(!providerName.isEmpty()) {
            executorService.submit(() -> {
                ProviderEntityDao providerEntityDao = appDatabase.providerEntityDao();
                ProviderDto provider = EntityDtoMapper.entityToDto(providerEntityDao.getProviderByName(providerName));
                if (item != null) {
                    item.setProviderId(provider.getId());
                }
            }).get();
            LoggerManager.getInstance().log("saveAll: Provider associato: " + providerName, "DEBUG");
        }

        Integer portionsPerWeekend = parseIntegerValueToTextView(portionsPerWeekendTextView);
        Integer portionsRequiredOnSaturday = parseIntegerValueToTextView(portionsRequiredOnSaturdayTextView);
        Integer portionsRequiredOnSunday = parseIntegerValueToTextView(portionsRequiredOnSundayTextView);
        Integer portionsOnHoliday = parseIntegerValueToTextView(portionsOnHolidayTextView);
        Integer maxPortionsSold = parseIntegerValueToTextView(maxPortionsSoldTextView);
        ZonedDateTime deliveryDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(selectedDateMillis), ZoneId.of(ConstantUtils.ZONE_ID));

        Long totPortionsAvailable = calculateTotPortions(quantityTypes, QuantityPurpose.AVAILABLE);
        Long totPortionsToBeOrdered = calculateTotPortions(quantityTypes, QuantityPurpose.TO_BE_ORDERED);

        String note = notesEditText.getText().toString();
        String itemName = itemNameTextView.getText().toString();
        String barcode = barcodeEditText.getText().toString();

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(ConstantUtils.ZONE_ID));

        ItemDto finalItem = new ItemDto();
        finalItem.setId(item != null ? item.getId() : null);
        finalItem.setName(itemName);
        finalItem.setFtsId(item != null ? item.getFtsId() : null);
        finalItem.setCreationDate(Objects.requireNonNull(item).getCreationDate());
        finalItem.setLastUpdateDate(now);
        finalItem.setCheckDate(now);
        finalItem.setStatus(item.getStatus());
        finalItem.setChecked(true);
        finalItem.setTotPortions(totPortionsAvailable);
        finalItem.setBarcode(barcode);
        finalItem.setNote(note);
        finalItem.setImageUrl(item.getImageUrl());
        finalItem.setProviderId(item.getProviderId());

        ItemDetailDto finalItemDetail = new ItemDetailDto();
        finalItemDetail.setId(itemDetail != null ? itemDetail.getId() : null);
        finalItemDetail.setItemId(itemDetail != null ? itemDetail.getItemId() : null);
        finalItemDetail.setPortionsPerWeekend(portionsPerWeekend);
        finalItemDetail.setPortionsRequiredOnSaturday(portionsRequiredOnSaturday);
        finalItemDetail.setPortionsRequiredOnSunday(portionsRequiredOnSunday);
        finalItemDetail.setPortionsOnHoliday(portionsOnHoliday);
        finalItemDetail.setMaxPortionsSold(maxPortionsSold);
        finalItemDetail.setDeliveryDate(deliveryDate);
        finalItemDetail.setCreationDate(itemDetail != null ? itemDetail.getCreationDate() : null);
        finalItemDetail.setLastUpdateDate(now);
        finalItemDetail.setQuantityToBeOrdered(totPortionsToBeOrdered);
        finalItemDetail.setOrderedQuantity(itemDetail != null ? itemDetail.getOrderedQuantity() : null);
        finalItemDetail.setItemId(finalItem.getId());

        List<QuantityTypeDto> finalQuantityTypes = new ArrayList<>();
        for (QuantityTypeDto quantityTypeDto : quantityTypes) {
            QuantityTypeDto finalQuantityType = new QuantityTypeDto();
            finalQuantityType.setId(quantityTypeDto.getId());
            finalQuantityType.setItemId(quantityTypeDto.getItemId());
            finalQuantityType.setQuantityType(quantityTypeDto.getQuantityType());
            finalQuantityType.setDescription(quantityTypeDto.getDescription());
            finalQuantityType.setQuantity(quantityTypeDto.getQuantity());
            finalQuantityType.setPurpose(quantityTypeDto.getPurpose());
            finalQuantityType.setUnitsPerQuantityType(quantityTypeDto.getUnitsPerQuantityType());
            finalQuantityType.setCreationDate(quantityTypeDto.getCreationDate());
            finalQuantityType.setLastUpdateDate(now);
            finalQuantityTypes.add(finalQuantityType);
        }

        Future<?> future = executorService.submit(() -> {
            ItemEntity itemEntity = EntityDtoMapper.dtoToEntity(finalItem);
            itemEntityRepository.update(itemEntity);

            ItemDetailEntity itemDetailEntity = EntityDtoMapper.detailDtoToEntity(finalItemDetail);
            itemDetailEntityDao.upsert(itemDetailEntity);

            finalQuantityTypes.forEach(quantityTypeDto -> {
                QuantityTypeEntity quantityTypeEntity = EntityDtoMapper.dtoToEntity(quantityTypeDto);
                if(quantityTypeEntity.getId() != null) {
                    quantityTypeEntityDao.upsert(quantityTypeEntity);
                } else {
                    quantityTypeEntity.setId(UUID.randomUUID());
                    quantityTypeEntity.setItemId(itemEntity.getId());
                    quantityTypeEntityDao.upsert(quantityTypeEntity);
                }
            });
        });

        future.get();

        generalItemViewModel.updateItem(finalItem);
        List<ItemDto> updatedItems = new ArrayList<>(generalItemViewModel.getItems().getValue());
        generalItemViewModel.setItems(updatedItems);
        generalItemViewModel.updateItemDetail(finalItemDetail);
        generalItemViewModel.updateQuantityTypes(finalQuantityTypes);

        Toast.makeText(requireContext(), "Prodotto salvato", Toast.LENGTH_SHORT).show();
        LoggerManager.getInstance().log("Toast: Prodotto salvato", "INFO");
        LoggerManager.getInstance().log("saveAll: Salvataggio completato", "INFO");
        NavHostFragment.findNavController(this).popBackStack();
    }

    private void checkIfItemNameExists(String newItemName, String currentItemName, FloatingActionButton saveButton) throws ExecutionException, InterruptedException {
        LoggerManager.getInstance().log("checkIfItemNameExists: Controllo esistenza nome item", "DEBUG");
        Future<?> future = executorService.submit(() -> {
            List<String> existingItems = appDatabase.itemEntityDao().getUniqueItemNames();
            boolean itemNameExists = existingItems.stream()
                    .anyMatch(item -> item.equalsIgnoreCase(newItemName));

            requireActivity().runOnUiThread(() -> {
                if (itemNameExists && !newItemName.equalsIgnoreCase(currentItemName)) {
                    saveButton.setEnabled(false);
                    Toast.makeText(getContext(), "Esiste già un prodotto con questo nome", Toast.LENGTH_SHORT).show();
                    LoggerManager.getInstance().log("Toast: Esiste già un prodotto con questo nome", "WARN");
                } else {
                    saveButton.setEnabled(true);
                }
            });
        });

        future.get();
    }

    private void updatePortionsNeededForWeekendWhenPortionsRequiredOnSaturdayAndOnSundayChanged(View view) {
        LoggerManager.getInstance().log("updatePortionsNeeded: Impostazione dei listener per aggiornare le porzioni", "DEBUG");
        EditText portionsRequiredOnSaturday = view.findViewById(R.id.portions_required_on_saturday);
        EditText portionsRequiredOnSunday = view.findViewById(R.id.portions_required_on_sunday);
        TextView portionsPerWeekendTextView = view.findViewById(R.id.portions_per_weekend);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Normalizza il testo nei due campi, come nell'adapter
                String normalizedSaturday = ItemUtilityManager.normalizeText(portionsRequiredOnSaturday.getText().toString());
                String normalizedSunday = ItemUtilityManager.normalizeText(portionsRequiredOnSunday.getText().toString());

                if (!normalizedSaturday.equals(portionsRequiredOnSaturday.getText().toString())) {
                    int selStart = portionsRequiredOnSaturday.getSelectionStart();
                    int selEnd = portionsRequiredOnSaturday.getSelectionEnd();
                    portionsRequiredOnSaturday.removeTextChangedListener(this);
                    portionsRequiredOnSaturday.setText(normalizedSaturday);
                    portionsRequiredOnSaturday.setSelection(Math.min(selStart, normalizedSaturday.length()), Math.min(selEnd, normalizedSaturday.length()));
                    portionsRequiredOnSaturday.addTextChangedListener(this);
                }
                if (!normalizedSunday.equals(portionsRequiredOnSunday.getText().toString())) {
                    int selStart = portionsRequiredOnSunday.getSelectionStart();
                    int selEnd = portionsRequiredOnSunday.getSelectionEnd();
                    portionsRequiredOnSunday.removeTextChangedListener(this);
                    portionsRequiredOnSunday.setText(normalizedSunday);
                    portionsRequiredOnSunday.setSelection(Math.min(selStart, normalizedSunday.length()), Math.min(selEnd, normalizedSunday.length()));
                    portionsRequiredOnSunday.addTextChangedListener(this);
                }

                // Converte i valori (0 se il campo è vuoto)
                int saturdayPortions = normalizedSaturday.isEmpty() ? 0 : Integer.parseInt(normalizedSaturday);
                int sundayPortions = normalizedSunday.isEmpty() ? 0 : Integer.parseInt(normalizedSunday);

                int totalWeekend = saturdayPortions + sundayPortions;
                portionsPerWeekendTextView.setText(String.valueOf(totalWeekend));

                // Aggiorna l'oggetto itemDetail se disponibile
                ItemDetailDto itemDetail = Objects.requireNonNull(generalItemViewModel.getItemDetails().getValue())
                        .stream()
                        .filter(detail -> detail.getItemId().equals(itemId))
                        .findFirst().orElse(null);
                if (itemDetail != null) {
                    itemDetail.setPortionsRequiredOnSaturday(saturdayPortions);
                    itemDetail.setPortionsRequiredOnSunday(sundayPortions);
                    itemDetail.setPortionsPerWeekend(totalWeekend);
                }

                // Richiama il metodo onTextChanged del fragment per aggiornare altri elementi (es. background)
                ItemDetailFragment.this.onTextChanged();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        };

        // Aggiunge lo stesso listener ad entrambe le EditText
        portionsRequiredOnSaturday.addTextChangedListener(textWatcher);
        portionsRequiredOnSunday.addTextChangedListener(textWatcher);

        EditText portionsOnHoliday = view.findViewById(R.id.portions_on_holiday);
        EditText maxPortionsSold = view.findViewById(R.id.max_portions_sold);

        TextWatcher normalizeTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String normalizedTextPortionsOnHoliday = normalizeText(portionsOnHoliday.getText().toString());
                if (!normalizedTextPortionsOnHoliday.equals(portionsOnHoliday.getText().toString())) {
                    portionsOnHoliday.setText(normalizedTextPortionsOnHoliday);
                    portionsOnHoliday.setSelection(normalizedTextPortionsOnHoliday.length());
                }

                String normalizedMaxPortionsSold = normalizeText(maxPortionsSold.getText().toString());
                if (!normalizedMaxPortionsSold.equals(maxPortionsSold.getText().toString())) {
                    maxPortionsSold.setText(normalizedMaxPortionsSold);
                    maxPortionsSold.setSelection(normalizedMaxPortionsSold.length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        };

        portionsOnHoliday.addTextChangedListener(normalizeTextWatcher);
        maxPortionsSold.addTextChangedListener(normalizeTextWatcher);
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            Log.d("hideKeyboard: Tastiera nascosta", "DEBUG");
        }
    }

    private void openKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            Log.d("openKeyboard: Tastiera mostrata", "DEBUG");
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupUI(View view) {
        if (!(view instanceof EditText)) {
            view.setOnTouchListener((v, event) -> {
                ItemDetailFragment.this.hideKeyboard(view);
                view.clearFocus();
                return false;
            });
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    private RecyclerView setupRecyclerViewAndButtonForQuantityTypes(View view, LayoutInflater inflater, int recyclerViewId, int buttonId, QuantityPurpose purpose) {
        RecyclerView recyclerView = view.findViewById(recyclerViewId);

        QuantityTypeAdapter quantityTypeAdapter = new QuantityTypeAdapter(new ArrayList<>(), this, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(quantityTypeAdapter);

        generalItemViewModel.getQuantityTypes().observe(getViewLifecycleOwner(), quantityTypeDtos -> {
            List<QuantityTypeDto> filtered = quantityTypeDtos.stream()
                    .filter(q -> q.getPurpose() == purpose && q.getItemId().equals(itemId))
                    .collect(Collectors.toList());
            quantityTypeAdapter.setQuantityTypes(filtered);
        });

        Button addButton = view.findViewById(buttonId);
        addButton.setOnClickListener(v -> addQuantityType(inflater, purpose));
        return recyclerView;
    }

        private void addQuantityType(LayoutInflater inflater, QuantityPurpose purpose) {
        List<QuantityTypeDto> existingQuantityTypes = Objects.requireNonNull(generalItemViewModel.getQuantityTypes().getValue()).stream()
                .filter(q -> q.getPurpose() == purpose && q.getItemId().equals(itemId))
                .collect(Collectors.toList());

        List<QuantityTypeEnum> availableQuantityTypeEnums = getAvailableQuantityTypes(existingQuantityTypes);

        if (availableQuantityTypeEnums.isEmpty()) {
            Toast.makeText(requireContext(), "Tutti i tipi di quantità sono già presenti", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = inflater.inflate(R.layout.dialog_add_quantity_type, null);
        Spinner spinner = dialogView.findViewById(R.id.quantity_type_spinner);
        EditText quantityAvailable = dialogView.findViewById(R.id.quantity_type_available);
        EditText unitsPerQuantityType = dialogView.findViewById(R.id.units_per_quantity_type);

        ArrayAdapter<QuantityTypeEnum> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, availableQuantityTypeEnums);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(dialogView)
                .setPositiveButton("Aggiungi", null)
                .setNegativeButton("Indietro", (dialog, id) -> dialog.cancel());

        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(dlg -> {
            Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                QuantityTypeDto quantityTypeDto = new QuantityTypeDto();
                quantityTypeDto.setId(UUID.randomUUID());
                quantityTypeDto.setItemId(itemId);
                quantityTypeDto.setPurpose(purpose);
                quantityTypeDto.setCreationDate(ZonedDateTime.now());

                if (spinner.getSelectedItem() != null) {
                    quantityTypeDto.setQuantityType((QuantityTypeEnum) spinner.getSelectedItem());
                    quantityTypeDto.setDescription(((QuantityTypeEnum) spinner.getSelectedItem()).getDescription());
                }

                quantityTypeDto.setQuantity(quantityAvailable.getText().toString().isEmpty() ? 0 : Integer.parseInt(quantityAvailable.getText().toString()));
                quantityTypeDto.setUnitsPerQuantityType(unitsPerQuantityType.getText().toString().isEmpty() ? 0 : Integer.parseInt(unitsPerQuantityType.getText().toString()));

                List<QuantityTypeDto> currentQuantityTypes = generalItemViewModel.getQuantityTypes().getValue();
                currentQuantityTypes.add(quantityTypeDto);
                generalItemViewModel.setQuantityTypes(currentQuantityTypes);

                alertDialog.dismiss();
            });
        });
        alertDialog.show();
    }

    private List<QuantityTypeEnum> getAvailableQuantityTypes(List<QuantityTypeDto> existingQuantityTypes) {
        LoggerManager.getInstance().log("getAvailableQuantityTypes: Calcolo tipi disponibili", "DEBUG");
        List<QuantityTypeEnum> allQuantityTypeEnums = Arrays.asList(QuantityTypeEnum.values());
        List<QuantityTypeEnum> usedQuantityTypeEnums = existingQuantityTypes.stream()
                .map(QuantityTypeDto::getQuantityType)
                .collect(Collectors.toList());
        return allQuantityTypeEnums.stream()
                .filter(quantityType -> !usedQuantityTypeEnums.contains(quantityType))
                .collect(Collectors.toList());
    }

    private ItemWithDetailAndQuantityTypeDto fetchItemWithDetailAndQuantityTypeById(UUID itemId) throws ExecutionException, InterruptedException {
        LoggerManager.getInstance().log("fetchItemWithDetailAndQuantityTypeById: Recupero dati per itemId " + itemId, "DEBUG");
        Future<ItemWithDetailAndQuantityTypeDto> future = executorService.submit(() -> {
            ItemEntityDao entityDao = appDatabase.itemEntityDao();
            ItemWithDetailAndQuantityTypeEntity entity = entityDao.getItemsWithDetailsAndQuantityType(itemId);

            if (entity == null || entity.item == null) {
                throw new IllegalArgumentException("Item not found");
            }

            ItemWithDetailAndQuantityTypeDto dto = new ItemWithDetailAndQuantityTypeDto();
            dto.setItem(EntityDtoMapper.entityToDto(entity.item));

            if (entity.itemDetail != null) {
                dto.setItemDetail(EntityDtoMapper.detailEntityToDto(entity.itemDetail));
            } else {
                dto.setItemDetail(new ItemDetailDto());
            }

            if (entity.quantityTypes != null) {
                dto.setQuantityTypes(EntityDtoMapper.entitiesToDtosForQuantityTypes(entity.quantityTypes));
            } else {
                dto.setQuantityTypes(new ArrayList<>());
            }

            return dto;
        });

        return future.get();
    }

    private UUID fetchItemWithDetailByBarcode(String barcode) throws ExecutionException, InterruptedException {
        LoggerManager.getInstance().log("fetchItemWithDetailByBarcode: Recupero item per barcode " + barcode, "DEBUG");
        Future<UUID> future = executorService.submit(() -> {
            ItemEntityDao itemEntityDao = appDatabase.itemEntityDao();
            UUID itemId = UUID.fromString(itemEntityDao.getItemByBarcode(barcode));
            if (itemId != null) {
                return itemId;
            } else {
                throw new IllegalArgumentException();
            }
        });

        return future.get();
    }

    protected void prefillFields(View view) {
        LoggerManager.getInstance().log("prefillFields: Pre-compilazione dei campi", "DEBUG");
        Spinner providerSpinner = view.findViewById(R.id.provider_spinner);

        ItemDto itemDto = Objects.requireNonNull(generalItemViewModel.getItems().getValue()).stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst().orElse(null);

        UUID providerId = itemDto != null ? itemDto.getProviderId() : null;

        executorService.submit(() -> {
            ProviderEntityDao providerEntityDao = appDatabase.providerEntityDao();
            ProviderEntity providerEntity = providerEntityDao.getProviderById(providerId);
            if (providerEntity != null) {
                ProviderDto provider = EntityDtoMapper.entityToDto(providerEntity);
                requireActivity().runOnUiThread(() -> {
                    ArrayAdapter<String> spinnerAdapter = (ArrayAdapter<String>) providerSpinner.getAdapter();
                    int spinnerPosition = spinnerAdapter.getPosition(provider.getName());
                    if (spinnerPosition >= 0) {
                        providerSpinner.setSelection(spinnerPosition);
                    }
                    LoggerManager.getInstance().log("prefillFields: Provider selezionato: " + provider.getName(), "DEBUG");
                });
            }
        });

        if (itemDto != null) {
            EditText itemNameTextView = view.findViewById(R.id.item_name_card_detail);
            TextView totPortionsAvailableTextView = view.findViewById(R.id.tot_portions_avalaible);
            EditText barcodeEditText = view.findViewById(R.id.barcode);
            TextView noteTextView = view.findViewById(R.id.note);

            itemNameTextView.setText(itemDto.getName());
            totPortionsAvailableTextView.setText(String.valueOf(itemDto.getTotPortions()));
            barcodeEditText.setText(itemDto.getBarcode());
            noteTextView.setText(itemDto.getNote());
            LoggerManager.getInstance().log("prefillFields: Campi base precompilati", "DEBUG");
        }

        ItemDetailDto itemDetail = Objects.requireNonNull(generalItemViewModel.getItemDetails().getValue()).stream()
                .filter(detail -> detail.getItemId().equals(itemId))
                .findFirst().orElse(null);

        if (itemDetail != null) {
            TextView totPortionsToBeOrderedTextView = view.findViewById(R.id.tot_portions_to_be_ordered);
            totPortionsToBeOrderedTextView.setText(String.valueOf(itemDetail.getQuantityToBeOrdered() != null ? itemDetail.getQuantityToBeOrdered() : 0));

            EditText portionsRequiredOnSaturdayEditText = view.findViewById(R.id.portions_required_on_saturday);
            Integer portionsRequiredOnSaturday = itemDetail.getPortionsRequiredOnSaturday() != null ? itemDetail.getPortionsRequiredOnSaturday() : 0;
            portionsRequiredOnSaturdayEditText.setText(String.valueOf(portionsRequiredOnSaturday));

            EditText portionsRequiredOnSundayEditText = view.findViewById(R.id.portions_required_on_sunday);
            Integer portionsRequiredOnSunday = itemDetail.getPortionsRequiredOnSunday() != null ? itemDetail.getPortionsRequiredOnSunday() : 0;
            portionsRequiredOnSundayEditText.setText(String.valueOf(portionsRequiredOnSunday));

            TextView portionsPerWeekendTextView = view.findViewById(R.id.portions_per_weekend);
            Integer portionsPerWeekend = portionsRequiredOnSaturday + portionsRequiredOnSunday;
            portionsPerWeekendTextView.setText(String.valueOf(portionsPerWeekend));
            itemDetail.setPortionsPerWeekend(portionsPerWeekend);

            EditText portionsOnHoliday = view.findViewById(R.id.portions_on_holiday);
            portionsOnHoliday.setText(String.valueOf(itemDetail.getPortionsOnHoliday() != null ? itemDetail.getPortionsOnHoliday() : 0));

            EditText maxPortionsSold = view.findViewById(R.id.max_portions_sold);
            maxPortionsSold.setText(String.valueOf(itemDetail.getMaxPortionsSold() != null ? itemDetail.getMaxPortionsSold() : 0));

            CalendarView deliveryDateCalendarView = view.findViewById(R.id.delivery_date);
            ZonedDateTime deliveryDate = itemDetail.getDeliveryDate();
            if (deliveryDate != null) {
                long dateMillis = deliveryDate.toInstant().toEpochMilli();
                deliveryDateCalendarView.setDate(dateMillis);
                selectedDateMillis = dateMillis;
            }
            LoggerManager.getInstance().log("prefillFields: Campi dettagli precompilati", "DEBUG");
        }

        List<QuantityTypeDto> quantityTypes = Objects.requireNonNull(generalItemViewModel.getQuantityTypes().getValue()).stream()
                .filter(q -> q.getItemId().equals(itemId))
                .collect(Collectors.toList());

        List<QuantityTypeDto> quantityTypeAvailable = quantityTypes.stream()
                .filter(quantityType -> QuantityPurpose.AVAILABLE.equals(quantityType.getPurpose()))
                .collect(Collectors.toList());

        List<QuantityTypeDto> quantityTypeToBeOrdered = quantityTypes.stream()
                .filter(quantityType -> QuantityPurpose.TO_BE_ORDERED.equals(quantityType.getPurpose()))
                .collect(Collectors.toList());

        QuantityTypeAdapter adapterAvailable = (QuantityTypeAdapter) quantityTypesAvailable.getAdapter();
        if(adapterAvailable != null) {
            adapterAvailable.setQuantityTypes(quantityTypeAvailable);
        }

        QuantityTypeAdapter adapterToBeOrdered = (QuantityTypeAdapter) quantityTypesToBeOrdered.getAdapter();
        if(adapterToBeOrdered != null) {
            adapterToBeOrdered.setQuantityTypes(quantityTypeToBeOrdered);
        }
        LoggerManager.getInstance().log("prefillFields: Quantity types aggiornati", "DEBUG");
    }

    @Override
    public UUID onItemLongClick(QuantityTypeDto quantityTypeDto) throws ExecutionException, InterruptedException {
        LoggerManager.getInstance().log("onItemLongClick: Long click su QuantityTypeEnum " + (quantityTypeDto != null ? quantityTypeDto.getId() : "null"), "DEBUG");
        if (quantityTypeDto == null) {
            return null;
        }

        Future<?> future = executorService.submit(() -> {
            QuantityTypeEntityDao quantityTypeEntityDao = appDatabase.quantityTypeEntityDao();
            QuantityTypeEntity quantityTypeEntity = EntityDtoMapper.dtoToEntity(quantityTypeDto);
            quantityTypeEntityDao.delete(quantityTypeEntity);
        });
        future.get();

        Objects.requireNonNull(generalItemViewModel.getQuantityTypes().getValue())
                .removeIf(q -> q.getId().equals(quantityTypeDto.getId()));

        LoggerManager.getInstance().log("Toast: QuantityTypeEnum eliminato", "INFO");
        return quantityTypeDto.getId();
    }

    @Override
    public void onTextChanged() {
        LoggerManager.getInstance().log("onTextChanged: Aggiornamento porzioni totali", "DEBUG");

        if (generalItemViewModel.getItems().getValue() == null ||
                generalItemViewModel.getQuantityTypes().getValue() == null) {
            LoggerManager.getInstance().log("onTextChanged: items o quantityTypes ancora null, uscita anticipata", "WARN");
            return;
        }

        ItemDto itemDto = Objects.requireNonNull(generalItemViewModel.getItems().getValue()).stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElse(null);

        // Ottieni la lista dei QuantityTypeEnum che è già aggiornata (gli oggetti sono gli stessi modificati dai TextWatcher)
        List<QuantityTypeDto> quantityTypeDtos = generalItemViewModel.getQuantityTypes().getValue().stream()
                .filter(q -> q.getItemId().equals(itemId))
                .collect(Collectors.toList());

        // Calcola i totali in base allo stato attuale della lista
        Long totPortionsAvailable = ItemUtilityManager.calculateTotPortions(quantityTypeDtos, QuantityPurpose.AVAILABLE);
        Long totPortionsToBeOrdered = ItemUtilityManager.calculateTotPortions(quantityTypeDtos, QuantityPurpose.TO_BE_ORDERED);
        long totPortionsAvailablePlusOrdered = totPortionsAvailable + totPortionsToBeOrdered;

        TextView totPortionsAvailableTextView = requireView().findViewById(R.id.tot_portions_avalaible);
        TextView totPortionsToBeOrderedTextView = requireView().findViewById(R.id.tot_portions_to_be_ordered);
        TextView totPortionsAvailablePlusOrderedTextView = requireView().findViewById(R.id.tot_portions_avalaible_plus_ordered);
        TextView portionsPerWeekendTextView = requireView().findViewById(R.id.portions_per_weekend);
        totPortionsAvailableTextView.setText(String.valueOf(totPortionsAvailable));
        totPortionsToBeOrderedTextView.setText(String.valueOf(totPortionsToBeOrdered));
        totPortionsAvailablePlusOrderedTextView.setText(String.valueOf(totPortionsAvailablePlusOrdered));

        int portionsPerWeekend = Integer.parseInt(portionsPerWeekendTextView.getText().toString().isEmpty() ? "0" : portionsPerWeekendTextView.getText().toString());
        if (totPortionsAvailablePlusOrdered >= portionsPerWeekend) {
            Objects.requireNonNull(itemDto).setStatus(ItemStatus.GREEN);
            CardView itemNameAndTotPortionsCardView = requireView().findViewById(R.id.item_name_tot_portions);
            itemNameAndTotPortionsCardView.setCardBackgroundColor(ContextCompat.getColor(requireView().getContext(), R.color.green));
        } else {
            Objects.requireNonNull(itemDto).setStatus(ItemStatus.RED);
            CardView itemNameAndTotPortionsCardView = requireView().findViewById(R.id.item_name_tot_portions);
            itemNameAndTotPortionsCardView.setCardBackgroundColor(ContextCompat.getColor(requireView().getContext(), R.color.red));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LoggerManager.getInstance().log("onDestroy: Chiusura di ItemDetailFragment", "INFO");
        executorService.shutdown();
    }

    private Integer parseIntegerValueToTextView(TextView portionsPerWeekendTextView) {
        LoggerManager.getInstance().log("parseIntegerValueToTextView: Parsing valore", "DEBUG");
        if (portionsPerWeekendTextView.getText() == null || portionsPerWeekendTextView.getText().toString().isEmpty()) {
            return 0;
        } else {
            return Integer.parseInt(portionsPerWeekendTextView.getText().toString());
        }
    }

    private void addProviderItem(LayoutInflater inflater, ArrayAdapter<String> spinnerAdapter, Spinner providerSpinner) {
        LoggerManager.getInstance().log("addProviderItem: Apertura dialog per nuovo fornitore", "DEBUG");
        View dialogView = inflater.inflate(R.layout.new_provider_alert, null);

        EditText newProviderNameEditText = dialogView.findViewById(R.id.new_item_name);
        newProviderNameEditText.requestFocus();
        openKeyboard(newProviderNameEditText);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(dialogView)
                .setPositiveButton("Aggiungi", null)
                .setNegativeButton("Indietro", (dialog, id) -> dialog.cancel());

        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(dlg -> {
            Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String newProviderName = newProviderNameEditText.getText().toString().trim();

                if (newProviderName.isEmpty()) {
                    Toast.makeText(getContext(), "Inserisci un nome per il nuovo fornitore", Toast.LENGTH_SHORT).show();
                    LoggerManager.getInstance().log("Toast: Nome fornitore non inserito", "WARN");
                    return;
                }

                List<String> providerNames;
                try {
                    providerNames = executorService.submit(() -> appDatabase.providerEntityDao().getProviderNames()).get();
                } catch (ExecutionException | InterruptedException e) {
                    LoggerManager.getInstance().logException(e);
                    throw new RuntimeException(e);
                }

                boolean isUnique = providerNames.stream().noneMatch(provider -> provider.equalsIgnoreCase(newProviderName));

                if (!isUnique) {
                    Toast.makeText(getContext(), "Esiste già un fornitore con questo nome", Toast.LENGTH_SHORT).show();
                    LoggerManager.getInstance().log("Toast: Fornitore già esistente", "WARN");
                    return;
                }

                ZonedDateTime now = ZonedDateTime.now(ZoneId.of(ConstantUtils.ZONE_ID));

                ProviderEntity newProvider = new ProviderEntity();
                newProvider.setId(UUID.randomUUID());
                newProvider.setName(newProviderName);
                newProvider.setCreationDate(now);

                executorService.execute(() -> {
                    appDatabase.providerEntityDao().insert(newProvider);

                    requireActivity().runOnUiThread(() -> {
                        spinnerAdapter.add(newProviderName);
                        spinnerAdapter.notifyDataSetChanged();
                        providerSpinner.setSelection(spinnerAdapter.getPosition(newProviderName));
                        alertDialog.dismiss();
                        LoggerManager.getInstance().log("addProviderItem: Nuovo fornitore aggiunto: " + newProviderName, "INFO");
                    });
                });
            });
        });
        alertDialog.show();
    }

}
