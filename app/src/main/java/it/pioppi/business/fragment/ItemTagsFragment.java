package it.pioppi.business.fragment;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import it.pioppi.R;
import it.pioppi.business.adapter.ItemTagsAdapter;
import it.pioppi.business.dto.ItemDto;
import it.pioppi.business.dto.ItemTagDto;
import it.pioppi.business.viewmodel.ItemTagsViewModel;
import it.pioppi.business.viewmodel.ItemViewModel;
import it.pioppi.database.AppDatabase;
import it.pioppi.database.mapper.EntityDtoMapper;
import it.pioppi.database.model.entity.ItemEntity;
import it.pioppi.database.model.entity.ItemDetailEntity;
import it.pioppi.database.model.entity.ItemTagEntity;
import it.pioppi.database.model.entity.ItemTagJoinEntity;
import it.pioppi.database.model.entity.ProviderEntity;
import it.pioppi.database.model.entity.QuantityTypeEntity;
import it.pioppi.database.model.entity.ItemStatus;
import it.pioppi.database.repository.ItemEntityRepository;

public class ItemTagsFragment extends Fragment implements ItemTagsAdapter.OnItemClickListener, Searchable {
    private AppDatabase appDatabase;
    private ItemTagsAdapter itemTagsAdapter;
    private ExecutorService executorService;
    private ItemTagsViewModel itemTagsViewModel;
    private RecyclerView recyclerView;
    private UUID itemId;
    private ItemEntityRepository itemEntityRepository;

    public ItemTagsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appDatabase = AppDatabase.getInstance(getContext());
        executorService = Executors.newSingleThreadExecutor();
        itemEntityRepository = new ItemEntityRepository(requireActivity().getApplication());
        itemTagsViewModel = new ViewModelProvider(requireActivity()).get(ItemTagsViewModel.class);

        Bundle bundle = getArguments();
        if (bundle != null) {
            itemId = UUID.fromString(bundle.getString("itemId"));
        }

        try {
            List<ItemTagDto> itemDtoList = fetchItemTags(itemId);
            itemTagsViewModel.setItemTags(itemDtoList);
            itemTagsAdapter = new ItemTagsAdapter(itemTagsViewModel.getItemTags().getValue(), this, getContext());
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private List<ItemTagDto> fetchItemTags(UUID itemId) throws ExecutionException, InterruptedException {
        List<ItemTagDto> itemTagsDtos = new ArrayList<>();
        Future<?> future = executorService.submit(() -> {
            List<ItemTagEntity> itemTagsForItem = appDatabase.itemTagEntityDao().getItemTagsForItem(itemId);
            itemTagsForItem.forEach(itemEntity -> {
                ItemTagDto itemTagDto = EntityDtoMapper.entityToDto(itemEntity);
                itemTagsDtos.add(itemTagDto);
            });
        });
        future.get();
        return itemTagsDtos;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_tags, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_tags);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        setupMenuToolbar();

        itemTagsViewModel.getItemTags().observe(getViewLifecycleOwner(), itemList -> {
            itemTagsAdapter.setItemTagDtos(itemList);
            recyclerView.setAdapter(itemTagsAdapter);
        });

        FloatingActionButton fab = view.findViewById(R.id.new_tag_fab);
        fab.setOnClickListener(v -> addNewTag(inflater, container));

        prefillFields(view);

        return view;
    }

