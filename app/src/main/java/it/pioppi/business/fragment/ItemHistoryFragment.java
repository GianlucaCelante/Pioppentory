package it.pioppi.business.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import it.pioppi.R;
import it.pioppi.business.adapter.ItemHistoryGroupAdapter;
import it.pioppi.business.manager.GoogleDriveManager;
import it.pioppi.business.viewmodel.ItemHistoryViewModel;
import it.pioppi.utils.LoggerManager;

public class ItemHistoryFragment extends Fragment {

    private RecyclerView recyclerViewItemHistory;
    private ItemHistoryGroupAdapter groupAdapter;
    private ItemHistoryViewModel viewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoggerManager.getInstance().log("onCreate: Inizializzazione di ItemHistoryFragment", "INFO");
        setHasOptionsMenu(true);
        // Ottieni l'istanza del ViewModel
        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(ItemHistoryViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LoggerManager.getInstance().log("onCreateView: Creazione della view di ItemHistoryFragment", "INFO");
        View view = inflater.inflate(R.layout.fragment_item_history, container, false);
        recyclerViewItemHistory = view.findViewById(R.id.recycler_view_item_history);
        recyclerViewItemHistory.setLayoutManager(new LinearLayoutManager(getContext()));

        // Osserva i dati dal ViewModel e configura l'adapter
        viewModel.getItemHistoryGroups().observe(getViewLifecycleOwner(), groupList -> {
            LoggerManager.getInstance().log("onCreateView: Osservati " + groupList.size() + " gruppi di item history", "DEBUG");
            // Recupera il GoogleDriveManager dalla MainActivity
            GoogleDriveManager driveManager = ((it.pioppi.business.activity.MainActivity) requireActivity()).getGoogleDriveManager();
            groupAdapter = new ItemHistoryGroupAdapter(groupList, driveManager);
            recyclerViewItemHistory.setAdapter(groupAdapter);
        });

        return view;
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        LoggerManager.getInstance().log("onPrepareOptionsMenu: Configurazione menu di ItemHistoryFragment", "DEBUG");
        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem != null) {
            searchItem.setVisible(false); // Nascondi l'elemento di ricerca
            LoggerManager.getInstance().log("onPrepareOptionsMenu: Elemento di ricerca nascosto", "DEBUG");
        }
        super.onPrepareOptionsMenu(menu);
    }
}