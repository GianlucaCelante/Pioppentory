package it.pioppi.business.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import it.pioppi.R;
import it.pioppi.business.adapter.SettingAdapter;
import it.pioppi.business.dto.settings.BaseSettingDto;
import it.pioppi.business.dto.settings.ComplexSettingDto;
import it.pioppi.business.dto.settings.SettingType;
import it.pioppi.utils.LoggerManager;

public class SettingsFragment extends Fragment implements SettingAdapter.OnSettingClickListener {

    private RecyclerView recyclerViewSettings;
    private SettingAdapter adapter;
    private List<BaseSettingDto> settings;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        LoggerManager.getInstance().log("SettingsFragment onCreate started", "INFO");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        LoggerManager.getInstance().log("SettingsFragment onCreate completed", "INFO");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        LoggerManager.getInstance().log("SettingsFragment onCreateView started", "INFO");
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        recyclerViewSettings = view.findViewById(R.id.settings_recycler_view);
        recyclerViewSettings.setLayoutManager(new LinearLayoutManager(getContext()));

        initSettings();

        adapter = new SettingAdapter(settings, this);
        recyclerViewSettings.setAdapter(adapter);
        LoggerManager.getInstance().log("SettingsFragment onCreateView completed", "INFO");
        return view;
    }

    private void initSettings() {
        LoggerManager.getInstance().log("initSettings started", "DEBUG");
        settings = new ArrayList<>();
        settings.add(new ComplexSettingDto("Bluetooth", "Open Bluetooth fragment",
                R.drawable.bluetooth_signal_icon, BluetoothFragment.class));
        LoggerManager.getInstance().log("initSettings completed with " + settings.size() + " setting(s)", "DEBUG");
    }

    @Override
    public void onSettingClick(BaseSettingDto setting) {
        LoggerManager.getInstance().log("onSettingClick called for setting: " + setting.getName(), "DEBUG");
        if (setting.getType() == SettingType.COMPLEX) {
            LoggerManager.getInstance().log("Navigating to BluetoothFragment", "DEBUG");
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.action_settingsFragment_to_bluetoothFragment, null);
        } else {
            LoggerManager.getInstance().log("Basic setting clicked: " + setting.getName(), "DEBUG");
            // Gestione diretta per impostazioni basilari (es. toggle o slider)
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
}