    private void addNewTag(LayoutInflater inflater, ViewGroup container) {
        View dialogView = inflater.inflate(R.layout.new_tag_alert, container, false);
        EditText newTagEditText = dialogView.findViewById(R.id.new_tag_name);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView)
                .setPositiveButton("Aggiungi", null)
                .setNegativeButton("Indietro", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dlg -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String newTagName = newTagEditText.getText().toString().trim();
            if (newTagName.isEmpty()) {
                Toast.makeText(getContext(), "Inserisci un nome per il nuovo tag", Toast.LENGTH_SHORT).show();
                return;
            }

            List<ItemTagDto> itemTagDtos = itemTagsViewModel.getItemTags().getValue();
            if (itemTagDtos == null) {
                itemTagDtos = new ArrayList<>();
            }

            boolean isUnique = itemTagDtos.stream().noneMatch(tag -> tag.getName().equalsIgnoreCase(newTagName));
            if (!isUnique) {
                Toast.makeText(getContext(), "Esiste già un tag con questo nome", Toast.LENGTH_SHORT).show();
                return;
            }

            ItemTagDto newItemTag = new ItemTagDto();
            newItemTag.setId(UUID.randomUUID());
            newItemTag.setName(newTagName);
            newItemTag.setCreationDate(LocalDateTime.now());

            itemTagDtos.add(newItemTag);
            List<ItemTagDto> updatedList = new ArrayList<>(itemTagDtos);
            itemTagsViewModel.setItemTags(updatedList);
            itemTagsAdapter.setItemTagDtos(updatedList);

            executorService.execute(() -> {

                UUID itemByNameId = appDatabase.itemEntityDao().getItemByName(newItemTag.getName());
                if(itemByNameId == null) {
                    addNewItem(newItemTag.getName());
                    requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Prodotto aggiunto", Toast.LENGTH_SHORT).show());
                }

                ItemTagEntity newItemTagEntity = EntityDtoMapper.dtoToEntity(newItemTag);
                appDatabase.itemTagEntityDao().insert(newItemTagEntity);
                ItemTagJoinEntity joinEntity = new ItemTagJoinEntity(itemId, newItemTag.getId());
                appDatabase.itemTagEntityDao().insertItemTagJoin(joinEntity);

                requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Ingrediente aggiunto", Toast.LENGTH_SHORT).show());
            });
            dialog.dismiss();
        }));
        dialog.show();
    }

    protected void prefillFields(View view) {
        if (itemId != null) {
            try {
                List<ItemTagDto> itemTagDtos = fetchItemTags(itemId);
                itemTagsViewModel.setItemTags(itemTagDtos);
                itemTagsAdapter.setItemTagDtos(itemTagDtos);
                recyclerView.setAdapter(itemTagsAdapter);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onSearchQueryChanged(String query) {
        List<ItemTagDto> itemTags = itemTagsViewModel.getItemTags().getValue();
        if (itemTags != null) {
            List<ItemTagDto> filteredTags = itemTags.stream()
                    .filter(tag -> tag.getName().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
            itemTagsAdapter.setItemTagDtos(filteredTags);
        }
    }

    @Override
    public void onItemClick(ItemTagDto item) throws ExecutionException, InterruptedException {
        if (item == null || item.getId() == null) {
            return;
        }

        AtomicReference<UUID> itemByNameId = new AtomicReference<>();
        Future<?> future = executorService.submit(() -> itemByNameId.set(appDatabase.itemEntityDao().getItemByName(item.getName())));
        future.get();

        if(itemByNameId.get() == null) {
            Toast.makeText(getContext(), "Prodotto non più disponibile", Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString("itemId", itemByNameId.get().toString());
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_itemTagsFragment_to_itemDetailFragment, bundle);
    }

    private void addNewItem(String tagName) {
        executorService.execute(() -> {

            Integer nextId;
            try {
                nextId = appDatabase.itemFTSEntityDao().getNextId();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            LocalDateTime now = LocalDateTime.now();

            ItemEntity newItem = new ItemEntity();
            UUID itemId = UUID.randomUUID();
            newItem.setId(itemId);
            newItem.setFtsId(nextId);
            newItem.setName(tagName);
            newItem.setTotPortions(0L);
            newItem.setStatus(ItemStatus.WHITE);
            newItem.setCreationDate(now);

            appDatabase.runInTransaction(() -> itemEntityRepository.insert(newItem));

            String providerName = "Default Provider";

            ProviderEntity providerEntity = new ProviderEntity();
            providerEntity.setId(itemId);
            providerEntity.setItemId(newItem.getId());
            providerEntity.setName(providerName);
            providerEntity.setCreationDate(now);

            ItemDetailEntity itemDetailEntity = new ItemDetailEntity();
            itemDetailEntity.setId(itemId);
            itemDetailEntity.setItemId(newItem.getId());
            itemDetailEntity.setCreationDate(now);

            QuantityTypeEntity quantityTypeEntity = new QuantityTypeEntity();
            quantityTypeEntity.setId(itemId);
            quantityTypeEntity.setItemId(newItem.getId());
            quantityTypeEntity.setCreationDate(now);

            appDatabase.runInTransaction(() -> {
                appDatabase.providerEntityDao().insert(providerEntity);
                appDatabase.itemDetailEntityDao().insert(itemDetailEntity);
                appDatabase.quantityTypeEntityDao().insert(quantityTypeEntity);
            });

            requireActivity().runOnUiThread(() -> {
                ItemViewModel itemViewModel = new ViewModelProvider(requireActivity()).get(ItemViewModel.class);
                List<ItemDto> currentItems = itemViewModel.getItems().getValue();
                if (currentItems == null) {
                    currentItems = new ArrayList<>();
                }
                currentItems.add(EntityDtoMapper.entityToDto(newItem));
                itemViewModel.setItems(currentItems);

            });
        });
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
                    searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));
                }

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

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.delete_tag) {
                    List<ItemTagDto> itemTagDtos = itemTagsViewModel.getItemTags().getValue();
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
                    itemTagsViewModel.setItemTags(updatedList);
                    itemTagsAdapter.setItemTagDtos(updatedList);
                    return true;
                } else if (itemId == R.id.filter_tags) {
                    Objects.requireNonNull(itemTagsViewModel.getItemTags().getValue()).sort(Comparator.comparing(ItemTagDto::getName));
                    itemTagsAdapter.setItemTagDtos(itemTagsViewModel.getItemTags().getValue());
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
