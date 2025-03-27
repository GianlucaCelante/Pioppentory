package it.pioppi.business.fragment;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import it.pioppi.R;
import it.pioppi.business.adapter.ItemTagsAdapter;
import it.pioppi.business.adapter.ItemsInTagAdapter;
import it.pioppi.business.dto.item.detail.ItemDetailDto;
import it.pioppi.business.dto.item.ItemDto;
import it.pioppi.business.dto.item.tag.ItemTagDto;
import it.pioppi.business.viewmodel.GeneralItemViewModel;
import it.pioppi.database.AppDatabase;
import it.pioppi.database.entity.ItemEntity;
import it.pioppi.database.mapper.EntityDtoMapper;
import it.pioppi.database.entity.ItemDetailEntity;
import it.pioppi.database.entity.ItemTagEntity;
import it.pioppi.database.entity.ItemTagJoinEntity;
import it.pioppi.utils.LoggerManager;

public class ItemTagsFragment extends Fragment implements ItemTagsAdapter.OnItemClickListener, ItemsInTagAdapter.OnItemClickListener, Searchable {
    private AppDatabase appDatabase;
    private ItemTagsAdapter itemTagsAdapter;
    private ExecutorService executorService;
    private GeneralItemViewModel generalItemViewModel;
    private RecyclerView recyclerViewTags;
    private UUID itemId;
    private List<ItemDto> itemDtos;
    private List<ItemDetailDto> itemDetailDtos;
    private Map<UUID, Set<UUID>> itemTagJoins;

    public ItemTagsFragment() {
        // Required empty public constructor
        LoggerManager.getInstance().log("ItemTagsFragment constructor called", "DEBUG");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LoggerManager.getInstance().log("ItemTagsFragment onCreate started", "INFO");
        super.onCreate(savedInstanceState);
        appDatabase = AppDatabase.getInstance(getContext());
        executorService = Executors.newSingleThreadExecutor();
        generalItemViewModel = new ViewModelProvider(requireActivity()).get(GeneralItemViewModel.class);

        Bundle bundle = getArguments();
        if (bundle != null) {
            itemId = UUID.fromString(bundle.getString("itemId"));
            LoggerManager.getInstance().log("Item ID received: " + itemId, "DEBUG");
        } else {
            LoggerManager.getInstance().log("No arguments found in onCreate", "INFO");
        }

        try {
            List<ItemTagDto> itemTagDtos = fetchItemTagsDtos(itemId);
            LoggerManager.getInstance().log("Fetched item tags count: " + (itemTagDtos != null ? itemTagDtos.size() : 0), "DEBUG");
            // Salva la lista filtrata nel ViewModel per il dettaglio corrente
            generalItemViewModel.setItemTags(itemTagDtos);

            // Carica anche la lista globale di tag dal database
            List<ItemTagDto> allTags = fetchAllItemTagsDtos();
            LoggerManager.getInstance().log("Fetched all global tags count: " + (allTags != null ? allTags.size() : 0), "DEBUG");
            generalItemViewModel.setAllItemTags(allTags);

            itemDtos = fetchItemsForTags(itemTagDtos);
            LoggerManager.getInstance().log("Fetched items for tags count: " + (itemDtos != null ? itemDtos.size() : 0), "DEBUG");
            itemDetailDtos = fetchItemDetails(itemDtos);
            LoggerManager.getInstance().log("Fetched item details count: " + (itemDetailDtos != null ? itemDetailDtos.size() : 0), "DEBUG");
            itemTagJoins = generalItemViewModel.getItemTagJoins();

            itemTagsAdapter = new ItemTagsAdapter(itemTagDtos, itemDtos, itemDetailDtos, itemTagJoins,
                    this, this, getContext());
        } catch (ExecutionException | InterruptedException e) {
            LoggerManager.getInstance().logException(e);
            throw new RuntimeException(e);
        }
        LoggerManager.getInstance().log("ItemTagsFragment onCreate completed", "INFO");
    }

