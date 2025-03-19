package it.pioppi.business.fragment;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import it.pioppi.ConstantUtils;
import it.pioppi.R;
import it.pioppi.business.adapter.PreviewItemsAdapter;
import it.pioppi.business.dto.ItemDetailDto;
import it.pioppi.business.dto.ItemTagJoinDto;
import it.pioppi.business.viewmodel.GeneralItemViewModel;
import it.pioppi.business.adapter.ItemAdapter;
import it.pioppi.business.dto.ItemDto;
import it.pioppi.database.AppDatabase;
import it.pioppi.database.entity.ItemHistoryEntity;
import it.pioppi.database.entity.ItemTagJoinEntity;
import it.pioppi.database.entity.ItemWithDetailEntity;
import it.pioppi.database.entity.ProviderEntity;
import it.pioppi.database.mapper.EntityDtoMapper;
import it.pioppi.database.entity.ItemDetailEntity;
import it.pioppi.database.entity.ItemEntity;
import it.pioppi.database.model.ItemStatus;
import it.pioppi.database.entity.QuantityTypeEntity;
import it.pioppi.database.repository.ItemEntityRepository;

public class ItemFragment extends Fragment implements ItemAdapter.OnItemClickListener, ItemAdapter.OnLongItemClickListener, Searchable {

    private AppDatabase appDatabase;
    private ItemAdapter itemAdapter;
    private ExecutorService executorService;
    private GeneralItemViewModel generalItemViewModel;
    private RecyclerView recyclerView;
    private ItemEntityRepository itemEntityRepository;
    private AlertDialog newItemDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appDatabase = AppDatabase.getInstance(getContext());
        executorService = Executors.newSingleThreadExecutor();
        itemEntityRepository = new ItemEntityRepository(requireActivity().getApplication());
        generalItemViewModel = new ViewModelProvider(requireActivity()).get(GeneralItemViewModel.class);

