package it.pioppi.business.activity;

import android.app.SearchManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.pioppi.R;
import it.pioppi.business.dto.ItemDto;
import it.pioppi.business.viewmodel.ItemViewModel;
import it.pioppi.database.AppDatabase;
import it.pioppi.database.dao.ItemFTSEntityDao;
import it.pioppi.database.mapper.EntityDtoMapper;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private ItemViewModel itemViewModel;
    private ItemFTSEntityDao itemFTSEntityDao;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }
        NavigationUI.setupActionBarWithNavController(this, navController);

        // Initialize ViewModel and database
        itemViewModel = new ViewModelProvider(this).get(ItemViewModel.class);
        AppDatabase appDatabase = AppDatabase.getInstance(this);
        itemFTSEntityDao = appDatabase.itemFTSEntityDao();
        executorService = Executors.newSingleThreadExecutor();

        setupBottomNavigationBar();
    }

    private void setupBottomNavigationBar() {

        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    navController.navigate(R.id.itemFragment);
                    return true;
                } else if (itemId == R.id.nav_history) {
                    navController.navigate(R.id.itemHistoryFragment);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_searchable, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                executorService.submit(() -> {
                    List<ItemDto> items = EntityDtoMapper.dtosToEntitiesForItemDto(itemFTSEntityDao.searchForItem(newText));
                    runOnUiThread(() -> itemViewModel.setItems(items));
                });
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}
