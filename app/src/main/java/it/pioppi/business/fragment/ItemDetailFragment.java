package it.pioppi.business.fragment;

import static it.pioppi.business.manager.ItemDetailFragmentManager.calculateTotPortions;
import static it.pioppi.business.manager.ItemDetailFragmentManager.normalizeText;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.ImageView;
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
import com.google.android.material.button.MaterialButton;
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

import it.pioppi.utils.ConstantUtils;
import it.pioppi.R;
import it.pioppi.business.adapter.EnumAdapter;
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
import it.pioppi.database.model.QuantityType;
import it.pioppi.database.entity.ItemDetailEntity;
import it.pioppi.database.entity.ItemEntity;
import it.pioppi.database.model.ItemStatus;
import it.pioppi.database.entity.ProviderEntity;
import it.pioppi.database.entity.QuantityTypeEntity;
import it.pioppi.database.repository.ItemEntityRepository;
import it.pioppi.utils.LoggerManager;

public class ItemDetailFragment extends Fragment implements EnumAdapter.OnItemLongClickListener, EnumAdapter.OnTextChangeListener  {

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
                generalItemViewModel.setQuantityTypes(dto.getQuantityTypes());
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
        String imageUrl = generalItemViewModel.getItems().getValue().stream()
                .filter(item -> item.getId().equals(itemId)).findFirst().get().getImageUrl();

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LoggerManager.getInstance().log("onViewCreated: Vista completata", "INFO");
        onTextChanged();
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
        ZonedDateTime deliveryDate = ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(selectedDateMillis),
                ZoneId.of("Europe/Rome")
        );

        Long totPortionsAvailable = calculateTotPortions(quantityTypes, QuantityPurpose.AVAILABLE);
        Long totPortionsToBeOrdered = calculateTotPortions(quantityTypes, QuantityPurpose.TO_BE_ORDERED);

        String note = notesEditText.getText().toString();
        String itemName = itemNameTextView.getText().toString();
        String barcode = barcodeEditText.getText().toString();

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Rome"));

        ItemDto finalItem = new ItemDto();
        finalItem.setId(item != null ? item.getId() : null);
        finalItem.setName(itemName);
        finalItem.setFtsId(item != null ? item.getFtsId() : null);
        finalItem.setCreationDate(item.getCreationDate());
        finalItem.setLastUpdateDate(now);
        finalItem.setCheckDate(now);
        finalItem.setStatus(ItemStatus.BLUE);
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
            if(itemDetailEntity.getId() != null) {
                itemDetailEntityDao.upsert(itemDetailEntity);
            } else {
                itemDetailEntity.setId(UUID.randomUUID());
                itemDetailEntityDao.upsert(itemDetailEntity);
            }

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
        generalItemViewModel.updateItemDetail(finalItemDetail);
        generalItemViewModel.setQuantityTypes(finalQuantityTypes);

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
        EditText portionsNeededForSaturday = view.findViewById(R.id.portions_required_on_saturday);
        EditText portionsNeededForSunday = view.findViewById(R.id.portions_required_on_sunday);
        TextView portionsPerWeekendTextView = view.findViewById(R.id.portions_per_weekend);

        TextWatcher portionsTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String normalizedSaturdayText = normalizeText(portionsNeededForSaturday.getText().toString());
                String normalizedSundayText = normalizeText(portionsNeededForSunday.getText().toString());

                if (!normalizedSaturdayText.equals(portionsNeededForSaturday.getText().toString())) {
                    portionsNeededForSaturday.setText(normalizedSaturdayText);
                    portionsNeededForSaturday.setSelection(normalizedSaturdayText.length());
                }
                if (!normalizedSundayText.equals(portionsNeededForSunday.getText().toString())) {
                    portionsNeededForSunday.setText(normalizedSundayText);
                    portionsNeededForSunday.setSelection(normalizedSundayText.length());
                }

                int saturdayPortions = 0;
                int sundayPortions = 0;

                if (!portionsNeededForSaturday.getText().toString().isEmpty()) {
                    saturdayPortions = Integer.parseInt(portionsNeededForSaturday.getText().toString());
                }
                if (!portionsNeededForSunday.getText().toString().isEmpty()) {
                    sundayPortions = Integer.parseInt(portionsNeededForSunday.getText().toString());
                }

                Integer portionsPerWeekend = saturdayPortions + sundayPortions;

                ItemDetailDto itemDetail = Objects.requireNonNull(generalItemViewModel.getItemDetails().getValue()).stream()
                        .filter(detail -> detail.getItemId().equals(itemId))
                        .findFirst().orElse(null);

                if(itemDetail != null) {
                    itemDetail.setPortionsRequiredOnSaturday(saturdayPortions);
                    itemDetail.setPortionsRequiredOnSunday(sundayPortions);
                    itemDetail.setPortionsPerWeekend(portionsPerWeekend);
                    portionsPerWeekendTextView.setText(String.valueOf(portionsPerWeekend));
                    LoggerManager.getInstance().log("updatePortionsNeeded: Porzioni per weekend aggiornate a " + portionsPerWeekend, "DEBUG");
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        };

        portionsNeededForSaturday.addTextChangedListener(portionsTextWatcher);
        portionsNeededForSunday.addTextChangedListener(portionsTextWatcher);

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
            LoggerManager.getInstance().log("hideKeyboard: Tastiera nascosta", "DEBUG");
        }
    }

    private void openKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            LoggerManager.getInstance().log("openKeyboard: Tastiera mostrata", "DEBUG");
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
        LoggerManager.getInstance().log("setupUI: Impostazione listener per nascondere tastiera", "DEBUG");
    }

    private RecyclerView setupRecyclerViewAndButtonForQuantityTypes(View view, LayoutInflater inflater, int recyclerViewId, int buttonId, QuantityPurpose purpose) {
        LoggerManager.getInstance().log("setupRecyclerViewAndButtonForQuantityTypes: Setup per purpose " + purpose, "DEBUG");
        List<QuantityTypeDto> filteredQuantityTypes = Objects.requireNonNull(generalItemViewModel.getQuantityTypes().getValue()).stream()
                .filter(quantityTypeDto -> purpose.equals(quantityTypeDto.getPurpose()))
                .collect(Collectors.toList());

        RecyclerView recyclerView = view.findViewById(recyclerViewId);
        EnumAdapter enumAdapter = new EnumAdapter(filteredQuantityTypes, this, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(enumAdapter);

        Button addButton = view.findViewById(buttonId);
        addButton.setOnClickListener(v -> addQuantityType(inflater, recyclerView, enumAdapter, purpose));
        return recyclerView;
    }

    private void addQuantityType(LayoutInflater inflater, RecyclerView recyclerView, EnumAdapter adapter, QuantityPurpose purpose) {
        LoggerManager.getInstance().log("addQuantityType: Apertura dialog per aggiungere QuantityType", "DEBUG");
        List<QuantityTypeDto> existingQuantityTypes = adapter.getQuantityTypes();
        List<QuantityType> availableQuantityTypes = getAvailableQuantityTypes(existingQuantityTypes);

        if (availableQuantityTypes.isEmpty()) {
            Toast.makeText(requireContext(), "Tutti i tipi di quantità sono già presenti", Toast.LENGTH_SHORT).show();
            LoggerManager.getInstance().log("Toast: Tutti i tipi di quantità sono già presenti", "WARN");
            return;
        }

        View dialogView = inflater.inflate(R.layout.dialog_add_quantity_type, null);
        Spinner spinner = dialogView.findViewById(R.id.quantity_type_spinner);
        EditText quantityAvailable = dialogView.findViewById(R.id.quantity_type_available);
        EditText unitsPerQuantityType = dialogView.findViewById(R.id.units_per_quantity_type);

        ArrayAdapter<QuantityType> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, availableQuantityTypes);
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
                String newProviderName = "";
                QuantityTypeDto quantityTypeDto = new QuantityTypeDto();
                quantityTypeDto.setId(UUID.randomUUID());
                quantityTypeDto.setItemId(itemId);
                quantityTypeDto.setPurpose(purpose);
                quantityTypeDto.setCreationDate(ZonedDateTime.now(ZoneId.of("Europe/Rome")));

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

                String unitsPerQuantityTypeText = unitsPerQuantityType.getText().toString();
                if (!unitsPerQuantityTypeText.isEmpty()) {
                    quantityTypeDto.setUnitsPerQuantityType(Integer.parseInt(unitsPerQuantityTypeText));
                } else {
                    quantityTypeDto.setUnitsPerQuantityType(0);
                }

                if (!adapter.containsQuantityType(quantityTypeDto)) {
                    adapter.addQuantityType(quantityTypeDto);
                    generalItemViewModel.getQuantityTypes().getValue().add(quantityTypeDto);
                    recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                    onTextChanged();
                    LoggerManager.getInstance().log("addQuantityType: Nuovo QuantityType aggiunto", "INFO");
                } else {
                    Toast.makeText(requireContext(), "Elemento già presente", Toast.LENGTH_SHORT).show();
                    LoggerManager.getInstance().log("Toast: Elemento già presente", "WARN");
                }
            });
        });
        alertDialog.show();
    }

    private List<QuantityType> getAvailableQuantityTypes(List<QuantityTypeDto> existingQuantityTypes) {
        LoggerManager.getInstance().log("getAvailableQuantityTypes: Calcolo tipi disponibili", "DEBUG");
        List<QuantityType> allQuantityTypes = Arrays.asList(QuantityType.values());
        List<QuantityType> usedQuantityTypes = existingQuantityTypes.stream()
                .map(QuantityTypeDto::getQuantityType)
                .collect(Collectors.toList());
        return allQuantityTypes.stream()
                .filter(quantityType -> !usedQuantityTypes.contains(quantityType))
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

        List<QuantityTypeDto> quantityTypes = generalItemViewModel.getQuantityTypes().getValue();
        if(quantityTypes != null) {
            List<QuantityTypeDto> quantityTypeAvailable = quantityTypes.stream()
                    .filter(quantityType -> QuantityPurpose.AVAILABLE.equals(quantityType.getPurpose()))
                    .collect(Collectors.toList());

            List<QuantityTypeDto> quantityTypeToBeOrdered = quantityTypes.stream()
                    .filter(quantityType -> QuantityPurpose.TO_BE_ORDERED.equals(quantityType.getPurpose()))
                    .collect(Collectors.toList());

            EnumAdapter adapterAvailable = (EnumAdapter) quantityTypesAvailable.getAdapter();
            if(adapterAvailable != null) {
                adapterAvailable.setQuantityTypes(quantityTypeAvailable);
            }

            EnumAdapter adapterToBeOrdered = (EnumAdapter) quantityTypesToBeOrdered.getAdapter();
            if(adapterToBeOrdered != null) {
                adapterToBeOrdered.setQuantityTypes(quantityTypeToBeOrdered);
            }
            LoggerManager.getInstance().log("prefillFields: Quantity types aggiornati", "DEBUG");
        }
    }

    @Override
    public UUID onItemLongClick(QuantityTypeDto quantityTypeDto) throws ExecutionException, InterruptedException {
        LoggerManager.getInstance().log("onItemLongClick: Long click su QuantityType " + (quantityTypeDto != null ? quantityTypeDto.getId() : "null"), "DEBUG");
        if (quantityTypeDto == null) {
            return null;
        }

        Future<?> future = executorService.submit(() -> {
            QuantityTypeEntityDao quantityTypeEntityDao = appDatabase.quantityTypeEntityDao();
            QuantityTypeEntity quantityTypeEntity = EntityDtoMapper.dtoToEntity(quantityTypeDto);
            quantityTypeEntityDao.delete(quantityTypeEntity);
        });
        future.get();

        generalItemViewModel.getQuantityTypes().getValue()
                .removeIf(q -> q.getId().equals(quantityTypeDto.getId()));

        Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show();
        LoggerManager.getInstance().log("Toast: QuantityType eliminato", "INFO");
        return quantityTypeDto.getId();
    }

    @Override
    public void onTextChanged() {
        LoggerManager.getInstance().log("onTextChanged: Aggiornamento porzioni totali", "DEBUG");
        TextView totPortionsAvailableTextView = requireView().findViewById(R.id.tot_portions_avalaible);
        TextView totPortionsToBeOrderedTextView = requireView().findViewById(R.id.tot_portions_to_be_ordered);
        TextView totPortionsAvailablePlusOrderedTextView = requireView().findViewById(R.id.tot_portions_avalaible_plus_ordered);
        TextView portionsPerWeekendTextView = requireView().findViewById(R.id.portions_per_weekend);
        CardView itemNameAndTotPortionsCardView = requireView().findViewById(R.id.item_name_tot_portions);

        List<QuantityTypeDto> quantityTypeDtos = generalItemViewModel.getQuantityTypes().getValue();
        Long totPortionsAvailable = calculateTotPortions(quantityTypeDtos, QuantityPurpose.AVAILABLE);
        Long totPortionsToBeOrdered = calculateTotPortions(quantityTypeDtos, QuantityPurpose.TO_BE_ORDERED);
        long totPortionsAvailablePlusOrdered = totPortionsAvailable + totPortionsToBeOrdered;

        totPortionsAvailableTextView.setText(String.valueOf(totPortionsAvailable));
        totPortionsToBeOrderedTextView.setText(String.valueOf(totPortionsToBeOrdered));
        totPortionsAvailablePlusOrderedTextView.setText(String.valueOf(totPortionsAvailablePlusOrdered));

        int portionsPerWeekend = Integer.parseInt(portionsPerWeekendTextView.getText().toString().isEmpty() ? "0" : portionsPerWeekendTextView.getText().toString());

        if (totPortionsAvailablePlusOrdered >= portionsPerWeekend) {
            itemNameAndTotPortionsCardView.setCardBackgroundColor(ContextCompat.getColor(requireView().getContext(), R.color.green));
        } else {
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

                ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Rome"));

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
