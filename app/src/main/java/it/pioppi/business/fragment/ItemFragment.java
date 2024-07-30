package it.pioppi.business.fragment;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import it.pioppi.R;
import it.pioppi.business.activity.SearchableActivity;
import it.pioppi.business.viewmodel.ItemViewModel;
import it.pioppi.business.adapter.ItemAdapter;
import it.pioppi.business.dto.ItemDto;
import it.pioppi.database.AppDatabase;
import it.pioppi.database.mapper.EntityDtoMapper;
import it.pioppi.database.model.entity.ItemDetailEntity;
import it.pioppi.database.model.entity.ItemEntity;
import it.pioppi.database.model.entity.ItemStatus;
import it.pioppi.database.model.entity.ProviderEntity;
import it.pioppi.database.model.entity.QuantityTypeEntity;
import it.pioppi.database.repository.ItemEntityRepository;

public class ItemFragment extends Fragment implements ItemAdapter.OnItemClickListener, ItemAdapter.OnLongItemClickListener {

    private AppDatabase appDatabase;
    private ItemAdapter itemAdapter;
    private ExecutorService executorService;
    private ItemViewModel itemViewModel;
    private RecyclerView recyclerView;
    private ItemEntityRepository itemEntityRepository;
    private AlertDialog newItemDialog;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appDatabase = AppDatabase.getInstance(getContext());
        executorService = Executors.newSingleThreadExecutor();
        itemEntityRepository = new ItemEntityRepository(requireActivity().getApplication());
        itemViewModel = new ViewModelProvider(requireActivity()).get(ItemViewModel.class);

        try {
            List<ItemDto> itemDtoList = loadItems();
            itemViewModel.setItems(itemDtoList);
            Log.d("ItemFragment", "Items set in ViewModel: " + itemDtoList.size());

        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        itemAdapter = new ItemAdapter(new ArrayList<>(), this, this, getContext());

        itemViewModel.getItems().observe(getViewLifecycleOwner(), itemList -> {
            itemAdapter.setItemList(itemList);
            Log.d("ItemFragment", "Observed items: " + itemList.size());
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
            recyclerView.setAdapter(itemAdapter);
        });

        setupMenuToolbar();

        FloatingActionButton fab = view.findViewById(R.id.new_item_fab);
        fab.setOnClickListener(v -> addNewItem(inflater, itemViewModel.getItems().getValue()));

        return view;
    }

