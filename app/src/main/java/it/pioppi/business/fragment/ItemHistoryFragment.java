package it.pioppi.business.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.pioppi.R;
import it.pioppi.business.adapter.ItemHistoryGroupAdapter;
import it.pioppi.business.viewmodel.ItemHistoryViewModel;
import it.pioppi.business.dto.ItemHistoryGroup;

public class ItemHistoryFragment extends Fragment implements Searchable {

    private RecyclerView recyclerViewItemHistory;
    private ItemHistoryGroupAdapter groupAdapter;
    private ItemHistoryViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ottieni l'istanza del ViewModel
        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())).get(ItemHistoryViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_item_history, container, false);
        recyclerViewItemHistory = view.findViewById(R.id.recycler_view_item_history);
        recyclerViewItemHistory.setLayoutManager(new LinearLayoutManager(getContext()));

        // Osserva i dati dal ViewModel
        viewModel.getItemHistoryGroups().observe(getViewLifecycleOwner(), groupList -> {
            groupAdapter = new ItemHistoryGroupAdapter(groupList);
            recyclerViewItemHistory.setAdapter(groupAdapter);
        });

        return view;
    }

    @Override
    public void onSearchQueryChanged(String query) {
        // Implementa la logica di ricerca se necessario
    }
}
