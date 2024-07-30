package it.pioppi.business.activity;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import it.pioppi.R;
import it.pioppi.business.adapter.ItemAdapter;
import it.pioppi.business.dto.ItemDto;
import it.pioppi.business.viewmodel.ItemViewModel;
import it.pioppi.database.AppDatabase;
import it.pioppi.database.dao.ItemFTSEntityDao;

public class SearchableActivity extends AppCompatActivity {

    private ItemAdapter itemAdapter;
    private ItemViewModel sharedItemViewModel;
    private AppDatabase appDatabase;
    private ItemFTSEntityDao itemFTSEntityDao;
    private ExecutorService executorService;
    private RecyclerView recyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);

        // Inizializzazione database e servizi
        appDatabase = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();
        itemFTSEntityDao = appDatabase.itemFTSEntityDao();

        // Inizializzazione ViewModel e Adapter
        sharedItemViewModel = new ViewModelProvider(this).get(ItemViewModel.class);
        itemAdapter = new ItemAdapter(new ArrayList<>(), item -> {}, item -> {}, this);

        // Configurazione RecyclerView
        recyclerView = findViewById(R.id.recycler_view_search);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        recyclerView.setAdapter(itemAdapter);

        // Ricevi i dati dall'intent e imposta nel ViewModel
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("items")) {
            List<ItemDto> items = intent.getParcelableArrayListExtra("items");
            sharedItemViewModel.setItems(items);
        }

        // Gestione dell'intent di ricerca
        handleSearchIntent(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_searchable, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_search) {
            SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
            SearchView searchView = (SearchView) item.getActionView();
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    // Esegui la ricerca con il testo inserito
                    searchForNameAndBarcode(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    // Esegui azioni durante la modifica del testo di ricerca, se necessario
                    return false;
                }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleSearchIntent(intent);
    }

    private void handleSearchIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            if (query != null) {
                searchForNameAndBarcode(query);
            }
        }
    }

    private void searchForNameAndBarcode(String query) {
        executorService.execute(() -> {
            List<Integer> itemFTSIds = itemFTSEntityDao.searchForNameAndBarcode(query);
            Log.d("SearchableActivity", "FTS Ids: " + itemFTSIds.size());

            runOnUiThread(() -> sharedItemViewModel.getItems().observe(this, itemDtos -> {
                if (itemDtos != null) {
                    Log.d("SearchableActivity", "Observed items in ViewModel: " + itemDtos.size());
                    List<ItemDto> filteredItemDtos = itemDtos.stream()
                            .filter(itemDto -> itemFTSIds.contains(itemDto.getFtsId()))
                            .collect(Collectors.toList());
                    itemAdapter.setItemList(filteredItemDtos);
                } else {
                    Log.d("SearchableActivity", "No items in ViewModel");
                }
            }));
        });
    }
}
