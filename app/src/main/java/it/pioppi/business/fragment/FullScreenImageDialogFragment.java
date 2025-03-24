package it.pioppi.business.fragment;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.button.MaterialButton;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;

import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.DataSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.pioppi.R;
import it.pioppi.business.manager.GoogleDriveManager;
import it.pioppi.business.viewmodel.GeneralItemViewModel;
import it.pioppi.database.AppDatabase;
import it.pioppi.database.dao.ItemEntityDao;
import it.pioppi.utils.ConstantUtils;
import it.pioppi.utils.ImageUploadCallback;
import it.pioppi.utils.LoggerManager;

public class FullScreenImageDialogFragment extends DialogFragment {

    private ExecutorService executorService;
    private ItemEntityDao itemDao;
    private String imageUrl;
    private UUID itemId;
    private GeneralItemViewModel generalItemViewModel;
    private ActivityResultLauncher<String> pickImageLauncher;
    private MaterialButton uploadImageButton;
    private MaterialButton selectImageButton;
    private ProgressBar progressBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Imposta il dialog in modalità full screen
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);

        executorService = Executors.newSingleThreadExecutor();
        itemDao = AppDatabase.getInstance(getContext()).itemEntityDao();

        // Nascondi toolbar e bottom navigation
        if (getActivity() != null) {
            View toolbar = getActivity().findViewById(R.id.toolbar);
            if (toolbar != null) toolbar.setVisibility(View.GONE);
            View bottomNav = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) bottomNav.setVisibility(View.GONE);
        }

        // Recupera gli argomenti passati al fragment
        if (getArguments() != null) {
            itemId = UUID.fromString(getArguments().getString(ConstantUtils.ITEM_ID));
            imageUrl = getArguments().getString(ConstantUtils.IMAGE_URL);
        }

        // Inizializza il GeneralItemViewModel condiviso
        generalItemViewModel = new ViewModelProvider(requireActivity()).get(GeneralItemViewModel.class);

        // Registra il launcher per selezionare l’immagine dal device
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                uploadImageFromDevice(uri);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_full_image, container, false);

        ImageView fullImageView = view.findViewById(R.id.full_image_view);
        uploadImageButton = view.findViewById(R.id.upload_image_button);
        selectImageButton = view.findViewById(R.id.select_image_button);
        progressBar = view.findViewById(R.id.progressBar);

        // Imposta il listener per il pulsante per selezionare un'immagine già presente su Drive (Opzione 1)
        selectImageButton.setOnClickListener(v -> {
            if (itemId == null) return;
            NavController navController = NavHostFragment.findNavController(this);
            Bundle bundle = new Bundle();
            bundle.putString(ConstantUtils.ITEM_ID, itemId.toString());
            navController.navigate(R.id.action_fullScreenImageDialogFragment_to_driveImageSelectionFragment, bundle);
        });

        // Imposta il listener per il pulsante di upload dal device (Opzione 2)
        uploadImageButton.setOnClickListener(v -> {
            // Disabilita la UI e mostra il progressBar
            setUIEnabled(false);
            Toast.makeText(requireContext(), "Caricamento immagine in corso, attendere...", Toast.LENGTH_SHORT).show();
            pickImageLauncher.launch("image/*");
        });

        // Carica l'immagine corrente se presente
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(requireContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_thin)
                    .error(R.drawable.placeholder_thin)
                    .centerCrop()
                    .into(fullImageView);
        } else {
            fullImageView.setImageResource(R.drawable.placeholder_thin);
        }

        // Dismiss cliccando sull'area vuota
        view.setOnClickListener(v -> {
            dismiss();
            getParentFragmentManager().popBackStack();
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView fullImageView = view.findViewById(R.id.full_image_view);

        // Aggiungi un observer sul LiveData degli item
        generalItemViewModel.getItems().observe(getViewLifecycleOwner(), items -> {
            items.stream()
                    .filter(item -> item.getId().equals(itemId))
                    .findFirst()
                    .ifPresent(item -> {
                        String updatedImageUrl = item.getImageUrl();
                        if (updatedImageUrl != null && !updatedImageUrl.isEmpty()) {
                            Glide.with(requireContext())
                                    .load(updatedImageUrl)
                                    .placeholder(R.drawable.placeholder_thin)
                                    .error(R.drawable.placeholder_thin)
                                    .centerCrop()
                                    .into(fullImageView);
                            Log.d("FullScreenImageDialog", "Immagine aggiornata: " + updatedImageUrl);
                        }
                    });
        });
    }

    /**
     * Metodo per abilitare o disabilitare i pulsanti e mostrare/nascondere la ProgressBar.
     */
    private void setUIEnabled(boolean enabled) {
        if (uploadImageButton != null && selectImageButton != null && progressBar != null) {
            uploadImageButton.setEnabled(enabled);
            selectImageButton.setEnabled(enabled);
            progressBar.setVisibility(enabled ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Legge l'immagine selezionata dal device e la carica su Drive.
     */
    private void uploadImageFromDevice(Uri uri) {
        try {
            InputStream is = requireContext().getContentResolver().openInputStream(uri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }
            is.close();
            byte[] imageBytes = baos.toByteArray();
            String fileName = "img_" + System.currentTimeMillis() + ".jpg";

            // Ottieni l'istanza di GoogleDriveManager dalla MainActivity
            GoogleDriveManager driveManager = ((it.pioppi.business.activity.MainActivity) requireActivity()).getGoogleDriveManager();

            // Carica l'immagine su Drive
            driveManager.uploadImage(fileName, imageBytes, requireContext(), new ImageUploadCallback() {
                @Override
                public void onSuccess(String fileId) {
                    if (!isAdded()) return;
                    // Usa il formato URL "uc?export=view&id=FILE_ID"
                    String newImageUrl = "https://drive.google.com/uc?export=view&id=" + fileId;

                    executorService.submit(() -> {
                        // Aggiorna il DB tramite il DAO
                        itemDao.updateItemImageUrl(itemId, newImageUrl);
                        // Aggiorna il LiveData nel ViewModel; questo dovrebbe notificare tutti gli osservatori
                        generalItemViewModel.updateItemImageUrl(itemId, newImageUrl);
                    });

                    requireActivity().runOnUiThread(() -> {
                        loadImageWithRetry(newImageUrl, requireView().findViewById(R.id.full_image_view), 5);
                        Toast.makeText(requireContext(), "Immagine caricata con successo", Toast.LENGTH_SHORT).show();
                        setUIEnabled(true);
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    if (!isAdded()) return;
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Errore durante il caricamento dell'immagine", Toast.LENGTH_SHORT).show();
                        LoggerManager.getInstance().log("Upload image error", "ERROR");
                        // Riabilita la UI per consentire di riprovare
                        setUIEnabled(true);
                    });
                }
            });
        } catch (IOException e) {
            LoggerManager.getInstance().logException(e);
            Toast.makeText(requireContext(), "Errore nella lettura del file", Toast.LENGTH_SHORT).show();
            setUIEnabled(true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Nascondi toolbar e bottom navigation
        if (getActivity() != null) {
            View toolbar = getActivity().findViewById(R.id.toolbar);
            if (toolbar != null) toolbar.setVisibility(View.GONE);
            View bottomNav = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) bottomNav.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Ripristina la visibilità quando il dialog viene chiuso
        if (getActivity() != null) {
            View toolbar = getActivity().findViewById(R.id.toolbar);
            if (toolbar != null) toolbar.setVisibility(View.VISIBLE);
            View bottomNav = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) bottomNav.setVisibility(View.VISIBLE);
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

    private void loadImageWithRetry(String url, ImageView imageView, int retryCount) {
        Glide.with(requireContext())
                .load(url)
                .placeholder(R.drawable.placeholder_thin)
                .error(R.drawable.placeholder_thin)
                .centerCrop()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        if (retryCount > 0) {

                            Toast.makeText(requireContext(), "Immagine non ancora disponibile", Toast.LENGTH_SHORT).show();

                            // Ritenta dopo 2 secondi
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                loadImageWithRetry(url, imageView, retryCount - 1);
                            }, 2000);
                        } else {
                            // Se esauriti i tentativi, mostra l'errore (o lascia il placeholder)
                            Toast.makeText(requireContext(), "Impossibile caricare l'immagine", Toast.LENGTH_SHORT).show();
                        }
                        return false; // consente a Glide di gestire ulteriori operazioni
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        // Immagine caricata con successo
                        return false;
                    }
                })
                .into(imageView);
    }
}