    private List<ItemDto> fetchItemsForTags(List<ItemTagDto> itemTagDtos) {
        LoggerManager.getInstance().log("Fetching items for tags...", "DEBUG");
        List<ItemDto> items = new ArrayList<>();
        itemTagDtos.forEach(itemTagDto -> {
            try {
                Future<?> future = executorService.submit(() -> {
                    // Recupera gli item per il tag
                    java.util.List<ItemEntity> itemsForTag = appDatabase.itemTagEntityDao().getItemsForTag(itemTagDto.getId());
                    itemsForTag.forEach(itemEntity -> {
                        ItemDto itemDto = EntityDtoMapper.entityToDto(itemEntity);
                        items.add(itemDto);
                        LoggerManager.getInstance().log("Item fetched for tag " + itemTagDto.getId() + ": " + itemDto.getId(), "DEBUG");
                    });
                });
                future.get();
            } catch (ExecutionException | InterruptedException e) {
                LoggerManager.getInstance().logException(e);
                throw new RuntimeException(e);
            }
        });
        LoggerManager.getInstance().log("Total items fetched: " + items.size(), "DEBUG");
        return items;
    }

    private List<ItemDetailDto> fetchItemDetails(List<ItemDto> itemDtos) throws ExecutionException, InterruptedException {
        LoggerManager.getInstance().log("Fetching item details...", "DEBUG");
        List<ItemDetailDto> details = new ArrayList<>();
        itemDtos.forEach(itemDto -> {
            try {
                Future<?> future = executorService.submit(() -> {
                    ItemDetailEntity detailEntity = appDatabase.itemDetailEntityDao().getItemDetailByItemId(itemDto.getId());
                    ItemDetailDto detailDto = EntityDtoMapper.detailEntityToDto(detailEntity);
                    details.add(detailDto);
                    LoggerManager.getInstance().log("Detail fetched for item " + itemDto.getId(), "DEBUG");
                });
                future.get();
            } catch (ExecutionException | InterruptedException e) {
                LoggerManager.getInstance().logException(e);
                throw new RuntimeException(e);
            }
        });
        LoggerManager.getInstance().log("Total item details fetched: " + details.size(), "DEBUG");
        return details;
    }

    private List<ItemTagDto> fetchItemTagsDtos(UUID itemId) throws ExecutionException, InterruptedException {
        LoggerManager.getInstance().log("Fetching item tags for item: " + itemId, "DEBUG");
        List<ItemTagDto> tags = new ArrayList<>();
        Future<?> future = executorService.submit(() -> {
            java.util.List<ItemTagEntity> tagEntities = appDatabase.itemTagEntityDao().getItemTagsForItem(itemId);
            tagEntities.forEach(entity -> {
                ItemTagDto dto = EntityDtoMapper.entityToDto(entity);
                tags.add(dto);
                LoggerManager.getInstance().log("Tag fetched: " + dto.getName(), "DEBUG");
            });
        });
        future.get();
        return tags;
    }

