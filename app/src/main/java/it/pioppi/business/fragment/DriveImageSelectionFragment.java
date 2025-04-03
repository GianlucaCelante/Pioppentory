package it.pioppi.business.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.api.services.drive.model.File;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.pioppi.R;
import it.pioppi.business.adapter.DriveImageAdapter;
import it.pioppi.business.manager.GoogleDriveManager;
import it.pioppi.business.viewmodel.GeneralItemViewModel;
import it.pioppi.database.AppDatabase;
import it.pioppi.database.dao.ItemEntityDao;
import it.pioppi.utils.ConstantUtils;
import it.pioppi.utils.ImageListCallback;
import it.pioppi.utils.LoggerManager;

public class DriveImageSelectionFragment extends Fragment implements ImageListCallback, DriveImageAdapter.OnImageClickListener {

    private ExecutorService executorService;
    private ProgressBar progressBar;
    private DriveImageAdapter adapter;
    private GoogleDriveManager driveManager;
    private ItemEntityDao itemDao;
    private UUID itemId;
    private GeneralItemViewModel generalItemViewModel;
    private SwipeRefreshLayout swipeRefreshLayout;

    public interface OnImageSelectedListener {
        void onImageSelected(String imageUrl);
    }
    private OnImageSelectedListener listener;

    public void setOnImageSelectedListener(OnImageSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        executorService = Executors.newSingleThreadExecutor();

        Bundle args = getArguments();
        if (args != null) {
            itemId = UUID.fromString(args.getString(ConstantUtils.ITEM_ID));
        }

        driveManager = ((it.pioppi.business.activity.MainActivity) getActivity()).getGoogleDriveManager();
        itemDao = AppDatabase.getInstance(getContext()).itemEntityDao();
        generalItemViewModel = new ViewModelProvider(requireActivity()).get(GeneralItemViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Usa il layout che include lo SwipeRefreshLayout
        View view = inflater.inflate(R.layout.fragment_image_selection_from_drive, container, false);
        progressBar = view.findViewById(R.id.progressBar);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewImages);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        adapter = new DriveImageAdapter(this);
        recyclerView.setAdapter(adapter);

        // Imposta il listener dello SwipeRefreshLayout per ricaricare le immagini
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (driveManager == null) {
                driveManager = ((it.pioppi.business.activity.MainActivity) requireActivity()).getGoogleDriveManager();
            }
            if (driveManager != null) {
                driveManager.listImages(this);
            } else {
                Toast.makeText(getContext(), "Drive manager non disponibile", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressBar.setVisibility(View.VISIBLE);
        if (driveManager == null) {
            driveManager = ((it.pioppi.business.activity.MainActivity) requireActivity()).getGoogleDriveManager();
        }
        if (driveManager != null) {
            driveManager.listImages(this);
        } else {
            Toast.makeText(getContext(), "Drive manager non disponibile", Toast.LENGTH_SHORT).show();
            Log.e("DriveImageSelection", "driveManager è null");
        }
    }

    // Callback da ImageListCallback
    @Override
    public void onResult(List<File> imageFiles) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            adapter.setImageFiles(imageFiles);
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onError(Exception e) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getContext(), "Errore nel recupero delle immagini: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("DriveImageSelection", "Errore: ", e);
        });
    }

    // Callback dal DriveImageAdapter quando l'utente seleziona un'immagine
    @Override
    public void onImageClicked(File imageFile) {
        String imageUrl = imageFile.getWebContentLink();
        if (listener != null) {
            listener.onImageSelected(imageUrl);
        }
        executorService.submit(() -> {
            itemDao.updateItemImageUrl(itemId, imageUrl);
            generalItemViewModel.updateItemImageUrl(itemId, imageUrl);
        });
        Toast.makeText(getContext(), "Immagine selezionata: " + imageUrl, Toast.LENGTH_SHORT).show();
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem != null) {
            searchItem.setVisible(false);
            LoggerManager.getInstance().log("onPrepareOptionsMenu: Elemento di ricerca nascosto", "DEBUG");
        }
        super.onPrepareOptionsMenu(menu);
    }
}