    private void setupMenuToolbar() {
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_item_fragment, menu);
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
                } else if (itemId == R.id.filter_by_provider) {
                    filterItemsByProvider();
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
                } else if (itemId == R.id.filter_by_provider_and_status) {
                    filterItemsByProviderAndStatus();
                    return true;
                } else if (itemId == R.id.search) {
                    SearchView searchView = (SearchView) menuItem.getActionView();
                    Objects.requireNonNull(searchView).setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            // Lancia l'intent di ricerca con i dati del ViewModel
                            Intent intent = new Intent(requireContext(), SearchableActivity.class);
                            intent.setAction(Intent.ACTION_SEARCH);
                            intent.putExtra(SearchManager.QUERY, query);

                            // Aggiungi i dati del ViewModel all'intent
                            ArrayList<ItemDto> items = new ArrayList<>(Objects.requireNonNull(itemViewModel.getItems().getValue()));
                            intent.putParcelableArrayListExtra("items", items);

                            startActivity(intent);
                            return true;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                            return false;
                        }
                    });
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void addProviderItem(LayoutInflater inflater, ArrayAdapter<String> spinnerAdapter, Spinner providerSpinner) {
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
                    return;
                }

                List<String> providerNames;
                try {
                    providerNames = executorService.submit(() -> appDatabase.providerEntityDao().getProviderNames()).get();
                } catch (ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }

                providerNames.remove(null);
                boolean isUnique = providerNames.stream().noneMatch(provider -> provider.equalsIgnoreCase(newProviderName));

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
                        alertDialog.dismiss();
                    });
                });
            });
        });
        alertDialog.show();
    }

    private void addNewItem(@NonNull LayoutInflater inflater, List<ItemDto> itemDtos) {
        View dialogView = inflater.inflate(R.layout.new_item_alert, null);

        Spinner providerSpinner = dialogView.findViewById(R.id.provider_spinner);
        EditText newItemNameEditText = dialogView.findViewById(R.id.new_item_name);
        newItemNameEditText.requestFocus();
        openKeyboard(newItemNameEditText);

        List<String> providers;
        try {
            providers = executorService.submit(() -> appDatabase.providerEntityDao().getProviderNames()).get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        providers.remove(null);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, providers);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        providerSpinner.setAdapter(spinnerAdapter);

        Button addProviderButton = dialogView.findViewById(R.id.add_provider_button);
        addProviderButton.setOnClickListener(v -> addProviderItem(inflater, spinnerAdapter, providerSpinner));

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(dialogView)
                .setPositiveButton("Aggiungi", null)
                .setNegativeButton("Indietro", (dialog, id) -> dialog.cancel());

        newItemDialog = builder.create();
        newItemDialog.setOnShowListener(dlg -> {
            Button positiveButton = newItemDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
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

                executorService.execute(() -> {
                    try {
                        UUID existingItem = appDatabase.itemEntityDao().getItemByName(newItemName);
                        Integer getMaxFtsId = appDatabase.itemFTSEntityDao().getNextId();
                        if (existingItem != null) {
                            requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Esiste già un prodotto con questo nome", Toast.LENGTH_SHORT).show());
                            return;
                        }

                        LocalDateTime now = LocalDateTime.now();

                        ItemEntity newItem = new ItemEntity();
                        newItem.setId(UUID.randomUUID());
                        newItem.setFtsId(getMaxFtsId);
                        newItem.setName(newItemName);
                        newItem.setTotPortions(0);
                        newItem.setStatus(ItemStatus.WHITE);
                        newItem.setBarcode("FAKE_BARCODE");
                        newItem.setCreationDate(now);
                        appDatabase.runInTransaction(() -> itemEntityRepository.insert(newItem));

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

                        appDatabase.runInTransaction(() -> {
                            appDatabase.providerEntityDao().insert(providerEntity);
                            appDatabase.itemDetailEntityDao().insert(itemDetailEntity);
                            appDatabase.quantityTypeEntityDao().insert(quantityTypeEntity);
                        });

                        itemDtos.add(EntityDtoMapper.entityToDto(newItem));
                        requireActivity().runOnUiThread(() -> {
                            itemAdapter.setItemList(itemDtos);
                            newItemDialog.dismiss();
                        });
                    } catch (Exception e) {
                        requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Errore durante la creazione del prodotto", Toast.LENGTH_SHORT).show());
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
                            List<ItemDto> currentList = itemViewModel.getItems().getValue();
                            if (currentList != null) {
                                List<ItemDto> updatedList = new ArrayList<>(currentList);
                                int index = updatedList.indexOf(itemDto);
                                if (index != RecyclerView.NO_POSITION) {
                                    updatedList.remove(index);
                                    itemViewModel.setItems(updatedList);
                                    itemAdapter.setItemList(updatedList);
                                    recyclerView.setAdapter(itemAdapter);
                                    Toast.makeText(getContext(), "Elemento eliminato", Toast.LENGTH_SHORT).show();
                                }
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

    }



    private void filterItemsByNameAscending() {
        Objects.requireNonNull(itemViewModel.getItems().getValue()).sort(Comparator.comparing(ItemDto::getName, String.CASE_INSENSITIVE_ORDER));
        itemAdapter.setItemList(itemViewModel.getItems().getValue());
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        recyclerView.setAdapter(itemAdapter);

    }

    private void filterItemsByNameDescending() {
        Objects.requireNonNull(itemViewModel.getItems().getValue()).sort(Comparator.comparing(ItemDto::getName, String.CASE_INSENSITIVE_ORDER).reversed());
        itemAdapter.setItemList(itemViewModel.getItems().getValue());
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        recyclerView.setAdapter(itemAdapter);

    }

    private void filterItemsByProvider() {

    }

    private void filterItemsByStatus(ItemStatus status) {
        List<ItemDto> itemDtoList = Objects.requireNonNull(itemViewModel.getItems().getValue()).stream()
                .filter(item -> item.getStatus().equals(status))
                .collect(Collectors.toList());
        itemAdapter.setItemList(itemDtoList);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        recyclerView.setAdapter(itemAdapter);
    }

    private void filterItemsByProviderAndStatus() {
        // Implementa la logica per filtrare gli item per provider e status
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