    private List<ItemTagDto> fetchAllItemTagsDtos() throws ExecutionException, InterruptedException {
        LoggerManager.getInstance().log("Fetching all item tags from database", "DEBUG");
        List<ItemTagDto> tags = new ArrayList<>();
        Future<?> future = executorService.submit(() -> {
            java.util.List<ItemTagEntity> tagEntities = appDatabase.itemTagEntityDao().getItemTags();
            tagEntities.forEach(entity -> {
                ItemTagDto dto = EntityDtoMapper.entityToDto(entity);
                tags.add(dto);
                LoggerManager.getInstance().log("Global tag fetched: " + dto.getName(), "DEBUG");
            });
        });
        future.get();
        return tags;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LoggerManager.getInstance().log("onCreateView started", "INFO");
        View view = inflater.inflate(R.layout.fragment_item_tags, container, false);
        recyclerViewTags = view.findViewById(R.id.recycler_view_tags);
        recyclerViewTags.setAdapter(itemTagsAdapter);
        recyclerViewTags.setLayoutManager(new LinearLayoutManager(getContext()));

        setupMenuToolbar();

        FloatingActionButton fab = view.findViewById(R.id.new_tag_fab);
        fab.setOnClickListener(v -> {
            LoggerManager.getInstance().log("FAB clicked to add new tag", "DEBUG");
            addNewTag();
        });

        prefillFields();

        LoggerManager.getInstance().log("onCreateView completed", "INFO");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        LoggerManager.getInstance().log("onViewCreated started", "INFO");
        super.onViewCreated(view, savedInstanceState);

        generalItemViewModel.getItems().observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                Optional<ItemDto> maybeItem = items.stream()
                        .filter(item -> item.getId().equals(itemId))
                        .findFirst();
                if (maybeItem.isPresent()) {
                    String itemName = maybeItem.get().getName();
                    Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(itemName);
                    LoggerManager.getInstance().log("ActionBar title set to: " + itemName, "DEBUG");
                } else {
                    Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle("Titolo di Default");
                    LoggerManager.getInstance().log("ActionBar title set to default", "DEBUG");
                }
            }
        });

        generalItemViewModel.getItemTags().observe(getViewLifecycleOwner(), updatedTags -> {
            itemTagsAdapter.setItemTagDtos(updatedTags);
            LoggerManager.getInstance().log("ItemTags updated, count: " + (updatedTags != null ? updatedTags.size() : 0), "DEBUG");
        });

        generalItemViewModel.getItems().observe(getViewLifecycleOwner(), updatedItems -> {
            List<ItemDto> filtered = updatedItems.stream()
                    .filter(item -> itemDtos.stream()
                            .anyMatch(dto -> dto.getId().equals(item.getId())))
                    .collect(Collectors.toList());
            itemTagsAdapter.setItemDtos(filtered);
            LoggerManager.getInstance().log("Items updated, filtered count: " + filtered.size(), "DEBUG");
        });

        generalItemViewModel.getItemDetails().observe(getViewLifecycleOwner(), updatedDetails -> {
            List<ItemDetailDto> filteredDetails = updatedDetails.stream()
                    .filter(detail -> itemDtos.stream()
                            .anyMatch(item -> item.getId().equals(detail.getItemId())))
                    .collect(Collectors.toList());
            itemTagsAdapter.setItemDetailDtos(filteredDetails);
            LoggerManager.getInstance().log("Item details updated, filtered count: " + filteredDetails.size(), "DEBUG");
        });

        LoggerManager.getInstance().log("onViewCreated completed", "INFO");
    }

    private void addNewTag() {
        LoggerManager.getInstance().log("addNewTag started", "INFO");
        Context context = getContext();
        if (context == null) {
            LoggerManager.getInstance().log("Context is null in addNewTag", "INFO");
            return;
        }

        AlertDialog.Builder tagNameDialogBuilder = new AlertDialog.Builder(context);
        tagNameDialogBuilder.setTitle("Inserisci nome del tag");

        final EditText tagNameEditText = new EditText(context);
        tagNameEditText.setHint("Tag name");
        tagNameEditText.setPadding(16, 16, 16, 16);
        tagNameEditText.setTextSize(20);
        tagNameDialogBuilder.setView(tagNameEditText);

        tagNameDialogBuilder.setPositiveButton("Avanti", (dialog, which) -> {
            String tagName = tagNameEditText.getText().toString().trim();
            if (tagName.isEmpty()) {
                Toast.makeText(context, "Il nome del tag non può essere vuoto", Toast.LENGTH_SHORT).show();
                LoggerManager.getInstance().log("Tag name empty - abort", "INFO");
                return;
            }
            // Verifica nella lista globale di tag
            List<ItemTagDto> globalTags = generalItemViewModel.getAllItemTags().getValue();
            if (globalTags != null) {
                Optional<ItemTagDto> maybeTag = globalTags.stream()
                        .filter(tag -> tag.getName().equalsIgnoreCase(tagName))
                        .findFirst();
                if (maybeTag.isPresent()) {
                    LoggerManager.getInstance().log("Tag esistente trovato: " + tagName, "DEBUG");
                    new AlertDialog.Builder(context)
                            .setTitle("Tag esistente")
                            .setMessage("Il tag \"" + tagName + "\" esiste già. Vuoi usarlo? Verrà associato questo elemento se non già presente.")
                            .setPositiveButton("Sì", (d, w) -> {
                                importExistingTag(maybeTag.get());
                            })
                            .setNegativeButton("No, annulla", (d, w) -> {
                                Toast.makeText(context, "Operazione annullata", Toast.LENGTH_SHORT).show();
                                LoggerManager.getInstance().log("Importazione tag annullata", "INFO");
                            })
                            .create().show();
                    return;
                }
            }
            // Se il tag non esiste, procedi con la creazione
            showItemMultiSelectionDialog(tagName);
        });
        tagNameDialogBuilder.setNegativeButton("Annulla", (dialog, which) -> dialog.dismiss());
        tagNameDialogBuilder.create().show();
        LoggerManager.getInstance().log("addNewTag completed", "INFO");
    }

    private void showItemMultiSelectionDialog(String tagName) {
        LoggerManager.getInstance().log("showItemMultiSelectionDialog started for tag: " + tagName, "INFO");
        Context context = getContext();
        if (context == null) {
            LoggerManager.getInstance().log("Context is null in showItemMultiSelectionDialog", "INFO");
            return;
        }

        List<ItemDto> allItems = generalItemViewModel.getItems().getValue();
        if (allItems == null) {
            allItems = new ArrayList<>();
        }

        String[] itemNames = allItems.stream()
                .map(item -> item.getName() != null ? item.getName() : "Item senza nome")
                .toArray(String[]::new);
        boolean[] checkedItems = new boolean[itemNames.length];

        AlertDialog.Builder multiSelectDialogBuilder = new AlertDialog.Builder(context);
        multiSelectDialogBuilder.setTitle("Seleziona gli item per il tag");
        multiSelectDialogBuilder.setMultiChoiceItems(itemNames, checkedItems, (dialog, which, isChecked) -> {
            checkedItems[which] = isChecked;
        });

        List<ItemDto> finalAllItems = allItems;
        multiSelectDialogBuilder.setPositiveButton("Crea Tag", (dialog, which) -> {
            UUID newTagId = UUID.randomUUID();
            final ItemTagDto newTag = new ItemTagDto();
            newTag.setId(newTagId);
            newTag.setName(tagName);

            executorService.execute(() -> {
                // Inserisci il nuovo tag nel database
                appDatabase.itemTagEntityDao().insert(EntityDtoMapper.dtoToEntity(newTag));
                LoggerManager.getInstance().log("Nuovo tag inserito nel DB: " + newTag.getName(), "DEBUG");

                // Raccogli gli item selezionati
                List<ItemDto> selectedItems = new ArrayList<>();
                for (int i = 0; i < checkedItems.length; i++) {
                    if (checkedItems[i]) {
                        selectedItems.add(finalAllItems.get(i));
                    }
                }
                // (Opzionale) Aggiungi anche l'item corrente se non già presente
                if (itemId != null) {
                    boolean containsCurrent = selectedItems.stream()
                            .anyMatch(item -> item.getId().equals(itemId));
                    if (!containsCurrent) {
                        for (ItemDto item : finalAllItems) {
                            if (item.getId().equals(itemId)) {
                                selectedItems.add(item);
                                break;
                            }
                        }
                    }
                }

                // Aggiorna la mappa dei join
                Map<UUID, Set<UUID>> currentJoins = generalItemViewModel.getItemTagJoins();
                if (currentJoins == null) {
                    currentJoins = new HashMap<>();
                }
                Set<UUID> joinSet = currentJoins.get(newTagId);
                if (joinSet == null) {
                    joinSet = new HashSet<>();
                    currentJoins.put(newTagId, joinSet);
                }
                for (ItemDto selectedItem : selectedItems) {
                    if (!joinSet.contains(selectedItem.getId())) {
                        joinSet.add(selectedItem.getId());
                        ItemTagJoinEntity joinEntity = new ItemTagJoinEntity(selectedItem.getId(), newTagId);
                        appDatabase.itemTagJoinDao().insert(joinEntity);
                        LoggerManager.getInstance().log("Join inserito per tag " + newTagId + " e item " + selectedItem.getId(), "DEBUG");
                    }
                }
                Map<UUID, Set<UUID>> finalCurrentJoins = currentJoins;
                requireActivity().runOnUiThread(() -> {
                    // Aggiorna la lista filtrata dei tag (dettaglio corrente)
                    List<ItemTagDto> currentTags = generalItemViewModel.getItemTags().getValue();
                    if (currentTags == null) {
                        currentTags = new ArrayList<>();
                    }
                    currentTags.add(newTag);
                    generalItemViewModel.setItemTags(currentTags);
                    itemTagsAdapter.setItemTagDtos(currentTags);

                    // Aggiorna la lista globale dei tag
                    List<ItemTagDto> globalTags = generalItemViewModel.getAllItemTags().getValue();
                    if (globalTags == null) {
                        globalTags = new ArrayList<>();
                    }
                    globalTags.add(newTag);
                    generalItemViewModel.setAllItemTags(globalTags);

                    generalItemViewModel.setItemTagJoins(finalCurrentJoins);

                    // Aggiorna anche la lista degli item associati
                    List<ItemDto> currentItems = generalItemViewModel.getItems().getValue();
                    if (currentItems == null) {
                        currentItems = new ArrayList<>();
                    }
                    for (ItemDto selectedItem : selectedItems) {
                        boolean exists = currentItems.stream().anyMatch(item -> item.getId().equals(selectedItem.getId()));
                        if (!exists) {
                            currentItems.add(selectedItem);
                        }
                    }
                    generalItemViewModel.setItems(currentItems);
                    itemTagsAdapter.setItemDtos(currentItems);

                    itemTagsAdapter.notifyDataSetChanged();
                    Toast.makeText(context, "Tag creato correttamente", Toast.LENGTH_SHORT).show();
                    LoggerManager.getInstance().log("Tag creato correttamente con " + selectedItems.size() + " items associati", "INFO");
                });
            });
        });
        multiSelectDialogBuilder.setNegativeButton("Annulla", (dialog, which) -> dialog.dismiss());
        multiSelectDialogBuilder.create().show();
        LoggerManager.getInstance().log("showItemMultiSelectionDialog completed", "INFO");
    }

    private void importExistingTag(ItemTagDto existingTag) {
        LoggerManager.getInstance().log("importExistingTag started for tag: " + existingTag.getName(), "INFO");
        Context context = getContext();
        if (context == null) {
            LoggerManager.getInstance().log("Context is null in importExistingTag", "INFO");
            return;
        }

        executorService.execute(() -> {

            List<UUID> globalItemIds = appDatabase.itemTagJoinDao().getItemIdsForTag(existingTag.getId());
            Set<UUID> globalJoinSet = new HashSet<>(globalItemIds);
            LoggerManager.getInstance().log("Global join set size for tag " + existingTag.getId() + ": " + globalJoinSet.size(), "DEBUG");

            if (itemId != null && !globalJoinSet.contains(itemId)) {
                ItemTagJoinEntity joinEntity = new ItemTagJoinEntity(itemId, existingTag.getId());
                appDatabase.itemTagJoinDao().insert(joinEntity);
                globalJoinSet.add(itemId);
                LoggerManager.getInstance().log("Item corrente associato al tag nel DB: " + itemId, "DEBUG");
            }

            Map<UUID, Set<UUID>> currentJoins = generalItemViewModel.getItemTagJoins();
            if (currentJoins == null) {
                currentJoins = new HashMap<>();
            }
            currentJoins.put(existingTag.getId(), globalJoinSet);

            List<ItemDto> importedItems = new ArrayList<>();
            for (UUID id : globalJoinSet) {
                ItemEntity entity = appDatabase.itemEntityDao().getItemById(id);
                if (entity != null) {
                    importedItems.add(EntityDtoMapper.entityToDto(entity));
                    LoggerManager.getInstance().log("Imported item for existing tag: " + id, "DEBUG");
                }
            }

            List<ItemDto> currentItems = generalItemViewModel.getItems().getValue();
            if (currentItems == null) {
                currentItems = new ArrayList<>();
            }
            for (ItemDto imported : importedItems) {
                boolean exists = currentItems.stream().anyMatch(item -> item.getId().equals(imported.getId()));
                if (!exists) {
                    currentItems.add(imported);
                }
            }

            Map<UUID, Set<UUID>> finalCurrentJoins = currentJoins;
            List<ItemDto> finalCurrentItems = currentItems;
            requireActivity().runOnUiThread(() -> {

                List<ItemTagDto> currentTags = generalItemViewModel.getItemTags().getValue();
                if (currentTags == null) {
                    currentTags = new ArrayList<>();
                }
                if (currentTags.stream().noneMatch(tag -> tag.getId().equals(existingTag.getId()))) {
                    currentTags.add(existingTag);
                    generalItemViewModel.setItemTags(currentTags);
                    itemTagsAdapter.setItemTagDtos(currentTags);
                    LoggerManager.getInstance().log("Existing tag aggiunto al dettaglio corrente: " + existingTag.getName(), "DEBUG");
                }

                generalItemViewModel.setItems(finalCurrentItems);
                itemTagsAdapter.setItemDtos(finalCurrentItems);
                generalItemViewModel.setItemTagJoins(finalCurrentJoins);
                itemTagsAdapter.notifyDataSetChanged();

                Toast.makeText(context, "Tag importato correttamente", Toast.LENGTH_SHORT).show();
                LoggerManager.getInstance().log("Tag importato correttamente: " + existingTag.getName(), "INFO");
            });
        });
    }


    protected void prefillFields() {
        LoggerManager.getInstance().log("prefillFields started", "INFO");
        if (itemId != null) {
            try {
                List<ItemTagDto> itemTagDtos = fetchItemTagsDtos(itemId);
                generalItemViewModel.setItemTags(itemTagDtos);
                itemTagsAdapter.setItemTagDtos(itemTagDtos);
                recyclerViewTags.setAdapter(itemTagsAdapter);
                LoggerManager.getInstance().log("prefillFields completed, tags count: " + (itemTagDtos != null ? itemTagDtos.size() : 0), "DEBUG");
            } catch (ExecutionException | InterruptedException e) {
                LoggerManager.getInstance().logException(e);
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onSearchQueryChanged(String query) {
        LoggerManager.getInstance().log("Search query changed: " + query, "DEBUG");
        List<ItemTagDto> itemTags = generalItemViewModel.getItemTags().getValue();
        if (itemTags != null) {
            List<ItemTagDto> filteredTags = itemTags.stream()
                    .filter(tag -> tag.getName().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
            itemTagsAdapter.setItemTagDtos(filteredTags);
        }
    }

    private void setupMenuToolbar() {
        LoggerManager.getInstance().log("setupMenuToolbar started", "INFO");
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_item_tags_fragment, menu);

                MenuItem searchItem = menu.findItem(R.id.action_search);
                SearchManager searchManager = (SearchManager) requireActivity().getSystemService(Context.SEARCH_SERVICE);
                SearchView searchView = (SearchView) searchItem.getActionView();
                if (searchManager != null && searchView != null) {
                    searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));
                }

                if (searchView != null) {
                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            return false;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                            Fragment navHostFragment = requireActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
                            if (navHostFragment != null) {
                                Fragment currentFragment = navHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();
                                if (currentFragment instanceof Searchable) {
                                    ((Searchable) currentFragment).onSearchQueryChanged(newText);
                                }
                            }
                            return true;
                        }
                    });
                }
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int menuItemId = menuItem.getItemId();
                if (menuItemId == R.id.delete_tag) {
                    List<ItemTagDto> itemTagDtos = generalItemViewModel.getItemTags().getValue();
                    if (itemTagDtos == null) {
                        return false;
                    }
                    List<ItemTagDto> selectedItems = new ArrayList<>();
                    for (ItemTagDto tag : itemTagDtos) {
                        if (tag.isSelected()) {
                            selectedItems.add(tag);
                        }
                    }
                    if (selectedItems.isEmpty()) {
                        Toast.makeText(getContext(), "Nessun elemento selezionato", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    itemTagDtos.removeIf(ItemTagDto::isSelected);
                    executorService.submit(() -> {
                        for (ItemTagDto tag : selectedItems) {
                            ItemTagEntity entity = EntityDtoMapper.dtoToEntity(tag);
                            appDatabase.itemTagEntityDao().delete(entity);
                            LoggerManager.getInstance().log("Tag eliminato dal DB: " + tag.getName(), "DEBUG");
                        }
                    });
                    List<ItemTagDto> updatedList = new ArrayList<>(itemTagDtos);
                    generalItemViewModel.setItemTags(updatedList);
                    itemTagsAdapter.setItemTagDtos(updatedList);
                    return true;
                } else if (menuItemId == R.id.filter_tags) {
                    Objects.requireNonNull(generalItemViewModel.getItemTags().getValue())
                            .sort(Comparator.comparing(ItemTagDto::getName, String.CASE_INSENSITIVE_ORDER));
                    itemTagsAdapter.setItemTagDtos(generalItemViewModel.getItemTags().getValue());
                    LoggerManager.getInstance().log("Tags filtrati per nome", "DEBUG");
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
        LoggerManager.getInstance().log("setupMenuToolbar completed", "INFO");
    }

    @Override
    public void onResume() {
        LoggerManager.getInstance().log("onResume started", "INFO");
        super.onResume();
        if (itemTagsAdapter != null) {
            itemTagsAdapter.notifyDataSetChanged();
        }
        LoggerManager.getInstance().log("onResume completed", "INFO");
    }

    @Override
    public void onDestroy() {
        LoggerManager.getInstance().log("onDestroy started", "INFO");
        super.onDestroy();
        executorService.shutdown();
        LoggerManager.getInstance().log("ExecutorService shutdown", "DEBUG");
        LoggerManager.getInstance().log("onDestroy completed", "INFO");
    }

    @Override
    public void onItemClick(ItemDto item) {
        LoggerManager.getInstance().log("onItemClick called for item: " + (item != null ? item.getId() : "null"), "DEBUG");
        if (item == null || item.getId() == null) {
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString("itemId", item.getId().toString());
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_itemTagsFragment_to_itemDetailFragment, bundle);
    }

    @Override
    public void onRemoveItemFromTag(ItemDto item, ItemTagDto tag) throws ExecutionException, InterruptedException {
        LoggerManager.getInstance().log("onRemoveItemFromTag called for item: " + (item != null ? item.getId() : "null") +
                " and tag: " + (tag != null ? tag.getName() : "null"), "DEBUG");
        if (item == null || tag == null) {
            return;
        }
        Set<UUID> itemsId = itemTagJoins.get(tag.getId());
        if (itemsId != null) {
            itemsId.remove(item.getId());
            Future<?> submit = executorService.submit(() -> appDatabase.itemTagJoinDao().delete(tag.getId(), item.getId()));
            submit.get();
        }
        requireActivity().runOnUiThread(() -> {
            itemTagsAdapter.notifyDataSetChanged();
            LoggerManager.getInstance().log("onRemoveItemFromTag UI updated", "DEBUG");
        });
    }

    @Override
    public void onAddItemsToTag(ItemTagDto tag) {
        LoggerManager.getInstance().log("onAddItemsToTag started for tag: " + tag.getName(), "INFO");
        Context context = getContext();
        if (context == null) {
            LoggerManager.getInstance().log("Context is null in onAddItemsToTag", "INFO");
            return;
        }
        List<ItemDto> allItems = generalItemViewModel.getItems().getValue();
        if (allItems == null || allItems.isEmpty()) {
            Toast.makeText(context, "Nessun item disponibile", Toast.LENGTH_SHORT).show();
            LoggerManager.getInstance().log("Nessun item disponibile", "INFO");
            return;
        }
        Set<UUID> associatedItemIds = itemTagJoins.get(tag.getId());
        List<ItemDto> availableItems = allItems.stream()
                .filter(item -> associatedItemIds == null || !associatedItemIds.contains(item.getId()))
                .collect(Collectors.toList());
        if (availableItems.isEmpty()) {
            Toast.makeText(context, "Non ci sono nuovi item da associare a questo tag", Toast.LENGTH_SHORT).show();
            LoggerManager.getInstance().log("Non ci sono nuovi item da associare al tag", "INFO");
            return;
        }

        String[] itemNames = availableItems.stream()
                .map(item -> item.getName() != null ? item.getName() : "Item senza nome")
                .toArray(String[]::new);
        boolean[] checkedItems = new boolean[itemNames.length];

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Seleziona uno o più item da aggiungere al tag")
                .setMultiChoiceItems(itemNames, checkedItems, (dialog, which, isChecked) -> {
                    checkedItems[which] = isChecked;
                })
                .setPositiveButton("Aggiungi", (dialog, which) -> {
                    List<ItemDto> selectedItems = new ArrayList<>();
                    for (int i = 0; i < checkedItems.length; i++) {
                        if (checkedItems[i]) {
                            selectedItems.add(availableItems.get(i));
                        }
                    }
                    if (selectedItems.isEmpty()) {
                        Toast.makeText(context, "Nessun item selezionato", Toast.LENGTH_SHORT).show();
                        LoggerManager.getInstance().log("Nessun item selezionato in onAddItemsToTag", "INFO");
                        return;
                    }
                    executorService.execute(() -> {
                        for (ItemDto selectedItem : selectedItems) {
                            ItemTagJoinEntity joinEntity = new ItemTagJoinEntity(selectedItem.getId(), tag.getId());
                            appDatabase.itemTagJoinDao().insert(joinEntity);
                            Set<UUID> set = itemTagJoins.get(tag.getId());
                            if (set == null) {
                                set = new HashSet<>();
                                itemTagJoins.put(tag.getId(), set);
                            }
                            set.add(selectedItem.getId());
                            if (!itemDtos.contains(selectedItem)) {
                                itemDtos.add(selectedItem);
                            }
                            LoggerManager.getInstance().log("Item aggiunto al tag: " + selectedItem.getId(), "DEBUG");
                        }
                        requireActivity().runOnUiThread(() -> {
                            itemDtos.sort((a, b) -> {
                                String nameA = a.getName() != null ? a.getName() : "";
                                String nameB = b.getName() != null ? b.getName() : "";
                                return nameA.compareToIgnoreCase(nameB);
                            });
                            generalItemViewModel.setItems(itemDtos);
                            Toast.makeText(context, "Item aggiunti al tag", Toast.LENGTH_SHORT).show();
                            itemTagsAdapter.notifyDataSetChanged();
                            LoggerManager.getInstance().log("onAddItemsToTag completed, items count: " + itemDtos.size(), "INFO");
                        });
                    });
                })
                .setNegativeButton("Annulla", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
