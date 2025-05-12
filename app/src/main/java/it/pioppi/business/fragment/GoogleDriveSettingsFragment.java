package it.pioppi.business.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import it.pioppi.R;
import it.pioppi.business.activity.MainActivity;
import it.pioppi.business.adapter.SettingAdapter;
import it.pioppi.business.dto.settings.BaseSettingDto;
import it.pioppi.business.dto.settings.SimpleSettingDto;
import it.pioppi.utils.ConstantUtils;
import it.pioppi.utils.LoggerManager;

public class GoogleDriveSettingsFragment extends Fragment implements SettingAdapter.OnSettingClickListener {

    private List<BaseSettingDto> driveSettings;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_drive_settings, container, false);
        RecyclerView rv = view.findViewById(R.id.drive_settings_recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        initDriveSettings();
        SettingAdapter adapter = new SettingAdapter(driveSettings, this);
        rv.setAdapter(adapter);

        return view;
    }

    private void initDriveSettings() {
        driveSettings = new ArrayList<>();
        driveSettings.add(new SimpleSettingDto<>(ConstantUtils.DRIVE_RETRY,
                "Ritenta connessione al Drive",
                R.drawable.reset_icon, null));
        driveSettings.add(new SimpleSettingDto<>(ConstantUtils.DRIVE_CHANGE_ACCOUNT,
                "Cambia account e riconnettiti",
                R.drawable.switch_icon, null));
    }

    @Override
    public void onSettingClick(BaseSettingDto setting) {
        MainActivity act = (MainActivity) requireActivity();
        switch (setting.getName()) {
            case ConstantUtils.DRIVE_CHANGE_ACCOUNT:
                if (act.getGoogleDriveManager() != null) {
                    ((MainActivity) requireActivity()).changeDriveAccount();
                } else {
                    Toast.makeText(getContext(), "Non connesso al Drive", Toast.LENGTH_SHORT).show();
                }
                break;

            case ConstantUtils.DRIVE_RETRY:
                act.signIn();  // usa il launcher già definito in MainActivity
                break;

            // case per altre opzioni…
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
