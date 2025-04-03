package it.pioppi.business.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import it.pioppi.R;
import it.pioppi.business.dto.settings.BaseSettingDto;
import it.pioppi.business.dto.settings.SettingType;

public class SettingAdapter extends RecyclerView.Adapter<SettingAdapter.SettingViewHolder> {

    private List<it.pioppi.business.dto.settings.BaseSettingDto> settings;
    private OnSettingClickListener listener;

    public interface OnSettingClickListener {
        void onSettingClick(BaseSettingDto setting);
    }

    public SettingAdapter(List<BaseSettingDto> settings, OnSettingClickListener listener) {
        this.settings = settings;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SettingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.settings_item, parent, false);
        return new SettingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingViewHolder holder, int position) {
        BaseSettingDto setting = settings.get(position);
        holder.nameTextView.setText(setting.getName());
        holder.iconImageView.setImageResource(setting.getIcon());
        // Se il setting è complesso, mostra la freccia; se basilare, nascondila.
        if (setting.getType() == SettingType.COMPLEX) {
            holder.arrowImageView.setVisibility(View.VISIBLE);
        } else {
            holder.arrowImageView.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSettingClick(setting);
            } else {
                Toast.makeText(v.getContext(), "Nessun listener impostato", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return settings.size();
    }

    public static class SettingViewHolder extends RecyclerView.ViewHolder {
        ImageView iconImageView;
        TextView nameTextView;
        ImageView arrowImageView;

        public SettingViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.setting_icon);
            nameTextView = itemView.findViewById(R.id.setting_name);
            arrowImageView = itemView.findViewById(R.id.setting_arrow);
        }
    }
}
