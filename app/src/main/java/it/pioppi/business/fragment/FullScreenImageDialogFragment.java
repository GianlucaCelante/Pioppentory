package it.pioppi.business.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.UUID;

import it.pioppi.R;
import it.pioppi.business.viewmodel.GeneralItemViewModel;
import it.pioppi.utils.ConstantUtils;

public class FullScreenImageDialogFragment extends DialogFragment {

    private String imageUrl;
    private UUID itemId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getActivity() != null) {
            View toolbar = getActivity().findViewById(R.id.toolbar);
            if (toolbar != null) {
                toolbar.setVisibility(View.GONE);
            }
            View bottomNav = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.GONE);
            }
        }

        if (getArguments() != null) {
            itemId = UUID.fromString(getArguments().getString(ConstantUtils.ITEM_ID));
            imageUrl = getArguments().getString(ConstantUtils.IMAGE_URL);
        }


        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_full_image, container, false);

        ImageView fullImageView = view.findViewById(R.id.full_image_view);
        MaterialButton uploadImageButton = view.findViewById(R.id.upload_image_button);
        MaterialButton selectImageButton = view.findViewById(R.id.select_image_button);

        selectImageButton.setOnClickListener(v -> {
            if (itemId == null) {
                return;
            }
            NavController navController = NavHostFragment.findNavController(this);
            Bundle bundle = new Bundle();
            bundle.putString("itemId", itemId.toString());
            navController.navigate(R.id.action_fullScreenImageDialogFragment_to_driveImageSelectionFragment, bundle);
        });

        // Listener per il bottone (es. per caricare/modificare l'immagine)
        uploadImageButton.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Upload image clicked", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        if (getArguments() != null) {
            imageUrl = getArguments().getString(ConstantUtils.IMAGE_URL);
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

        // Ottieni il GeneralItemViewModel condiviso dall'Activity
        GeneralItemViewModel viewModel = new ViewModelProvider(requireActivity()).get(GeneralItemViewModel.class);

        // Osserva la lista degli item (o un LiveData specifico per l'item corrente)
        viewModel.getItems().observe(getViewLifecycleOwner(), items -> {
            // Cerca l'item corrente per ID
            items.stream()
                    .filter(item -> item.getId().equals(itemId))
                    .findFirst()
                    .ifPresent(item -> {
                        String newImageUrl = item.getImageUrl();
                        if (newImageUrl != null && !newImageUrl.isEmpty()) {
                            Glide.with(requireContext())
                                    .load(newImageUrl)
                                    .placeholder(R.drawable.placeholder_thin)
                                    .error(R.drawable.placeholder_thin)
                                    .centerCrop()
                                    .into(fullImageView);
                            Log.d("FullScreenImageDialog", "Immagine aggiornata: " + newImageUrl);
                        }
                    });
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        // Imposta il dialog per occupare l'intero schermo
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            View toolbar = getActivity().findViewById(R.id.toolbar);
            if (toolbar != null) {
                toolbar.setVisibility(View.GONE);
            }
            View bottomNav = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() != null) {
            View toolbar = getActivity().findViewById(R.id.toolbar);
            if (toolbar != null) {
                toolbar.setVisibility(View.VISIBLE);
            }
            View bottomNav = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.VISIBLE);
            }
        }
    }
}
