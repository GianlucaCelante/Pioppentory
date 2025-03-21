package it.pioppi.business.adapter;

import android.animation.ValueAnimator;
import android.app.PendingIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import it.pioppi.R;
import it.pioppi.business.dto.bluetooth.BluetoothDeviceDto;

public class BluetoothDevicesAdapter extends RecyclerView.Adapter<BluetoothDevicesAdapter.BluetoothDevicesViewHolder> {

    private List<BluetoothDeviceDto> bluetoothDevices;
    private final OnItemClickListener listener;
    private final OnLongItemClickListener longListener;

    public interface OnItemClickListener {
        void onItemClick(BluetoothDeviceDto bluetoothDeviceDto) throws ExecutionException, InterruptedException, PendingIntent.CanceledException;
    }

    public interface OnLongItemClickListener {
        void onLongItemClick(BluetoothDeviceDto bluetoothDeviceDto) throws ExecutionException, InterruptedException;
    }

    public BluetoothDevicesAdapter(List<BluetoothDeviceDto> bluetoothDevices, OnItemClickListener listener, OnLongItemClickListener longListener) {
        this.bluetoothDevices = bluetoothDevices != null ? new ArrayList<>(bluetoothDevices) : new ArrayList<>();
        this.listener = listener;
        this.longListener = longListener;
    }

    @NonNull
    @Override
    public BluetoothDevicesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bluetooth_device_item, parent, false);
        return new BluetoothDevicesViewHolder(view);
    }

    public List<BluetoothDeviceDto> getBluetoothDevices() {
        return bluetoothDevices;
    }

    public void setBluetoothDevices(List<BluetoothDeviceDto> bluetoothDevices) {
        this.bluetoothDevices = bluetoothDevices != null ? new ArrayList<>(bluetoothDevices) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull BluetoothDevicesViewHolder holder, int position) {

        BluetoothDeviceDto bluetoothDevice = bluetoothDevices.get(position);
        holder.deviceName.setText(bluetoothDevice.getName());
        holder.deviceAddress.setText(bluetoothDevice.getAddress());

        int startColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.default_background);
        int endColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.connected_device_background);

        if (bluetoothDevice.isConnected()) {
            animateBackgroundColorChange(holder.itemView, startColor, endColor);
        } else {
            holder.itemView.setBackgroundColor(startColor);
        }

        holder.itemView.setOnClickListener(v -> {
            try {
                listener.onItemClick(bluetoothDevice);
            } catch (ExecutionException | InterruptedException | PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            try {
                longListener.onLongItemClick(bluetoothDevice);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return bluetoothDevices != null ? bluetoothDevices.size() : 0;
    }

    private void animateBackgroundColorChange(final View view, int startColor, int endColor) {
        ValueAnimator anim = ValueAnimator.ofArgb(startColor, endColor);
        anim.setDuration(500); // Durata in millisecondi
        anim.addUpdateListener(valueAnimator -> {
            view.setBackgroundColor((int) valueAnimator.getAnimatedValue());
        });
        anim.start();
    }

    public static class BluetoothDevicesViewHolder extends RecyclerView.ViewHolder {

        TextView deviceName;
        TextView deviceAddress;

        public BluetoothDevicesViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.text_view_device_name);
            deviceAddress = itemView.findViewById(R.id.text_view_device_address);
        }
    }
}
