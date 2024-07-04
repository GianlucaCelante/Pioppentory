package it.pioppi.business.fragment;

import static androidx.core.content.ContextCompat.getSystemService;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import it.pioppi.R;
import it.pioppi.business.adapter.ItemAdapter;
import it.pioppi.business.dto.ItemDto;
import it.pioppi.business.dto.ProviderDto;
import it.pioppi.database.AppDatabase;
import it.pioppi.database.dao.ItemEntityDao;
import it.pioppi.database.dao.QuantityTypeEntityDao;
import it.pioppi.database.mapper.EntityDtoMapper;
import it.pioppi.database.model.entity.ItemDetailEntity;
import it.pioppi.database.model.entity.ItemEntity;
import it.pioppi.database.model.entity.ItemStatus;
import it.pioppi.database.model.entity.ProviderEntity;
import it.pioppi.database.model.entity.QuantityTypeEntity;

public class ItemFragment extends Fragment implements ItemAdapter.OnItemClickListener, ItemAdapter.OnLongItemClickListener {

    private AppDatabase appDatabase;
    private ItemAdapter itemAdapter;
    private ExecutorService executorService;
    private List<ItemDto> itemDtos;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appDatabase = AppDatabase.getInstance(getContext());
        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getContext(), 4);
        recyclerView.setLayoutManager(mLayoutManager);

        try {
            itemDtos = loadItems();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        itemAdapter = new ItemAdapter(itemDtos, this, this, getContext());
        recyclerView.setAdapter(itemAdapter);

        FloatingActionButton fab = view.findViewById(R.id.new_item_fab);
        fab.setOnClickListener(v -> addNewItem(inflater, itemDtos));

        return view;

    }

    private void addProviderItem(LayoutInflater inflater, ArrayAdapter<String> spinnerAdapter, Spinner providerSpinner) {
        View dialogView = inflater.inflate(R.layout.new_provider_spinner, null);

        EditText newProviderNameEditText = dialogView.findViewById(R.id.new_item_name);
        newProviderNameEditText.requestFocus();
        openKeyboard(newProviderNameEditText);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(dialogView)
                .setPositiveButton("Aggiungi", (dialog, id) -> {
                    String newProviderName = newProviderNameEditText.getText().toString().trim();

                    if (newProviderName.isEmpty()) {
                        Toast.makeText(getContext(), "Inserisci un nome per il nuovo fornitore", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    AtomicReference<List<String>> providerNames = new AtomicReference<>(new ArrayList<>());
                    Future<?> future = executorService.submit(() -> providerNames.set(appDatabase.providerEntityDao().getProviderNames()));

                    try {
                        future.get();
                    } catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    List<String> providers = providerNames.get();
                    providers.remove(null);
                    boolean isUnique = providers.stream().noneMatch(provider -> provider.equalsIgnoreCase(newProviderName));

                    if (!isUnique) {
                        Toast.makeText(getContext(), "Esiste già un fornitore con questo nome", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    LocalDateTime now = LocalDateTime.now();

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
                        });
                    });
                })
                .setNegativeButton("Indietro", (dialog, id) -> dialog.cancel());

        builder.show();
    }


    private void addNewItem(@NonNull LayoutInflater inflater, List<ItemDto> itemDtos) {
        View dialogView = inflater.inflate(R.layout.new_item_spinner, null);

        Spinner providerSpinner = dialogView.findViewById(R.id.provider_spinner);
        EditText newItemNameEditText = dialogView.findViewById(R.id.new_item_name);
        newItemNameEditText.requestFocus();
        openKeyboard(newItemNameEditText);

        AtomicReference<List<String>> providerNames = new AtomicReference<>(new ArrayList<>());
        Future<?> future = executorService.submit(() -> providerNames.set(appDatabase.providerEntityDao().getProviderNames()));

        try {
            future.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        List<String> providers = providerNames.get();
        providers.remove(null);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, providers);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        providerSpinner.setAdapter(spinnerAdapter);

        Button addProviderButton = dialogView.findViewById(R.id.add_provider_button);
        addProviderButton.setOnClickListener(v -> addProviderItem(inflater, spinnerAdapter, providerSpinner));

        AtomicReference<ItemDto> newItemDto = new AtomicReference<>(new ItemDto());
        // Build and show the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(dialogView)
                .setPositiveButton("Aggiungi", (dialog, id) -> {
                    String provider = providerSpinner.getSelectedItem() != null ? providerSpinner.getSelectedItem().toString() : null;
                    String newItemName = newItemNameEditText.getText().toString();

                    if (newItemName.isEmpty()) {
                        Toast.makeText(getContext(), "Inserisci un nome per il nuovo prodotto", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (provider == null || provider.isEmpty()) {
                        Toast.makeText(getContext(), "Seleziona un fornitore", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    LocalDateTime now = LocalDateTime.now();

                    ItemEntity newItem = new ItemEntity();
                    newItem.setId(UUID.randomUUID());
                    newItem.setName(newItemName);
                    newItem.setTotPortions(0);
                    newItem.setStatus(ItemStatus.WHITE);
                    newItem.setCreationDate(now);

                    ProviderEntity providerEntity = new ProviderEntity();
                    providerEntity.setId(UUID.randomUUID());
                    providerEntity.setItemId(newItem.getId());
                    providerEntity.setName(provider);
                    providerEntity.setCreationDate(now);

                    ItemDetailEntity itemDetailEntity = new ItemDetailEntity();
                    itemDetailEntity.setId(UUID.randomUUID());
                    itemDetailEntity.setItemId(newItem.getId());
                    itemDetailEntity.setCreationDate(now);

                    QuantityTypeEntity quantityTypeEntity = new QuantityTypeEntity();
                    quantityTypeEntity.setId(UUID.randomUUID());
                    quantityTypeEntity.setItemId(newItem.getId());
                    quantityTypeEntity.setCreationDate(now);

                    executorService.execute(() -> {
                        appDatabase.itemEntityDao().insert(newItem);
                        appDatabase.providerEntityDao().insert(providerEntity);
                        appDatabase.itemDetailEntityDao().insert(itemDetailEntity);
                        appDatabase.quantityTypeEntityDao().insert(quantityTypeEntity);
                    });

                    newItemDto.set(EntityDtoMapper.entityToDto(newItem));
                    itemDtos.add(newItemDto.get());
                    itemAdapter.setItemList(itemDtos);
                })
                .setNegativeButton("Indietro", (dialog, id) -> dialog.cancel());

        builder.show();
    }

    private List<ItemDto> loadItems() throws ExecutionException, InterruptedException {
    AtomicReference<List<ItemDto>> itemDtos = new AtomicReference<>(new ArrayList<>());

    Future<?> future = executorService.submit(() -> {

            ItemEntityDao entityDao = appDatabase.itemEntityDao();
            List<ItemEntity> allItems = entityDao.getAllItems();
            itemDtos.set(EntityDtoMapper.entityToDto(allItems));

        });
        future.get();
        return itemDtos.get();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    @Override
    public void onItemClick(ItemDto item) {

        if (item == null || item.getId() == null) {
            return;
        }
        NavController navController = NavHostFragment.findNavController(this);
        Bundle bundle = new Bundle();
        bundle.putString("itemId", item.getId().toString());
        navController.navigate(R.id.action_itemFragment_to_itemDetailFragment, bundle);

    }

    @Override
    public UUID onLongItemClick(ItemDto itemDto) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Conferma eliminazione")
                .setMessage("Sei sicuro di voler eliminare questo elemento?")
                .setPositiveButton("Elimina", (dialog, which) -> executorService.execute(() -> {
                    try {
                        ItemEntity itemEntity = EntityDtoMapper.dtoToEntity(itemDto);
                        appDatabase.itemEntityDao().delete(itemEntity);

                        // Aggiorna la lista degli item nel thread principale
                        requireActivity().runOnUiThread(() -> {
                            int index = itemDtos.indexOf(itemDto);
                            if (index != RecyclerView.NO_POSITION) {
                                itemDtos.remove(index);
                                itemAdapter.notifyItemRemoved(index);
                                Toast.makeText(getContext(), "Elemento eliminato", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Errore durante l'eliminazione", Toast.LENGTH_SHORT).show();
                        });
                    }
                }))
                .setNegativeButton("Annulla", null)
                .show();

        return itemDto.getId();
    }


    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void openKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
        }
    }
}