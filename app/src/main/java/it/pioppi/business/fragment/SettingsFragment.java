package it.pioppi.business.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.work.impl.constraints.ConstraintsState;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import it.pioppi.R;
import it.pioppi.business.adapter.SettingAdapter;
import it.pioppi.business.dto.settings.BaseSettingDto;
import it.pioppi.business.dto.settings.ComplexSettingDto;
import it.pioppi.business.dto.settings.SettingType;
import it.pioppi.business.dto.settings.SimpleSettingDto;
import it.pioppi.database.AppDatabase;
import it.pioppi.utils.ConstantUtils;
import it.pioppi.utils.DateTimeUtils;
import it.pioppi.utils.LoggerManager;

public class SettingsFragment extends Fragment implements SettingAdapter.OnSettingClickListener {

    private List<BaseSettingDto> settings;
    private ActivityResultLauncher<Intent> backupLauncher;
    private ActivityResultLauncher<Intent> restoreLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        LoggerManager.getInstance().log("SettingsFragment onCreate started", "INFO");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        LoggerManager.getInstance().log("SettingsFragment onCreate completed", "INFO");

        backupLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        backupDatabaseToUri(uri);   // <<—— qui il vero backup
                    }
                }
        );

        // 2) Restore: apre il file ZIP e chiede conferma
        restoreLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        showRestoreConfirmationDialog(uri);
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        LoggerManager.getInstance().log("SettingsFragment onCreateView started", "INFO");
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        RecyclerView recyclerViewSettings = view.findViewById(R.id.settings_recycler_view);
        recyclerViewSettings.setLayoutManager(new LinearLayoutManager(getContext()));

        initSettings();

        SettingAdapter adapter = new SettingAdapter(settings, this);
        recyclerViewSettings.setAdapter(adapter);
        LoggerManager.getInstance().log("SettingsFragment onCreateView completed", "INFO");
        return view;
    }

    private void initSettings() {
        LoggerManager.getInstance().log("initSettings started", "DEBUG");
        settings = new ArrayList<>();
        settings.add(new ComplexSettingDto(ConstantUtils.BLUETOOTH, "Open Bluetooth fragment",
                R.drawable.bluetooth_signal_icon, BluetoothFragment.class));
        settings.add(new ComplexSettingDto(ConstantUtils.DRIVE,"Google Drive settings",
                R.drawable.drive_icon, GoogleDriveSettingsFragment.class));
        settings.add(new SimpleSettingDto<>(ConstantUtils.RESET_CHOICES, "Ripristina il dialogo di conferma per la deselezione",
                R.drawable.reset_icon, null));
        settings.add(new SimpleSettingDto<>(ConstantUtils.BACKUP_DB, "Crea una copia di backup del database locale",
                R.drawable.backup_icon, null));
        settings.add(new SimpleSettingDto<>(ConstantUtils.RESTORE_DB, "Ripristina database da backup",
                R.drawable.reset_icon, null));
        LoggerManager.getInstance().log("initSettings completed with " + settings.size() + " setting(s)", "DEBUG");
    }


    @Override
    public void onSettingClick(BaseSettingDto setting) {
        LoggerManager.getInstance().log("onSettingClick called for setting: " + setting.getName(), "DEBUG");
        if (setting.getType().equals(SettingType.COMPLEX)) {

            if(setting.getName().equals(ConstantUtils.BLUETOOTH)) {
                LoggerManager.getInstance().log("Navigating to BluetoothFragment", "DEBUG");
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.action_settingsFragment_to_bluetoothFragment, null);
            }

            if(setting.getName().equals(ConstantUtils.DRIVE)) {
                LoggerManager.getInstance().log("Navigating to DriveSettingsFragment", "DEBUG");
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.action_settingsFragment_to_googleDriveSettingsFragment, null);
            }

        } else {
            LoggerManager.getInstance().log("Basic setting clicked: " + setting.getName(), "DEBUG");
            if (setting.getName().equals(ConstantUtils.RESET_CHOICES)) {
                SharedPreferences prefs = requireContext().getSharedPreferences(ConstantUtils.MY_APP_PREFS, Context.MODE_PRIVATE);
                prefs.edit().putBoolean("skip_deselection_confirmation", false).apply();
                Toast.makeText(getContext(), "Reset conferme popup completato", Toast.LENGTH_SHORT).show();
            }

            if (setting.getName().equals(ConstantUtils.BACKUP_DB)) {
                backupDatabaseWithSAF();
            }

            if (setting.getName().equals(ConstantUtils.RESTORE_DB)) {
                openFileExplorerToRestore();
            }
        }
    }


    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        LoggerManager.getInstance().log("onPrepareOptionsMenu started", "DEBUG");
        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem != null) {
            searchItem.setVisible(false); // Nascondi l'elemento di ricerca
            LoggerManager.getInstance().log("Search menu item hidden", "DEBUG");
        }
        super.onPrepareOptionsMenu(menu);
        LoggerManager.getInstance().log("onPrepareOptionsMenu completed", "DEBUG");
    }

    private void backupDatabaseWithSAF() {
        String dbFileName = ConstantUtils.APP_DATABASE;
        File dbFile = requireContext().getDatabasePath(dbFileName);
        if (!dbFile.exists()) {
            Toast.makeText(requireContext(),
                    "Nessun database trovato da esportare (" + dbFile.getAbsolutePath() + ")",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // 1) Checkpoint WAL e chiusura DB
        performWalCheckpoint();
        AppDatabase.getInstance(requireContext()).close();

        // 2) Creazione dello ZIP via SAF
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/zip");
        String now = DateTimeUtils.formatForDisplayToDate(
                ZonedDateTime.now(ZoneId.of(ConstantUtils.ZONE_ID))
        );
        intent.putExtra(Intent.EXTRA_TITLE,
                ConstantUtils.DB_NAME + "_bk-" + now + ".zip");
        backupLauncher.launch(intent);
    }

    private void performWalCheckpoint() {
        SupportSQLiteDatabase sqldb = AppDatabase.getInstance(requireContext())
                        .getOpenHelper()
                        .getWritableDatabase();

        try (Cursor c = sqldb.query("PRAGMA wal_checkpoint(FULL)", new Object[]{})) {
        }
    }

    private void backupDatabaseToUri(Uri uri) {
        // nome e path del DB
        String dbFileName = ConstantUtils.APP_DATABASE;
        File dbFile = requireContext().getDatabasePath(dbFileName);
        File walFile = new File(dbFile.getAbsolutePath() + "-wal");
        File shmFile = new File(dbFile.getAbsolutePath() + "-shm");

        try (OutputStream os = requireContext()
                .getContentResolver()
                .openOutputStream(uri);
             ZipOutputStream zos = new ZipOutputStream(os)) {

            // file DB principale
            writeFileToZip(dbFile, dbFileName, zos);
            // WAL e SHM se presenti
            if (walFile.exists()) writeFileToZip(walFile, dbFileName + "-wal", zos);
            if (shmFile.exists()) writeFileToZip(shmFile, dbFileName + "-shm", zos);

            zos.finish();
            Toast.makeText(requireContext(),
                    "Backup completato", Toast.LENGTH_LONG).show();
            LoggerManager.getInstance().log("Backup ZIP completato", "INFO");

        } catch (IOException e) {
            LoggerManager.getInstance().logException(e);
            Toast.makeText(requireContext(),
                    "Backup fallito: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void writeFileToZip(File file, String entryName, ZipOutputStream zos) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            zos.putNextEntry(new ZipEntry(entryName));
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
            zos.closeEntry();
        }
    }


    private void restoreDatabaseFromUri(Uri zipUri) {

        File dbFile = requireContext().getDatabasePath(ConstantUtils.APP_DATABASE);
        File dbDir  = dbFile.getParentFile();
        if (dbDir == null || !dbDir.exists()) {
            Toast.makeText(requireContext(),
                    "Cartella database non trovata", Toast.LENGTH_LONG).show();
            return;
        }

        // Validazione dello ZIP
        Set<String> required = new HashSet<>(
                Arrays.asList(
                        ConstantUtils.APP_DATABASE,
                        ConstantUtils.APP_DATABASE + "-wal",
                        ConstantUtils.APP_DATABASE + "-shm"
                ));
        Set<String> found = new HashSet<>();

        try (InputStream is = requireContext().getContentResolver().openInputStream(zipUri);
             ZipInputStream zis = new ZipInputStream(is)) {

            ZipEntry entry;
            // Primo pass: controllo nomi entry
            while ((entry = zis.getNextEntry()) != null) {
                found.add(entry.getName());
                zis.closeEntry();
            }

            if (!found.containsAll(required)) {
                Toast.makeText(requireContext(),
                        "Backup non valido: mancano file nel pacchetto", Toast.LENGTH_LONG).show();
                return;
            }
        } catch (IOException e) {
            LoggerManager.getInstance().logException(e);
            Toast.makeText(requireContext(),
                    "Errore validazione backup: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            return;
        }

        try (InputStream is = requireContext().getContentResolver().openInputStream(zipUri);
             ZipInputStream zis = new ZipInputStream(is)) {

            byte[] buffer = new byte[1024];
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                // CREA IL FILE di destinazione nella cartella dbDir
                File outFile = new File(dbDir, entry.getName());
                // Se esiste già, sovrascrivi
                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
                zis.closeEntry();
            }

            Toast.makeText(requireContext(),
                    "Ripristino completato", Toast.LENGTH_LONG).show();
            LoggerManager.getInstance().log("Database ripristinato da ZIP", "INFO");

        } catch (IOException e) {
            LoggerManager.getInstance().logException(e);
            Toast.makeText(requireContext(),
                    "Ripristino fallito: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void openFileExplorerToRestore() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/zip");
        restoreLauncher.launch(intent);
    }

    private void showRestoreConfirmationDialog(Uri uri) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Conferma ripristino")
                .setMessage("Attenzione: il database corrente sarà sovrascritto. L'app verrà riavviata.")
                .setPositiveButton("Ripristina", (dialog, which) -> {
                    restoreDatabaseFromUri(uri);
                    restartApp();
                })
                .setNegativeButton("Annulla", null)
                .show();
    }

    private void restartApp() {
        Intent intent = requireActivity().getPackageManager()
                .getLaunchIntentForPackage(requireActivity().getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            requireActivity().finish();
            Runtime.getRuntime().exit(0);
        }
    }
}
