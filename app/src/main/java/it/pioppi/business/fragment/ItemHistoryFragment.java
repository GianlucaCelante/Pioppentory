package it.pioppi.business.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import it.pioppi.R;
import it.pioppi.business.adapter.ItemHistoryGroupAdapter;
import it.pioppi.business.manager.GoogleDriveManager;
import it.pioppi.business.viewmodel.ItemHistoryViewModel;

public class ItemHistoryFragment extends Fragment {

    private RecyclerView recyclerViewItemHistory;
    private ItemHistoryGroupAdapter groupAdapter;
    private ItemHistoryViewModel viewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ottieni l'istanza del ViewModel
        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(ItemHistoryViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_item_history, container, false);
        recyclerViewItemHistory = view.findViewById(R.id.recycler_view_item_history);
        recyclerViewItemHistory.setLayoutManager(new LinearLayoutManager(getContext()));

        // Osserva i dati dal ViewModel e configura l'adapter
        viewModel.getItemHistoryGroups().observe(getViewLifecycleOwner(), groupList -> {
            // Recupera il GoogleDriveManager dalla MainActivity
            GoogleDriveManager driveManager = ((it.pioppi.business.activity.MainActivity) requireActivity()).getGoogleDriveManager();
            groupAdapter = new ItemHistoryGroupAdapter(groupList, driveManager);
            recyclerViewItemHistory.setAdapter(groupAdapter);
        });

        return view;
    }
}
