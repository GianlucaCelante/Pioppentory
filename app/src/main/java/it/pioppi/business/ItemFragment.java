package it.pioppi.business;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.pioppi.R;
import it.pioppi.business.adapter.ItemAdapter;
import it.pioppi.business.dto.ItemDto;
import it.pioppi.database.AppDatabase;
import it.pioppi.database.dao.ItemEntityDao;
import it.pioppi.database.mapper.EntityDtoMapper;
import it.pioppi.database.model.entity.ItemEntity;

public class ItemFragment extends Fragment implements ItemAdapter.OnItemClickListener{

    private AppDatabase appDatabase;
    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private ExecutorService executorService;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appDatabase = AppDatabase.getInstance(getContext());
        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getContext(), 4);
        recyclerView.setLayoutManager(mLayoutManager);

        loadItems();

        return view;
    }

    private void loadItems() {
        executorService.execute(() -> {
            ItemEntityDao entityDao = appDatabase.itemEntityDao();
            List<ItemEntity> allItems = entityDao.getAllItems();
            if (allItems == null) {
                allItems = new ArrayList<>();
            }
            final List<ItemDto> itemDtos = EntityDtoMapper.entityToDto(allItems);

            requireActivity().runOnUiThread(() -> {
                itemAdapter = new ItemAdapter(itemDtos, ItemFragment.this);
                recyclerView.setAdapter(itemAdapter);
            });
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    @Override
    public void onItemClick(ItemDto item) {

        if(item == null || item.getId() == null) {
            return;
        }
        NavController navController = NavHostFragment.findNavController(this);
        Bundle bundle = new Bundle();
        bundle.putString("itemId", item.getId().toString());
        navController.navigate(R.id.action_itemFragment_to_itemDetailFragment, bundle);

    }
}