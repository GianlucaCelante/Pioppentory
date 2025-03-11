package it.pioppi.business.fragment;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
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
import it.pioppi.business.dto.ItemDetailDto;
import it.pioppi.business.dto.ItemDto;
import it.pioppi.business.dto.ItemTagDto;
import it.pioppi.business.viewmodel.GeneralItemViewModel;
import it.pioppi.database.AppDatabase;
import it.pioppi.database.entity.ItemEntity;
import it.pioppi.database.mapper.EntityDtoMapper;
import it.pioppi.database.entity.ItemDetailEntity;
import it.pioppi.database.entity.ItemTagEntity;
import it.pioppi.database.entity.ItemTagJoinEntity;

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
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appDatabase = AppDatabase.getInstance(getContext());
        executorService = Executors.newSingleThreadExecutor();
        generalItemViewModel = new ViewModelProvider(requireActivity()).get(GeneralItemViewModel.class);

        Bundle bundle = getArguments();
        if (bundle != null) {
            itemId = UUID.fromString(bundle.getString("itemId"));
        }

        try {

            List<ItemTagDto> itemTagDtos = fetchItemTagsDtos(itemId);
            // Salva la lista filtrata nel ViewModel per il dettaglio corrente
            generalItemViewModel.setItemTags(itemTagDtos);

            // Carica anche la lista globale di tag dal database
            List<ItemTagDto> allTags = fetchAllItemTagsDtos();
            generalItemViewModel.setAllItemTags(allTags);

            itemDtos = fetchItemsForTags(itemTagDtos);
            itemDetailDtos = fetchItemDetails(itemDtos);
            itemTagJoins = generalItemViewModel.getItemTagJoins();

            itemTagsAdapter = new ItemTagsAdapter(itemTagDtos, itemDtos, itemDetailDtos, itemTagJoins,
                    this, this, getContext());
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private List<ItemDto> fetchItemsForTags(List<ItemTagDto> itemTagDtos) {

        List<ItemDto> itemDtos = new ArrayList<>();
        itemTagDtos.forEach(itemTagDto -> {
            try {
                Future<?> future = executorService.submit(() -> {

                    List<ItemEntity> itemsForTag = appDatabase.itemTagEntityDao().getItemsForTag(itemTagDto.getId());
                    itemsForTag.forEach(itemEntity -> {
                        ItemDto itemDto = EntityDtoMapper.entityToDto(itemEntity);
                        itemDtos.add(itemDto);
                    });
                });
                future.get();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        return itemDtos;
    }

    private List<ItemDetailDto> fetchItemDetails(List<ItemDto> itemDtos) throws ExecutionException, InterruptedException {

        List<ItemDetailDto> itemDetailDtos = new ArrayList<>();

        itemDtos.forEach(itemDto -> {
            try {
                Future<?> future = executorService.submit(() -> {

                    ItemDetailEntity itemDetailsForItem = appDatabase.itemDetailEntityDao().getItemDetailByItemId(itemDto.getId());
                    ItemDetailDto itemDetailDto = EntityDtoMapper.detailEntityToDto(itemDetailsForItem);
                    itemDetailDtos.add(itemDetailDto);

                });
                future.get();


            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        return itemDetailDtos;
    }

    private List<ItemTagDto> fetchItemTagsDtos(UUID itemId) throws ExecutionException, InterruptedException {
        List<ItemTagDto> itemDtos = new ArrayList<>();
        Future<?> future = executorService.submit(() -> {
            List<ItemTagEntity> itemTagsForItem = appDatabase.itemTagEntityDao().getItemTagsForItem(itemId);
            itemTagsForItem.forEach(itemEntity -> {
                ItemTagDto itemDto = EntityDtoMapper.entityToDto(itemEntity);
                itemDtos.add(itemDto);
            });
        });
        future.get();
        return itemDtos;
    }

    private List<ItemTagDto> fetchAllItemTagsDtos() throws ExecutionException, InterruptedException {
        List<ItemTagDto> itemDtos = new ArrayList<>();
        Future<?> future = executorService.submit(() -> {
            List<ItemTagEntity> itemTagsForItem = appDatabase.itemTagEntityDao().getItemTags();
            itemTagsForItem.forEach(itemEntity -> {
                ItemTagDto itemDto = EntityDtoMapper.entityToDto(itemEntity);
                itemDtos.add(itemDto);
            });
        });
        future.get();
        return itemDtos;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_tags, container, false);
        recyclerViewTags = view.findViewById(R.id.recycler_view_tags);
        recyclerViewTags.setAdapter(itemTagsAdapter);
        recyclerViewTags.setLayoutManager(new LinearLayoutManager(getContext()));

        setupMenuToolbar();

        FloatingActionButton fab = view.findViewById(R.id.new_tag_fab);
        fab.setOnClickListener(v -> addNewTag());

        prefillFields();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        generalItemViewModel.getItemTags().observe(getViewLifecycleOwner(), updatedTags -> {
            itemTagsAdapter.setItemTagDtos(updatedTags);
        });

        generalItemViewModel.getItems().observe(getViewLifecycleOwner(), updatedItems -> {
            List<ItemDto> filtered = updatedItems.stream()
                    .filter(item -> itemDtos.stream()
                            .anyMatch(dto -> dto.getId().equals(item.getId())))
                    .collect(Collectors.toList());
            itemTagsAdapter.setItemDtos(filtered);
        });

        generalItemViewModel.getItemDetails().observe(getViewLifecycleOwner(), updatedDetails -> {
            List<ItemDetailDto> filteredDetails = updatedDetails.stream()
                    .filter(detail -> itemDtos.stream()
                            .anyMatch(item -> item.getId().equals(detail.getItemId())))
                    .collect(Collectors.toList());
            itemTagsAdapter.setItemDetailDtos(filteredDetails);
        });

    }

    private void addNewTag() {
        Context context = getContext();
        if (context == null) return;

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
                return;
            }
            // Utilizza la lista globale di tag per il controllo
            List<ItemTagDto> globalTags = generalItemViewModel.getAllItemTags().getValue();
            if (globalTags != null) {
                Optional<ItemTagDto> maybeTag = globalTags.stream()
                        .filter(tag -> tag.getName().equalsIgnoreCase(tagName))
                        .findFirst();
                if (maybeTag.isPresent()) {
                    new AlertDialog.Builder(context)
                            .setTitle("Tag esistente")
                            .setMessage("Il tag \"" + tagName + "\" esiste già. Vuoi usarlo? Verrà associato questo elemento se non già presente.")
                            .setPositiveButton("Sì", (d, w) -> {
                                importExistingTag(maybeTag.get());
                            })
                            .setNegativeButton("No, annulla", (d, w) -> {
                                Toast.makeText(context, "Operazione annullata", Toast.LENGTH_SHORT).show();
                            })
                            .create().show();
                    return;
                }
            }
            // Se il tag non esiste, procedi con la creazione di un nuovo tag
            showItemMultiSelectionDialog(tagName);
        });
        tagNameDialogBuilder.setNegativeButton("Annulla", (dialog, which) -> dialog.dismiss());
        tagNameDialogBuilder.create().show();
    }

    private void showItemMultiSelectionDialog(String tagName) {
        Context context = getContext();
        if (context == null) return;

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

                // Raccogli gli item selezionati dal dialog
                List<ItemDto> selectedItems = new ArrayList<>();
                for (int i = 0; i < checkedItems.length; i++) {
                    if (checkedItems[i]) {
                        selectedItems.add(finalAllItems.get(i));
                    }
                }

                // (Opzionale) Aggiungi anche l'item corrente se desiderato
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
                    // Inserisci solo se l'associazione non esiste già
                    if (!joinSet.contains(selectedItem.getId())) {
                        joinSet.add(selectedItem.getId());
                        ItemTagJoinEntity joinEntity = new ItemTagJoinEntity(selectedItem.getId(), newTagId);
                        appDatabase.itemTagJoinDao().insert(joinEntity);
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

                    // Aggiorna anche la lista globale dei tag
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
                        boolean exists = false;
                        for (ItemDto item : currentItems) {
                            if (item.getId().equals(selectedItem.getId())) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            currentItems.add(selectedItem);
                        }
                    }
                    generalItemViewModel.setItems(currentItems);
                    itemTagsAdapter.setItemDtos(currentItems);

                    itemTagsAdapter.notifyDataSetChanged();

                    Toast.makeText(context, "Tag creato correttamente", Toast.LENGTH_SHORT).show();
                });
            });
        });
        multiSelectDialogBuilder.setNegativeButton("Annulla", (dialog, which) -> dialog.dismiss());
        multiSelectDialogBuilder.create().show();
    }

    private void importExistingTag(ItemTagDto existingTag) {
        Context context = getContext();
        if (context == null) return;

        executorService.execute(() -> {
            // Recupera dal database gli ID globali degli item associati al tag esistente
            List<UUID> globalItemIds = appDatabase.itemTagJoinDao().getItemIdsForTag(existingTag.getId());
            Set<UUID> globalJoinSet = new HashSet<>(globalItemIds);

            // Aggiorna la mappa dei join nel ViewModel con i dati globali per il tag
            Map<UUID, Set<UUID>> currentJoins = generalItemViewModel.getItemTagJoins();
            if (currentJoins == null) {
                currentJoins = new HashMap<>();
            }
            currentJoins.put(existingTag.getId(), globalJoinSet);

            // Recupera gli ItemDto relativi agli ID ottenuti dal database
            List<ItemDto> importedItems = new ArrayList<>();
            for (UUID id : globalJoinSet) {
                // Assumiamo che il DAO degli item abbia un metodo getItemById(id)
                ItemEntity entity = appDatabase.itemEntityDao().getItemById(id);
                if (entity != null) {
                    importedItems.add(EntityDtoMapper.entityToDto(entity));
                }
            }

            // Aggiorna la lista degli item relativi al dettaglio corrente unendo quelli già presenti
            List<ItemDto> currentItems = generalItemViewModel.getItems().getValue();
            if (currentItems == null) {
                currentItems = new ArrayList<>();
            }
            for (ItemDto imported : importedItems) {
                boolean exists = false;
                for (ItemDto item : currentItems) {
                    if (item.getId().equals(imported.getId())) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    currentItems.add(imported);
                }
            }

            Map<UUID, Set<UUID>> finalCurrentJoins = currentJoins;
            List<ItemDto> finalCurrentItems = currentItems;
            requireActivity().runOnUiThread(() -> {
                // Se il tag non è già presente nel dettaglio corrente, aggiungilo
                List<ItemTagDto> currentTags = generalItemViewModel.getItemTags().getValue();
                if (currentTags == null) {
                    currentTags = new ArrayList<>();
                }
                if (currentTags.stream().noneMatch(tag -> tag.getId().equals(existingTag.getId()))) {
                    currentTags.add(existingTag);
                    generalItemViewModel.setItemTags(currentTags);
                    itemTagsAdapter.setItemTagDtos(currentTags);
                }
                // Aggiorna anche la lista degli item e la mappa dei join nel ViewModel
                generalItemViewModel.setItems(finalCurrentItems);
                itemTagsAdapter.setItemDtos(finalCurrentItems);
                generalItemViewModel.setItemTagJoins(finalCurrentJoins);

                Toast.makeText(context, "Tag importato correttamente", Toast.LENGTH_SHORT).show();
            });
        });
    }



    protected void prefillFields() {
        if (itemId != null) {
            try {
                List<ItemTagDto> itemTagDtos = fetchItemTagsDtos(itemId);
                generalItemViewModel.setItemTags(itemTagDtos);
                itemTagsAdapter.setItemTagDtos(itemTagDtos);
                recyclerViewTags.setAdapter(itemTagsAdapter);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onSearchQueryChanged(String query) {
        List<ItemTagDto> itemTags = generalItemViewModel.getItemTags().getValue();
        if (itemTags != null) {
            List<ItemTagDto> filteredTags = itemTags.stream()
                    .filter(tag -> tag.getName().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
            itemTagsAdapter.setItemTagDtos(filteredTags);
        }
    }

    private void setupMenuToolbar() {
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_item_tags_fragment, menu);

                MenuItem searchItem = menu.findItem(R.id.action_search);
                SearchManager searchManager = (SearchManager) requireActivity().getSystemService(Context.SEARCH_SERVICE);
                SearchView searchView = (SearchView) searchItem.getActionView();
                if (searchManager != null) {
                    if (searchView != null) {
                        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));
                    }
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
                int itemId = menuItem.getItemId();
                if (itemId == R.id.delete_tag) {
                    List<ItemTagDto> itemTagDtos = generalItemViewModel.getItemTags().getValue();
                    if (itemTagDtos == null) {
                        return false;
                    }

                    List<ItemTagDto> selectedItems = new ArrayList<>();
                    for (ItemTagDto itemTagDto : itemTagDtos) {
                        if (itemTagDto.isSelected()) {
                            selectedItems.add(itemTagDto);
                        }
                    }
                    if (selectedItems.isEmpty()) {
                        Toast.makeText(getContext(), "Nessun elemento selezionato", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    itemTagDtos.removeIf(ItemTagDto::isSelected);

                    executorService.submit(() -> {
                        for (ItemTagDto itemTagDto : selectedItems) {
                            ItemTagEntity itemTagEntity = EntityDtoMapper.dtoToEntity(itemTagDto);
                            appDatabase.itemTagEntityDao().delete(itemTagEntity);
                        }
                    });

                    List<ItemTagDto> updatedList = new ArrayList<>(itemTagDtos);
                    generalItemViewModel.setItemTags(updatedList);
                    itemTagsAdapter.setItemTagDtos(updatedList);
                    return true;
                } else if (itemId == R.id.filter_tags) {
                    Objects.requireNonNull(generalItemViewModel.getItemTags().getValue()).sort(Comparator.comparing(ItemTagDto::getName));
                    itemTagsAdapter.setItemTagDtos(generalItemViewModel.getItemTags().getValue());
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (itemTagsAdapter != null) {
            itemTagsAdapter.notifyDataSetChanged();
        }
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

        Bundle bundle = new Bundle();
        bundle.putString("itemId", item.getId().toString());
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_itemTagsFragment_to_itemDetailFragment, bundle);

    }

    @Override
    public void onRemoveItemFromTag(ItemDto item, ItemTagDto tag) throws ExecutionException, InterruptedException {
        if (item == null || tag == null) {
            return;
        }

        Set<UUID> itemsId = itemTagJoins.get(tag.getId());
        if (itemsId != null) {

            itemsId.remove(item.getId());
            Future<?> submit = executorService.submit(() -> appDatabase.itemTagJoinDao()
                    .delete(tag.getId(), item.getId()));
            submit.get();
        }
        requireActivity().runOnUiThread(() -> {
            itemTagsAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onAddItemsToTag(ItemTagDto tag) {
        Context context = getContext();
        if (context == null) {
            return;
        }
        List<ItemDto> allItems = generalItemViewModel.getItems().getValue();
        if (allItems == null || allItems.isEmpty()) {
            Toast.makeText(context, "Nessun item disponibile", Toast.LENGTH_SHORT).show();
            return;
        }

        Set<UUID> associatedItemIds = itemTagJoins.get(tag.getId());
        List<ItemDto> availableItems = allItems.stream()
                .filter(item -> associatedItemIds == null || !associatedItemIds.contains(item.getId()))
                .collect(Collectors.toList());
        if (availableItems.isEmpty()) {
            Toast.makeText(context, "Non ci sono nuovi item da associare a questo tag", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] itemNames = availableItems.stream()
                .map(item -> item.getName() != null ? item.getName() : "Item senza nome")
                .toArray(String[]::new);
        boolean[] checkedItems = new boolean[itemNames.length]; // inizialmente tutti false

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
                        return;
                    }
                    executorService.execute(() -> {
                        for (ItemDto selectedItem : selectedItems) {

                            ItemTagJoinEntity joinEntity = new ItemTagJoinEntity(selectedItem.getId(), tag.getId());
                            appDatabase.itemTagJoinDao().insert(joinEntity);


                            Set<UUID> set = itemTagJoins.get(tag.getId());
                            if (set == null) {
                                set = new java.util.HashSet<>();
                                itemTagJoins.put(tag.getId(), set);
                            }
                            set.add(selectedItem.getId());


                            if (!itemDtos.contains(selectedItem)) {
                                itemDtos.add(selectedItem);
                            }
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
                        });
                    });
                })
                .setNegativeButton("Annulla", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }


}
