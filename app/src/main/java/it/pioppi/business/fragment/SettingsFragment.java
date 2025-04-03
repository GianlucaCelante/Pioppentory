package it.pioppi.business.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.work.impl.constraints.ConstraintsState;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import it.pioppi.R;
import it.pioppi.business.adapter.SettingAdapter;
import it.pioppi.business.dto.settings.BaseSettingDto;
import it.pioppi.business.dto.settings.ComplexSettingDto;
import it.pioppi.business.dto.settings.SettingType;
import it.pioppi.business.dto.settings.SimpleSettingDto;
import it.pioppi.utils.ConstantUtils;
import it.pioppi.utils.DateTimeUtils;
import it.pioppi.utils.LoggerManager;

public class SettingsFragment extends Fragment implements SettingAdapter.OnSettingClickListener {

    private List<BaseSettingDto> settings;
    private ActivityResultLauncher<Intent> createDocumentLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        LoggerManager.getInstance().log("SettingsFragment onCreate started", "INFO");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        LoggerManager.getInstance().log("SettingsFragment onCreate completed", "INFO");

        createDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        backupDatabaseToUri(uri);
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
        settings.add(new SimpleSettingDto<>(ConstantUtils.RESET_CHOICES, "Ripristina il dialogo di conferma per la deselezione",
                R.drawable.reset_icon, null));
        settings.add(new SimpleSettingDto<>(ConstantUtils.BACKUP_DB, "Crea una copia di backup del database locale",
                R.drawable.backup_icon, null));
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
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // Imposta il MIME type, ad esempio per un database SQLite
        intent.setType("application/octet-stream");

        String now = DateTimeUtils.formatForDisplayToDate(ZonedDateTime.now(ZoneId.of(ConstantUtils.ZONE_ID)));
        intent.putExtra(Intent.EXTRA_TITLE, ConstantUtils.DB_NAME + "_bk-" + now + ".db");
        createDocumentLauncher.launch(intent);
    }

    private void backupDatabaseToUri(Uri uri) {
        String dbName = ConstantUtils.DB_NAME + ".db";
        File dbFile = requireContext().getDatabasePath(dbName);
        try (FileInputStream fis = new FileInputStream(dbFile);
             OutputStream os = requireContext().getContentResolver().openOutputStream(uri)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            Toast.makeText(requireContext(), "Backup completato: " + uri.getPath(), Toast.LENGTH_LONG).show();
            LoggerManager.getInstance().log("Backup completato: " + uri, "INFO");
        } catch (IOException e) {
            Log.e("SettingsFragment", "Error backing up database", e);
            Toast.makeText(requireContext(), "Backup fallito: " + e.getMessage(), Toast.LENGTH_LONG).show();
            LoggerManager.getInstance().logException(e);
        }
    }


}
