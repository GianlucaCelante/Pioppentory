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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_tags, container, false);
        recyclerViewTags = view.findViewById(R.id.recycler_view_tags);
        recyclerViewTags.setAdapter(itemTagsAdapter);
        recyclerViewTags.setLayoutManager(new LinearLayoutManager(getContext()));

        setupMenuToolbar();

        FloatingActionButton fab = view.findViewById(R.id.new_tag_fab);
        fab.setOnClickListener(v -> addNewTag(inflater, container));

        prefillFields(view);

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
                Toast.makeText(getContext(), "Inserisci un nome per il nuovo ingrediente", Toast.LENGTH_SHORT).show();
                return;
            }

            List<ItemTagDto> itemTagDtos = generalItemViewModel.getItemTags().getValue();
            if (itemTagDtos == null) {
                itemTagDtos = new ArrayList<>();
            }

            boolean isUnique = itemTagDtos.stream().noneMatch(tag -> tag.getName().equalsIgnoreCase(newTagName));
            if (!isUnique) {
                Toast.makeText(getContext(), "Esiste già un ingrediente con questo nome", Toast.LENGTH_SHORT).show();
                return;
            }

            ItemTagDto newItemTag = new ItemTagDto();
            newItemTag.setId(UUID.randomUUID());
            newItemTag.setName(newTagName);
            newItemTag.setCreationDate(ZonedDateTime.now(ZoneId.of("Europe/Rome")));

            itemTagDtos.add(newItemTag);
            List<ItemTagDto> updatedList = new ArrayList<>(itemTagDtos);
            generalItemViewModel.setItemTags(updatedList);
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



    private void addNewItem(String tagName) {


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
    public void onAddItemsToTag(ItemTagDto item) throws ExecutionException, InterruptedException {

        //TODO: alert dialog che mostra uno spinner di tutti gli item per associazione item a tag + insert tabella join

    }
}
