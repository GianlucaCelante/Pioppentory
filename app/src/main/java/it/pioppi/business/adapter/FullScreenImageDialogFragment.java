package it.pioppi.business.adapter;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import it.pioppi.R;

public class FullScreenImageDialogFragment extends DialogFragment {

    private static final String ARG_IMAGE_URL = "image_url";
    private String imageUrl;

    public static FullScreenImageDialogFragment newInstance(String imageUrl) {
        FullScreenImageDialogFragment fragment = new FullScreenImageDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE_URL, imageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Imposta lo stile full screen per il dialog
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_full_image, container, false);

        ImageView fullImageView = view.findViewById(R.id.full_image_view);
        MaterialButton uploadImageButton = view.findViewById(R.id.upload_image_button);

        if (getArguments() != null) {
            imageUrl = getArguments().getString(ARG_IMAGE_URL);
        }

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

        // Listener per il bottone (es. per caricare/modificare l'immagine)
        uploadImageButton.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Upload image clicked", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        // Dismiss cliccando sull'area vuota
        view.setOnClickListener(v -> dismiss());

        return view;
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
}
