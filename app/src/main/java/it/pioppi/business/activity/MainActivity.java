package it.pioppi.business.activity;

import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import it.pioppi.utils.ConstantUtils;
import it.pioppi.R;
import it.pioppi.business.dto.item.ItemDto;
import it.pioppi.business.manager.GoogleDriveManager;
import it.pioppi.business.viewmodel.GeneralItemViewModel;
import it.pioppi.database.AppDatabase;
import it.pioppi.database.dao.ItemFTSEntityDao;
import it.pioppi.database.mapper.EntityDtoMapper;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private GeneralItemViewModel generalItemViewModel;
    private ItemFTSEntityDao itemFTSEntityDao;
    private ExecutorService executorService;
    private AppDatabase appDatabase;

    // Google Sign-In & Drive integration
    private static final String DRIVE_SCOPE = "https://www.googleapis.com/auth/drive";
    private GoogleSignInClient googleSignInClient;
    private GoogleDriveManager googleDriveManager;
    private ActivityResultLauncher<Intent> signInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appDatabase = AppDatabase.getInstance(this);

        Log.d("MainActivity", "Registering BroadcastReceiver for scanned code events");
        // Registra il receiver per intercettare gli eventi di scansione
        IntentFilter filter = new IntentFilter(ConstantUtils.ACTION_CODE_SCANNED);
        LocalBroadcastManager.getInstance(this).registerReceiver(scanReceiver, filter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }
        NavigationUI.setupActionBarWithNavController(this, navController);

        // Initialize ViewModel and database
        generalItemViewModel = new ViewModelProvider(this).get(GeneralItemViewModel.class);
        AppDatabase appDatabase = AppDatabase.getInstance(this);
        itemFTSEntityDao = appDatabase.itemFTSEntityDao();
        executorService = Executors.newSingleThreadExecutor();

        setupBottomNavigationBar();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DRIVE_SCOPE))
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(Exception.class);
                            initializeDriveService(account);
                        } catch (Exception e) {
                            Log.e("MainActivity", "Errore durante il Sign-In", e);
                        }
                    }
                }
        );

        // **Nuovo approccio: prova il silent sign-in**
        attemptSilentSignIn();
    }

    private void attemptSilentSignIn() {
        googleSignInClient.silentSignIn()
                .addOnSuccessListener(this, account -> {
                    // Se ha già accettato tutti gli scope, arriva qui
                    Log.d("MainActivity", "SilentSignIn OK");
                    initializeDriveService(account);
                })
                .addOnFailureListener(this, e -> {
                    // Altrimenti mostro il picker
                    Log.d("MainActivity", "SilentSignIn fallito, prompt login");
                    signIn();
                });
    }

    // Metodo per avviare il processo di Sign-In
    public void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        signInLauncher.launch(signInIntent);
    }

    // Inizializza il servizio Drive e crea il GoogleDriveManager
    private void initializeDriveService(GoogleSignInAccount account) {
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                this,
                Collections.singleton(DRIVE_SCOPE)
        );
        credential.setSelectedAccount(account.getAccount());
        Drive driveService = new Drive.Builder(
                new NetHttpTransport(),
                new GsonFactory(),
                credential)
                .setApplicationName("Pioppentory")
                .build();

        googleDriveManager = new GoogleDriveManager(driveService);
        Toast.makeText(this, "Accesso a Drive riuscito", Toast.LENGTH_SHORT).show();
        Log.d("MainActivity", "Drive Service inizializzato");
    }

    // Getter per GoogleDriveManager, utile per i Fragment/Adapter
    public GoogleDriveManager getGoogleDriveManager() {
        return googleDriveManager;
    }

    private void setupBottomNavigationBar() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                navController.navigate(R.id.itemFragment);
                return true;
            } else if (itemId == R.id.nav_history) {
                navController.navigate(R.id.itemHistoryFragment);
                return true;
            } else if (itemId == R.id.nav_options) {
                navController.navigate(R.id.settingsFragment);
                return true;
            }
            return false;
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
                    runOnUiThread(() -> generalItemViewModel.setItems(items));
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

    private final BroadcastReceiver scanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("MainActivity", "action received: " + intent.getAction());
            if (ConstantUtils.ACTION_CODE_SCANNED.equals(intent.getAction())) {
                String scannedCode = intent.getStringExtra(ConstantUtils.SCANNED_CODE);
                Log.d("MainActivity", "Scanned code: " + scannedCode);
                openDetailFragment(scannedCode);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("MainActivity", "Unregistering BroadcastReceiver for scanned code events");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(scanReceiver);
    }

    private void openDetailFragment(String scannedCode) {
        Bundle bundle = new Bundle();
        bundle.putString(ConstantUtils.SCANNED_CODE, scannedCode);

        AtomicReference<String> itemId = new AtomicReference<>();
        Future<?> future = executorService.submit(() -> itemId.set(appDatabase.itemEntityDao().getItemByBarcode(scannedCode)));
        try {
            future.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        if(itemId.get() == null) {
            Toast.makeText(this, "Nessun articolo trovato con barcode: " + scannedCode, Toast.LENGTH_SHORT).show();
            return;
        }

        NavOptions navOptions = new NavOptions.Builder()
                .setPopUpTo(R.id.itemDetailFragment, true)
                .build();

        navController.navigate(R.id.itemDetailFragment, bundle, navOptions);
    }

    /**
     * Forza il cambio account: esegue il sign-out e poi rilancia il picker Google Sign-In.
     * Usato dal DriveSettingsFragment.
     */
    public void changeDriveAccount() {
        if (googleSignInClient == null) {
            Toast.makeText(this, "Non connesso al Drive", Toast.LENGTH_SHORT).show();
            return;
        }
        // 1) Sign-out dall’account corrente
        googleSignInClient.signOut()
                .addOnCompleteListener(this, task -> {
                    Log.d("MainActivity", "Drive sign-out completato, mostro account picker");
                    // 2) Dopo il sign-out, rilancio il picker
                    signIn();
                });
    }

}