        try {
            List<ItemDto> itemDtoList = loadItems();
            List <ItemDetailDto> itemDetailDtoList = loadItemDetails();

            List<ItemTagJoinDto> itemTagJoinDtoList = loadItemTagJoin();
            Map<UUID, Set<UUID>> itemTagJoinMap = new HashMap<>();
            for (ItemTagJoinDto join : itemTagJoinDtoList) {
                itemTagJoinMap.computeIfAbsent(join.getTagId(), k -> new HashSet<>()).add(join.getItemId());
            }

            generalItemViewModel.setItemTagJoins(itemTagJoinMap);
            generalItemViewModel.setItems(itemDtoList);
            generalItemViewModel.setItemDetails(itemDetailDtoList);
            Log.d("ItemFragment", "Items set in ViewModel: " + itemDtoList.size());
            Log.d("ItemFragment", "ItemDetails set in ViewModel: " + itemDetailDtoList.size());
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private List<ItemTagJoinDto> loadItemTagJoin() throws ExecutionException, InterruptedException {

        List<ItemTagJoinDto> itemTagJoinDtos = new ArrayList<>();
        Future<?> future = executorService.submit(() -> {
            List<ItemTagJoinEntity> itemTagJoinEntities = appDatabase.itemTagJoinDao().getAll();

            itemTagJoinEntities.forEach(itemTagJoinEntity -> {
                ItemTagJoinDto itemTagJoinDto = EntityDtoMapper.entityToDto(itemTagJoinEntity);
                itemTagJoinDtos.add(itemTagJoinDto);
            });
        });
        future.get();
        return itemTagJoinDtos;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_items);
        itemAdapter = new ItemAdapter(new ArrayList<>(), this, this, getContext());

        generalItemViewModel.getFilteredItems().observe(getViewLifecycleOwner(), itemList -> {
            itemAdapter.setItemList(itemList);
            Log.d("ItemFragment", "Observed items: " + itemList.size());
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), ConstantUtils.GRID_LAYOUT_NUMBER_COLUMNS));
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(itemAdapter);
        });

        setupMenuToolbar();

        FloatingActionButton fab = view.findViewById(R.id.new_item_fab);
        fab.setOnClickListener(v -> addNewItem(inflater, generalItemViewModel));

        return view;
    }

    private void setupMenuToolbar() {
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_item_fragment, menu);

                MenuItem searchItem = menu.findItem(R.id.action_search);
                SearchManager searchManager = (SearchManager) requireActivity().getSystemService(Context.SEARCH_SERVICE);
                SearchView searchView = (SearchView) searchItem.getActionView();
                if (searchManager != null) {
                    searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));
                }

                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        generalItemViewModel.setQuery(query);
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        generalItemViewModel.setQuery(newText);
                        return true;
                    }
                });

                MenuItem closeInventory = menu.findItem(R.id.close_inventory);
                closeInventory.setOnMenuItemClickListener(item -> {
                    previewItems();
                    return true;
                });

            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.filter_by_A_to_Z) {
                    filterItemsByNameAscending();
                    return true;
                } else if (itemId == R.id.filter_by_Z_to_A) {
                    filterItemsByNameDescending();
                    return true;
                } else if (itemId == R.id.filter_by_status_white) {
                    filterItemsByStatus(ItemStatus.WHITE);
                    return true;
                } else if (itemId == R.id.filter_by_status_blue) {
                    filterItemsByStatus(ItemStatus.BLUE);
                    return true;
                } else if (itemId == R.id.filter_by_status_red) {
                    filterItemsByStatus(ItemStatus.RED);
                    return true;
                } else if (itemId == R.id.filter_by_status_green) {
                    filterItemsByStatus(ItemStatus.GREEN);
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void previewItems() {
        // Filtra gli ItemDto con ItemStatus.BLUE
        List<ItemDto> blueItems = Objects.requireNonNull(generalItemViewModel.getItems().getValue()).stream()
                .filter(item -> ItemStatus.BLUE.equals(item.getStatus()))
                .collect(Collectors.toList());

        if (blueItems.isEmpty()) {
            Toast.makeText(getContext(), "Non ci sono elementi modificati.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostra il dialog con la lista degli Item filtrati
        showPreviewDialog(blueItems);
    }

    private void showPreviewDialog(List<ItemDto> itemList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Elementi Modificati");

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_preview_items, null);
        builder.setView(dialogView);

        RecyclerView recyclerView = dialogView.findViewById(R.id.preview_items_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        PreviewItemsAdapter adapter = new PreviewItemsAdapter(itemList);
        recyclerView.setAdapter(adapter);

        builder.setPositiveButton("Conferma Chiusura", null);
        builder.setNegativeButton("Annulla", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {

                saveItemHistory(itemList);

                resetItemStatus(itemList);

                requireActivity().runOnUiThread(() -> {
                    generalItemViewModel.setItems(generalItemViewModel.getItems().getValue());
                    itemAdapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Chiusura dell'inventario salvata con successo.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
                dialog.dismiss();
            });
        });
        dialog.show();
    }

    private void resetItemStatus(List<ItemDto> itemList) {
        executorService.execute(() -> {
            try {
                for (ItemDto item : itemList) {
                    item.setStatus(ItemStatus.WHITE);
                    ItemEntity itemEntity = EntityDtoMapper.dtoToEntity(item);
                    appDatabase.itemEntityDao().update(itemEntity);
                }
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Errore durante il ripristino dello stato degli elementi", Toast.LENGTH_SHORT).show());
            }
        });

    }

    private void saveItemHistory(List<ItemDto> itemList) {
        executorService.execute(() -> {
            try {

                ZonedDateTime closureDate = ZonedDateTime.now(ZoneId.of("Europe/Rome")).truncatedTo(ChronoUnit.DAYS);

                for (ItemDto item : itemList) {

                    ItemHistoryEntity itemHistoryByItemName = appDatabase.itemHistoryEntityDao().getItemHistoryByItemNameInClosureDate(item.getName(), String.valueOf(LocalDate.from(closureDate)));
                    ItemWithDetailEntity itemEntity = appDatabase.itemEntityDao().getItemWithDetail(item.getId());
                    ProviderEntity providerEntity = appDatabase.providerEntityDao().getProviderById(itemEntity.item.getProviderId());

                    ItemHistoryEntity historyRecord = new ItemHistoryEntity();

                    if(itemHistoryByItemName != null) {
                        historyRecord.setId(itemHistoryByItemName.getId());
                    } else {
                        historyRecord.setId(UUID.randomUUID());
                    }

                    historyRecord.setInventoryClosureDate(LocalDate.from(closureDate));
                    historyRecord.setItemName(item.getName());
                    historyRecord.setBarcode(item.getBarcode());
                    historyRecord.setQuantityPresent(item.getTotPortions());
                    historyRecord.setQuantityOrdered(itemEntity.itemDetail.getQuantityToBeOrdered());
                    historyRecord.setPortionsPerWeekend(Long.valueOf(itemEntity.itemDetail.getPortionsPerWeekend()));
                    historyRecord.setDeliveryDate(LocalDate.from(itemEntity.itemDetail.getDeliveryDate()));
                    historyRecord.setProviderName(providerEntity != null ? providerEntity.getName() : null);
                    historyRecord.setNote(item.getNote());
                    historyRecord.setCreationDate(closureDate);
                    historyRecord.setLastUpdate(closureDate);
                    appDatabase.itemHistoryEntityDao().upsert(historyRecord);
                }

            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Errore durante il salvataggio: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void addNewItem(@NonNull LayoutInflater inflater, GeneralItemViewModel generalItemViewModel) {

        List<ItemDto> items = generalItemViewModel.getItems().getValue();

        View dialogView = inflater.inflate(R.layout.new_item_alert, null);
        EditText newItemNameEditText = dialogView.findViewById(R.id.new_item_name);
        newItemNameEditText.requestFocus();
        openKeyboard(newItemNameEditText);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(dialogView)
                .setPositiveButton("Aggiungi", null)
                .setNegativeButton("Indietro", (dialog, id) -> dialog.cancel());

        newItemDialog = builder.create();
        newItemDialog.setOnShowListener(dlg -> {
            Button positiveButton = newItemDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String newItemName = newItemNameEditText.getText().toString();

                if (newItemName.isEmpty()) {
                    Toast.makeText(getContext(), "Inserisci un nome per il nuovo prodotto", Toast.LENGTH_SHORT).show();
                    return;
                }

                executorService.execute(() -> {
                    try {
                        UUID existingItem = appDatabase.itemEntityDao().getItemByName(newItemName);
                        if (existingItem != null) {
                            requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Esiste già un prodotto con questo nome", Toast.LENGTH_SHORT).show());
                            return;
                        }

                        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Rome"));

                        ItemEntity newItem = new ItemEntity();
                        UUID itemId = UUID.randomUUID();
                        newItem.setId(itemId);
                        newItem.setName(newItemName);
                        newItem.setTotPortions(0L);
                        newItem.setStatus(ItemStatus.WHITE);
                        newItem.setBarcode("FAKE_BARCODE");
                        newItem.setCreationDate(now);

                        ItemDetailEntity itemDetailEntity = new ItemDetailEntity();
                        itemDetailEntity.setId(UUID.randomUUID());
                        itemDetailEntity.setItemId(itemId);
                        itemDetailEntity.setCreationDate(now);

                        QuantityTypeEntity quantityTypeEntity = new QuantityTypeEntity();
                        quantityTypeEntity.setId(UUID.randomUUID());
                        quantityTypeEntity.setItemId(itemId);
                        quantityTypeEntity.setCreationDate(now);

                        // Esegui tutte le operationi in un'unica transazione
                        appDatabase.runInTransaction(() -> {
                            try {
                                appDatabase.itemEntityDao().insert(newItem);
                                appDatabase.itemDetailEntityDao().insert(itemDetailEntity);
                                appDatabase.quantityTypeEntityDao().insert(quantityTypeEntity);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });

                        generalItemViewModel.updateItem(EntityDtoMapper.entityToDto(newItem));
                        requireActivity().runOnUiThread(() -> {
                            itemAdapter.setItemList(items);
                            newItemDialog.dismiss();
                        });
                    } catch (Exception e) {
                        requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Errore durante la creazione del prodotto: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
                    }
                });
            });
        });
        newItemDialog.show();

    }

    private List<ItemDto> loadItems() throws ExecutionException, InterruptedException {
        List<ItemDto> itemDtos = new ArrayList<>();
        Future<?> future = executorService.submit(() -> {
            List<ItemEntity> itemEntities = appDatabase.itemEntityDao().getAllItems();

            itemEntities.forEach(itemEntity -> {
                ItemDto itemDto = EntityDtoMapper.entityToDto(itemEntity);
                itemDtos.add(itemDto);
            });
        });
        future.get();
        return itemDtos;
    }

    private List<ItemDetailDto> loadItemDetails() throws ExecutionException, InterruptedException {
        List<ItemDetailDto> itemDetailDtos = new ArrayList<>();
        Future<?> future = executorService.submit(() -> {
            List<ItemDetailEntity> itemDetailEntities = appDatabase.itemDetailEntityDao().getAllItemDetails();

            itemDetailEntities.forEach(itemDetailEntity -> {
                ItemDetailDto itemDto = EntityDtoMapper.detailEntityToDto(itemDetailEntity);
                itemDetailDtos.add(itemDto);
            });
        });
        future.get();
        return itemDetailDtos;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
        if (newItemDialog != null && newItemDialog.isShowing()) {
            newItemDialog.dismiss();
        }
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
    public void onLongItemClick(ItemDto itemDto) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Conferma eliminazione")
                .setMessage("Sei sicuro di voler eliminare questo elemento?")
                .setPositiveButton("Elimina", (dialog, which) -> executorService.execute(() -> {
                    try {
                        ItemEntity itemEntity = EntityDtoMapper.dtoToEntity(itemDto);
                        itemEntityRepository.delete(itemEntity);

                        requireActivity().runOnUiThread(() -> {
                            List<ItemDto> currentList = generalItemViewModel.getItems().getValue();
                            if (currentList != null) {
                                List<ItemDto> updatedList = new ArrayList<>(currentList);
                                int index = updatedList.indexOf(itemDto);
                                if (index != RecyclerView.NO_POSITION) {
                                    updatedList.remove(index);
                                    generalItemViewModel.setItems(updatedList);
                                    itemAdapter.setItemList(updatedList);
                                    recyclerView.setAdapter(itemAdapter);
                                    Toast.makeText(getContext(), "Elemento eliminato", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    } catch (Exception e) {
                        requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Errore durante l'eliminazione", Toast.LENGTH_SHORT).show());
                    }
                }))
                .setNegativeButton("Annulla", null)
                .show();
    }

    private void filterItemsByNameAscending() {
        Objects.requireNonNull(generalItemViewModel.getItems().getValue()).sort(Comparator.comparing(ItemDto::getName, String.CASE_INSENSITIVE_ORDER));
        itemAdapter.setItemList(generalItemViewModel.getItems().getValue());
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), ConstantUtils.GRID_LAYOUT_NUMBER_COLUMNS));
        recyclerView.setAdapter(itemAdapter);
    }

    private void filterItemsByNameDescending() {
        Objects.requireNonNull(generalItemViewModel.getItems().getValue()).sort(Comparator.comparing(ItemDto::getName, String.CASE_INSENSITIVE_ORDER).reversed());
        itemAdapter.setItemList(generalItemViewModel.getItems().getValue());
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), ConstantUtils.GRID_LAYOUT_NUMBER_COLUMNS));
        recyclerView.setAdapter(itemAdapter);
    }

    private void filterItemsByStatus(ItemStatus status) {
/*        List<ItemDto> itemDtoList = Objects.requireNonNull(itemViewModel.getItems().getValue()).stream()
                .filter(item -> item.getStatus().equals(status))
                .collect(Collectors.toList());
        itemAdapter.setItemList(itemDtoList);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), ConstantUtils.GRID_LAYOUT_NUMBER_COLUMNS));
        recyclerView.setAdapter(itemAdapter);*/
    }

    private void openKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
        }
    }

    @Override
    public void onSearchQueryChanged(String query) {
        generalItemViewModel.setQuery(query);
    }
}
