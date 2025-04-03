package it.pioppi.business.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.api.services.drive.model.File;
import java.util.ArrayList;
import java.util.List;
import it.pioppi.R;

public class DriveImageAdapter extends RecyclerView.Adapter<DriveImageAdapter.ImageViewHolder> {

    private List<File> imageFiles = new ArrayList<>();
    private OnImageClickListener listener;

    public interface OnImageClickListener {
        void onImageClicked(File imageFile);
    }

    public DriveImageAdapter(OnImageClickListener listener) {
        this.listener = listener;
    }

    public void setImageFiles(List<File> files) {
        this.imageFiles = files;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Qui puoi usare un layout personalizzato per mostrare un'immagine (con Glide) oppure solo il nome
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_drive_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        File imageFile = imageFiles.get(position);

        Glide.with(holder.itemView.getContext())
                .load(imageFile.getWebContentLink())
                .centerCrop()
                .into(holder.imageView);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageClicked(imageFile);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageFiles.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewItem);
        }
    }
}
